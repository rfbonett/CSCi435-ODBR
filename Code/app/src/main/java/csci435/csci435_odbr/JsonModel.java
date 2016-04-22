package csci435.csci435_odbr;

import android.hardware.Sensor;
import android.util.Log;
import com.google.gson.Gson;
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

    private static JsonModel model = new JsonModel();
    public static JsonModel getInstance() {
        return model;
    }

    public void build_device() {
        JsonModel.getInstance().setOs_version();
        JsonModel.getInstance().setDevice_type();
        app_name = "";
        app_version = "";
        title = BugReport.getInstance().getTitle();
        name = BugReport.getInstance().getReporterName();
        description_desired_outcome = BugReport.getInstance().getDesiredOutcome();
        description_actual_outcome = BugReport.getInstance().getActualOutcome();
        report_start_time = RecordFloatingWidget.getReportStartTime();
        report_end_time = RecordFloatingWidget.getReportEndTime();
        accelerometer_stream = "";
        gyroscope_stream = "";
        events = "";
    }

    public void setDevice_type(){
        device_type = android.os.Build.MODEL;
    }
    public String getDevice_type(){
        return device_type;
    }

    public void setOs_version(){
        os_version = android.os.Build.VERSION.SDK_INT;
    }
    public int getOs_version(){
        return os_version;
    }
    public int tester() {
        build_device();

        //Log Title, Reporter Name and Description
        Log.v("JSON", "os_version: " + JsonModel.getInstance().getOs_version());
        Log.v("JSON", "device_type: " + JsonModel.getInstance().getDevice_type());

        Log.v("JSON", "app_name: " + app_name);
        Log.v("JSON", "app_version: " + app_version);

        Log.v("JSON", "title: " + title);
        Log.v("JSON", "name: " + name);

        Log.v("JSON", "description_desired_outcome: " + description_desired_outcome);
        Log.v("JSON", "description_actual_outcome: " + description_actual_outcome);

        Log.v("JSON", "report_start_time: " + report_start_time);
        Log.v("JSON", "report_end_time: " + report_end_time);
        JsonModel.getInstance().JavatoJson();
        return 1;
    }

    public int JavatoJson(){
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        System.out.println(gson.toJson(albums))


    }
}
