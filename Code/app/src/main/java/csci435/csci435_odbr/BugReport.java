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
 * Singleton class containing all information for a specific bug report. It will hold all sensor data as well as a list
 * of report events which contain their specified coordinates and times of events.
 */
public class BugReport {
    public static int colors[] = {Color.BLUE, Color.GREEN, Color.RED, Color.CYAN, Color.YELLOW, Color.MAGENTA};
    private static int MAX_ITEMS_TO_PRINT = 10;

    private HashMap<Sensor, SensorDataList> sensorData = new HashMap<Sensor, SensorDataList>();
    private HashMap<Sensor, Bitmap> sensorGraphs = new HashMap<Sensor, Bitmap>();
    private List<ReportEvent> eventList = new ArrayList<ReportEvent>();
    private HashMap<Long, Integer> orientations = new HashMap<Long, Integer>();
    private String title = "";
    private String reporterName = "";
    private String desiredOutcome = "";
    private String actualOutcome = "";

    private static BugReport ourInstance = new BugReport();

    public static BugReport getInstance() {
        return ourInstance;
    }

    private BugReport() {
        clearReport();
    }

    //resets the data, called after report is submitted
    public void clearReport() {
        sensorData.clear();
        sensorGraphs.clear();
        eventList.clear();
        title = "";
        reporterName = "";
        desiredOutcome = "";
        actualOutcome = "";
    }


    public void addEvent(ReportEvent e) {
        eventList.add(e);
    }

    //adds a sensor 'event' to a specific sensor
    public void addSensorData(Sensor s, SensorEvent e) {
        if (!sensorData.containsKey(s)) {
            sensorData.put(s, new SensorDataList());
        }
        sensorData.get(s).addData(e.timestamp, e.values.clone());
    }

    public void addOrientation(long time, int orientation) {
        orientations.put(time, orientation);
    }

    public void setDesiredOutcome(String s) { desiredOutcome = s;}

    public void setActualOutcome(String s) {actualOutcome = s;}

    public void setTitle(String s) {title = s;}

    public void setReporterName(String s) {reporterName = s;}

    /**
     * Returns its data as a formatted JSON file; currently outputs data to LogCat
     * @return
     */




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

        int height = Globals.height / 2;
        Bitmap b = Bitmap.createBitmap(Globals.width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);

        SensorDataList data = sensorData.get(s);
        Paint color = new Paint();
        color.setColor(Color.BLACK);
        color.setStrokeWidth(5);
        c.drawARGB(255, 200, 200, 200);
        c.drawLine(0, 0, 0, height, color);
        c.drawLine(0, height / 2, Globals.width, height / 2, color);
        color.setStrokeWidth(3);

        long timeMod = data.getElapsedTime(data.numItems() - 1) / Globals.width;
        timeMod = timeMod > 0 ? timeMod : 1;
        for (int k = 0; k < data.sizeOfValueArray() && k < colors.length; k++) {
            float valueMod = data.meanValue(k) / (height / 2);
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
    public HashMap<Sensor, SensorDataList> getSensorData() {
        return sensorData;
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

    /*public float stDev(int index) {
        float stdev = 0;
        float mean = meanValue(index);
        for (int i = 0; i < numItems; i++) {
            float f = values.get(i)[index];
            stdev += (f - mean) * (f - mean);
        }
        return (float) Math.sqrt(stdev / numItems);
    }
    */

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