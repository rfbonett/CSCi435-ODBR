package csci435.csci435_odbr;

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
}
