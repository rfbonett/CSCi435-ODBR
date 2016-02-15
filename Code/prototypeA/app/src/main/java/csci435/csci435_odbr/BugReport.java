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

/**
 * Created by Rich on 2/11/16.
 */
public class BugReport {
    private HashMap<Sensor, List<SensorEvent>> sensorData;
    private List<AccessibilityEvent> userEvents;
    private SparseArray<Bitmap> screenshots;
    private SparseArray<String> benchmarkDescriptions;
    private String title;
    private String reporterName;
    private int events;

    private static BugReport ourInstance = new BugReport();

    public static BugReport getInstance() {
        return ourInstance;
    }

    private BugReport() {
        sensorData = new HashMap<Sensor, List<SensorEvent>>();
        userEvents = new ArrayList<AccessibilityEvent>();
        screenshots = new SparseArray<Bitmap>();
        benchmarkDescriptions = new SparseArray<String>();
        title = "";
        reporterName = "";
        events = 0;
    }

    public void clearReport() {
        sensorData.clear();
        userEvents.clear();
        screenshots.clear();
        benchmarkDescriptions.clear();
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

    public void addBenchmarkDescription(String s) {
        benchmarkDescriptions.put(events, s);
    }

    public void addTitle(String s) {
        title = s;
    }

    public void addReporter(String s) {
        reporterName = s;
    }

    public JSONObject toJSON() {

        return new JSONObject();
    }

    /* Getters */
    public List<AccessibilityEvent> getUserEvents() {
        return userEvents;
    }
    public SparseArray<Bitmap> getScreenshots() {
        return screenshots;
    }
    public SparseArray<String> getBenchmarkDescriptions() {
        return benchmarkDescriptions;
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
