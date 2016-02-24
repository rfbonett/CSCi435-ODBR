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

/**
 * Created by Rich on 2/11/16.
 */
public class BugReport {
    private int MAX_ITEMS_TO_PRINT = 10;
    private HashMap<Sensor, SensorDataList> sensorData;
    private List<AccessibilityEvent> userEvents;
    private SparseArray<Bitmap> screenshots;
    private String title;
    private String reporterName;
    private int events;
    private String desiredOutcome;
    private String actualOutcome;

    private static BugReport ourInstance = new BugReport();

    public static BugReport getInstance() {
        return ourInstance;
    }

    private BugReport() {
        sensorData = new HashMap<Sensor, SensorDataList>();
        userEvents = new ArrayList<AccessibilityEvent>();
        screenshots = new SparseArray<Bitmap>();
        title = "";
        reporterName = "";
        events = 0;
    }

    public void clearReport() {
        sensorData.clear();
        userEvents.clear();
        screenshots.clear();
        title = "";
        reporterName = "";
        events = 0;
    }

    public void addUserEvent(AccessibilityEvent e) {
        userEvents.add(e);
        ++events;
    }

    public void addSensorData(Sensor s, SensorEvent e) {
        if (!sensorData.containsKey(s)) {
            sensorData.put(s, new SensorDataList());
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
            Log.v("BugReport", "Data for Sensor: " + s.getName());
            SensorDataList data = sensorData.get(s);
            long timeStart = data.getTime(0);
            for (int i = 0; i < MAX_ITEMS_TO_PRINT && i < data.numItems(); i++) {
                Log.v("BugReport", "Time: " + (data.getTime(i) - timeStart) + "| " + "Data: " + readable(data.getValues(i)));
            }
            int printed = data.numItems() - MAX_ITEMS_TO_PRINT;
            Log.v("BugReport", "And " + (printed > 0 ? printed : 0) + " more");
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


    /* Getters */
    public List<AccessibilityEvent> getUserEvents() {
        return userEvents;
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
}

class SensorDataList {
    private ArrayList<Long> timestamps;
    private ArrayList<float[]> values;

    public SensorDataList() {
        timestamps = new ArrayList<Long>();
        values = new ArrayList<float[]>();
    }

    public void addData(long timestamp, float[] value) {
        timestamps.add(timestamp);
        values.add(value);
    }

    public long getTime(int index) {
        return timestamps.get(index);
    }

    public float[] getValues(int index) {
        return values.get(index);
    }

    public int numItems() {
        return timestamps.size();
    }
}