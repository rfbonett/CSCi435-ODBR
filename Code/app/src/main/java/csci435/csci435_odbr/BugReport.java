package csci435.csci435_odbr;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONObject;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.graphics.Bitmap;
import android.util.Log;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.view.accessibility.AccessibilityNodeInfo;

/**
 * Created by Rich on 2/11/16.
 */
public class BugReport {
    private static int colors[] = {Color.BLUE, Color.GREEN, Color.RED, Color.CYAN, Color.YELLOW, Color.MAGENTA};
    private static int MAX_ITEMS_TO_PRINT = 10;

    private final long report_start_time = 0;
    private long report_end_time;
    private HashMap<Sensor, SensorDataList> sensorData = new HashMap<Sensor, SensorDataList>();
    private HashMap<Sensor, Bitmap> sensorGraphs = new HashMap<Sensor, Bitmap>();
    private ArrayList<GetEvent> getEventList = new ArrayList<GetEvent>();
    private List<ReportEvent> eventList = new ArrayList<ReportEvent>();
    private String title = "";
    private String reporterName = "";
    private String desiredOutcome = "";
    private String actualOutcome = "";
    private ScreenshotManager sm;
    private HierarchyDumpManager hdm;

    private static BugReport ourInstance = new BugReport();

    public static BugReport getInstance() {
        return ourInstance;
    }

    private BugReport() {
        clearReport();
    }

    public void clearReport() {
        sensorData.clear();
        sensorGraphs.clear();
        eventList.clear();
        getEventList.clear();
        title = "";
        reporterName = "";
        desiredOutcome = "";
        actualOutcome = "";
        initializeManagers();
    }

    private void initializeManagers() {
        sm = new ScreenshotManager();
        hdm = new HierarchyDumpManager();
    }


    public void addGetEvent(GetEvent get_event){
        //throw this into a getEventQueue
        getEventList.add(get_event);
    }

    public long getReport_start_time(){
        return report_start_time;
    }

    public long getReport_end_time(){
        return report_end_time;
    }
    public void printGetEvents() {
        for (GetEvent e : getEventList) {
            e.printValues();
        }
        for (ReportEvent e : eventList) {
            Log.v("GetEvent", "ReportEvent: " + e.getData());
        }
    }

    public void matchGetEventsToReportEvents() {
        if (getEventList.size() == 0 || eventList.size() == 0) {
            return;
        }

        //Normalize times
        long getEventStartTime = getEventList.get(0).getStart();
        long reportEventStartTime = eventList.get(0).getTime();
        for (GetEvent e : getEventList) {
            e.setStart(e.getStart() - getEventStartTime);
        }
        for (ReportEvent e :  eventList) {
            e.setTime(e.getTime() - reportEventStartTime);
        }
        //Match events
        try {
            int ndx = 0;
            for (ReportEvent e : eventList) {
                ndx = closestGetEvent(e.getTime(), ndx);
                e.setDuration(getEventList.get(ndx).getDuration());
                for (int[] coord : getEventList.get(ndx).get_coords()) {
                    e.addInputEvents(coord);
                }
            }
        } catch (Exception e) {Log.v("BugReport", "Error matching GetEvents to ReportEvents");}
        int i = 0;
        ReportEvent cur, last = eventList.get(i);
        while (++i < eventList.size()) {
            cur = eventList.get(i);
            long time = cur.getTime() - last.getTime() - last.getDuration();
            last.setWaitTime(time > 0 ? time : 0);
            last = cur;
        }
    }

    private int closestGetEvent(long time, int ndx) {
        while (ndx + 1 < getEventList.size() && getEventList.get(ndx + 1).getStart() - time < time - getEventList.get(ndx).getStart()) {
            ++ndx;
        }
        return ndx;
    }

    public void addEvent(ReportEvent e, AccessibilityNodeInfo node) {
        e.addHierarchyDump(hdm.takeHierarchyDump(node));
        e.addScreenshot(sm.takeScreenshot());
        eventList.add(e);
    }


    public void addSensorData(Sensor s, SensorEvent e) {
        if (!sensorData.containsKey(s)) {
            sensorData.put(s, new SensorDataList());
        }
        sensorData.get(s).addData(e.timestamp, e.values.clone());
    }

    public void setDesiredOutcome(String s) { desiredOutcome = s;}

    public void setActualOutcome(String s) {actualOutcome = s;}

    public void setTitle(String s) {title = s;}

    public void setReporterName(String s) {reporterName = s;}

    /**
     * Returns its data as a formatted JSON file; currently outputs data to LogCat
     * @return
     */

    public JSONObject toJSON() {
        //Log Title, Reporter Name and Description
        Log.v("BugReport", "Reporter: " + reporterName);
        Log.v("BugReport", "Title: " + title);
        Log.v("BugReport", "What Should Happen: " + desiredOutcome);
        Log.v("BugReport", "What Does Happen: " + actualOutcome);

        //Log Sensor Data, each sensor capped at MAX_ITEMS_TO_PRINT
        for (Sensor s : sensorData.keySet()) {
            Log.v("BugReport", "|*************************************************|");
            Log.v("BugReport", "Data for Sensor: " + s.getName());
            SensorDataList data = sensorData.get(s);
            long timeStart = data.getTime(0);
            for (int i = 0; i < MAX_ITEMS_TO_PRINT && i < data.numItems(); i++) {
                Log.v("BugReport", "Time: " + (data.getTime(i) - timeStart) + "| " + "Data: " + makeSensorDataReadable(data.getValues(i)));
            }
            int printed = data.numItems() - MAX_ITEMS_TO_PRINT;
            Log.v("BugReport", "And " + (printed > 0 ? printed : 0) + " more");
            Log.v("BugReport", "|*************************************************|");
        }

        return new JSONObject();
    }


    private String makeSensorDataReadable(float[] input) {
        String s = "";
        for (float f : input) {
            s +=  f + " | ";
        }
        return s;
    }


    public Bitmap drawSensorData(Sensor s) {
        if (sensorGraphs.containsKey(s)) {
            return sensorGraphs.get(s);
        }

        Globals.height = Globals.height / 2;
        Bitmap b = Bitmap.createBitmap(Globals.width, Globals.height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);

        SensorDataList data = sensorData.get(s);
        Paint color = new Paint();
        color.setColor(Color.BLACK);
        color.setStrokeWidth(5);
        c.drawARGB(255, 200, 200, 200);
        c.drawLine(0, 0, 0, Globals.height, color);
        c.drawLine(0, Globals.height / 2, Globals.width, Globals.height / 2, color);
        color.setStrokeWidth(3);

        long timeMod = data.getElapsedTime(data.numItems() - 1) / Globals.width;
        timeMod = timeMod > 0 ? timeMod : 1;
        for (int k = 0; k < data.sizeOfValueArray() && k < colors.length; k++) {
            float valueMod = data.meanValue(k) / (Globals.height / 2);
            valueMod = valueMod > 0 ? valueMod : 1;
            color.setColor(colors[k]);
            float startX = data.getElapsedTime(0) / timeMod;
            float startY = data.getValues(0)[k] / valueMod;
            for (int i = 1; i < data.numItems(); i++) {
                float endX = data.getElapsedTime(i) / timeMod;
                float endY = data.getValues(i)[k] / valueMod;
                c.drawLine(startX, startY, endX, endY, color);
                startX = endX;
                startY = endY;
            }
        }
        sensorGraphs.put(s, b);
        Globals.height = Globals.height * 2;
        return b;
    }


    /* Getters */
    public String getReporterName() {
        return reporterName;
    }
    public String getTitle() {
        return title;
    }
    public String getDesiredOutcome(){
        return desiredOutcome;
    }
    public String getActualOutcome(){
        return actualOutcome;
    }
    public List<ReportEvent> getEventList() {
        return eventList;
    }
    public int numEvents() {
        return eventList.size();
    }
    public ReportEvent getEventAtIndex(int ndx) {
        return eventList.get(ndx);
    }
}


class SensorDataList {
    private ArrayList<Long> timestamps;
    private ArrayList<float[]> values;
    private float[] valueSums;
    private int numItems;

    public SensorDataList() {
        timestamps = new ArrayList<Long>();
        values = new ArrayList<float[]>();
        numItems = 0;
    }

    public void addData(long timestamp, float[] value) {
        ++numItems;
        timestamps.add(timestamp);
        values.add(value);
        if (numItems == 1) {
            valueSums = new float[value.length];
        }
        for (int i = 0; i < value.length; i++) {
            valueSums[i] += value[i];
        }
    }

    public long getTime(int index) {
        return timestamps.get(index);
    }

    public long getElapsedTime(int index) {
        return timestamps.get(index) - timestamps.get(0);
    }


    public float meanValue(int index) {
        return valueSums[index] / numItems;
    }

    public float stDev(int index) {
        float stdev = 0;
        float mean = meanValue(index);
        for (int i = 0; i < numItems; i++) {
            float f = values.get(i)[index];
            stdev += (f - mean) * (f - mean);
        }
        return (float) Math.sqrt(stdev / numItems);
    }

    public float[] getValues(int index) {
        return values.get(index);
    }

    public int numItems() {
        return numItems;
    }

    public int sizeOfValueArray() {
        return values.get(0).length;
    }
}