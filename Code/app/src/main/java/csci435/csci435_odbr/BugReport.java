package csci435.csci435_odbr;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONObject;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.view.accessibility.AccessibilityEvent;
import android.graphics.Bitmap;
import android.util.SparseArray;
import android.util.Log;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.Rect;
import android.view.accessibility.AccessibilityNodeInfo;
import android.content.res.Configuration;

/**
 * Created by Rich on 2/11/16.
 */
public class BugReport {
    private int colors[] = {Color.BLUE, Color.GREEN, Color.RED, Color.CYAN, Color.YELLOW, Color.MAGENTA};
    private int MAX_ITEMS_TO_PRINT = 10;
    private HashMap<Sensor, SensorDataList> sensorData;
    private ArrayList<Sensor> sensors;
    private List<Events> eventList;
    private SparseArray<Bitmap> screenshots;
    private String title;
    private String reporterName;
    private int events;
    private String desiredOutcome;
    private String actualOutcome;
    private HashMap<Sensor, Bitmap> sensorGraphs;

    private static BugReport ourInstance = new BugReport();

    public static BugReport getInstance() {
        return ourInstance;
    }

    private BugReport() {
        sensorData = new HashMap<Sensor, SensorDataList>();
        eventList = new ArrayList<Events>();
        screenshots = new SparseArray<Bitmap>();
        title = "";
        reporterName = "";
        events = 0;
        sensors = new ArrayList<Sensor>();
        sensorGraphs = new HashMap<Sensor, Bitmap>();
    }

    public void clearReport() {
        sensorData.clear();
        eventList.clear();
        screenshots.clear();
        title = "";
        reporterName = "";
        events = 0;
    }

    public void addUserEvent(Events e) {
        eventList.add(e);
        ++events;
    }

    public void addSensorData(Sensor s, SensorEvent e) {
        if (!sensorData.containsKey(s)) {
            sensorData.put(s, new SensorDataList());
            sensors.add(s);
        }
        sensorData.get(s).addData(e.timestamp, e.values.clone());
    }

    public void addScreenshot(Bitmap s) {
        screenshots.put(events, s);
    }

    public void addDesiredOutcome(String s) { desiredOutcome = s;}

    public void addActualOutcome(String s) {actualOutcome = s;}

    public void addTitle(String s) {
        title = s;
    }

    public void addReporter(String s) {
        reporterName = s;
    }

    /**
     * Returns its data as a formatted JSON file; currently outputs data to LogCat
     * @return
     */
    public JSONObject toJSON() {
        Log.v("BugReport", "Reporter: " + reporterName);
        Log.v("BugReport", "Title: " + title);
        Log.v("BugReport", "What Should Happen: " + desiredOutcome);
        Log.v("BugReport", "What Does Happen: " + actualOutcome);

        for (Sensor s : sensorData.keySet()) {
            Log.v("BugReport", "|*************************************************|");
            Log.v("BugReport", "Data for Sensor: " + s.getName());
            SensorDataList data = sensorData.get(s);
            long timeStart = data.getTime(0);
            for (int i = 0; i < MAX_ITEMS_TO_PRINT && i < data.numItems(); i++) {
                Log.v("BugReport", "Time: " + (data.getTime(i) - timeStart) + "| " + "Data: " + readable(data.getValues(i)));
            }
            int printed = data.numItems() - MAX_ITEMS_TO_PRINT;
            Log.v("BugReport", "And " + (printed > 0 ? printed : 0) + " more");
            Log.v("BugReport", "|*************************************************|");
        }

        for(int i = 0; i < eventList.size(); i++){
            Log.v("Event Number:", "" + i);
            eventList.get(i).printData();
        }
        return new JSONObject();
    }

    private String readable(float[] input) {
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

        Bitmap b = Bitmap.createBitmap(Globals.width, Globals.height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);

        SensorDataList data = sensorData.get(s);
        Paint color = new Paint();
        color.setColor(Color.BLACK);
        color.setStrokeWidth(5);
        c.drawARGB(255, 200, 200, 200);
        c.drawLine(0, 0, 0, Globals.height, color);
        c.drawLine(0, Globals.height/2, Globals.width, Globals.height/2, color);
        color.setStrokeWidth(3);

        long timeMod = data.getElapsedTime(data.numItems() - 1) / Globals.width;
        timeMod = timeMod > 0 ? timeMod : 1;
        for (int k = 0; k < data.sizeOfValueArray() && k < colors.length; k++) {
            float valueMean = data.meanValue(k);
            float valueStDev = data.stDev(k);
            float rangeY = valueStDev * 6;
            valueMean = valueMean > 0 ? valueMean : 1;
            color.setColor(colors[k]);
            float startX = data.getElapsedTime(0) / timeMod;
            float startY = Globals.height - ((Globals.height/2) * (data.getValues(0)[k] / valueMean));
            for (int i = 1; i < data.numItems(); i++) {
                float endX = data.getElapsedTime(i) / timeMod;
                float endY = Globals.height - ((Globals.height/2) * (data.getValues(i)[k] / valueMean));
                c.drawLine(startX, startY, endX, endY, color);
                startX = endX;
                startY = endY;
            }
        }
        sensorGraphs.put(s, b);
        return b;
    }


    /* Getters */
    public List<Events> getUserEvents() {
        return eventList;
    }
    public SparseArray<Bitmap> getScreenshots() {
        return screenshots;
    }
    public String getReporterName() {
        return reporterName;
    }
    public String getTitle() {
        return title;
    }
    public int numSensors() {return sensorData.keySet().size();}
    public Sensor getSensor(int ndx) {return sensors.get(ndx);}
}


class Events {
    private Long timeStamp;
    private int eventType;
    private AccessibilityNodeInfo source;
    private CharSequence packageName;
    private Rect boundsInParent;
    private Rect boundsInScreen;

    public Events(AccessibilityEvent e){
        packageName = e.getPackageName();
        eventType = e.getEventType();
        timeStamp = e.getEventTime();
        source = e.getSource();
        boundsInParent = new Rect();
        boundsInScreen = new Rect();
        source.getBoundsInParent(boundsInParent);
        source.getBoundsInScreen(boundsInScreen);
    }


    public void printData(){

        Log.v("Event: time", "" + timeStamp);
        Log.v("Event: type", "" + eventType);
        Log.v("Event: package name", "" + packageName);
        //Log.v("Event: source", "" + source);
        Log.v("Event: bounds in parent", "" + boundsInParent);
        Log.v("Event: bounds in screen", "" + boundsInScreen);
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