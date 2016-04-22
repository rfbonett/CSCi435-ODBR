package csci435.csci435_odbr;

import android.hardware.Sensor;
import android.util.Log;

import org.json.JSONObject;

/**
 * Created by danielpark on 4/21/16.
 */
public class JsonModel {
    private String device_type;
    private int os_version;
    private String app_name;
    private String app_version;
    private String title;
    private String name;
    private String description_desired_outcome;
    private String description_actual_outcome;
    private double report_start_time;
    private double report_end_time;
    private String accelerometer_stream;
    private String gyroscope_stream;
    private String events;

    public void Java_toJson() {
        os_version = android.os.Build.VERSION.SDK_INT;
        device_type = android.os.Build.MODEL;
        app_name = "";
        app_version = "";
        title = BugReport.getInstance().getTitle();
        name = BugReport.getInstance().getReporterName();
        description_desired_outcome = BugReport.getInstance().getDesiredOutcome();
        description_actual_outcome = BugReport.getInstance().getActualOutcome();
        report_start_time = 11;
        report_end_time = 11;
        accelerometer_stream = "";
        gyroscope_stream = "";
        events = "";
    }
    public int tester() {
        //Log Title, Reporter Name and Description
        Log.v("JSON", "os_version: " + os_version);
        Log.v("JSON", "device_type: " + device_type);

        Log.v("JSON", "app_name: " + app_name);
        Log.v("JSON", "app_version: " + app_version);

        Log.v("JSON", "title: " + title);
        Log.v("JSON", "name: " + name);

        Log.v("JSON", "description_desired_outcome: " + description_desired_outcome);
        Log.v("JSON", "description_actual_outcome: " + description_actual_outcome);

        Log.v("JSON", "report_start_time: " + report_start_time);
        Log.v("JSON", "report_end_time: " + report_end_time);
        return 1;
    }
}
