package csci435.csci435_odbr;

import java.util.Comparator;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONObject;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.view.accessibility.AccessibilityEvent;
import android.graphics.Bitmap;
import android.util.SparseArray;
import android.util.Log;

/**
 * Created by Rich on 2/11/16.
 */
public class BugReport {
    private HashMap<Sensor, HashMap<Long, float[]>> sensorData;
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
        sensorData = new HashMap<Sensor, HashMap<Long, float[]>>();
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
            sensorData.put(s, new HashMap<Long, float[]>());
        }
        sensorData.get(s).put(e.timestamp, e.values.clone());
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

    public JSONObject toJSON() {
        Log.v("BugReport", "Reporter: " + reporterName);
        Log.v("BugReport", "Title: " + title);
        Log.v("BugReport", "What Should Happen: " + desiredOutcome);
        Log.v("BugReport", "What Does Happen: " + actualOutcome);

        for (Sensor s : sensorData.keySet()) {
            Log.v("BugReport", "Data for Sensor: " + s.getName());
            HashMap<Long, float[]> data = sensorData.get(s);
            ArrayList<Long> timestamps = new ArrayList<Long>(data.keySet());
            Collections.sort(timestamps);

            int i = 0;
            long timeStart = timestamps.get(0);
            for (long timestamp : timestamps) {
                Log.v("BugReport", "Time: " + (timestamp - timeStart) + "| " + "Data: " + readable(data.get(timestamp)));
                if (++i > 10) {break;}
            }
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
