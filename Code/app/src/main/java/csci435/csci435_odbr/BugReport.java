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
    private HashMap<Sensor, List<SensorEvent>> sensorData;
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
        sensorData = new HashMap<Sensor, List<SensorEvent>>();
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
            sensorData.put(s, new ArrayList<SensorEvent>());
        }
        sensorData.get(s).add(e);
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

        return new JSONObject();
    }

    /* Getters */
    public List<AccessibilityEvent> getUserEvents() {
        return userEvents;
    }
    public SparseArray<Bitmap> getScreenshots() {
        return screenshots;
    }
    public HashMap<Sensor, List<SensorEvent>> getSensorData() {
        return sensorData;
    }
    public String getReporterName() {
        return reporterName;
    }
    public String getTitle() {
        return title;
    }
}
