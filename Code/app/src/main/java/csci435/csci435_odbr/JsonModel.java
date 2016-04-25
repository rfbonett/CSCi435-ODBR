package csci435.csci435_odbr;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

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
    //private List<float> accelerometer_streamlist;
    //private String gyroscope_stream;
    private List<ReportEvent> eventList = new ArrayList<ReportEvent>();

    private static JsonModel model = new JsonModel();
    public static JsonModel getInstance() {
        return model;
    }



    public void build_device() {
        JsonModel.getInstance().setOs_version();
        JsonModel.getInstance().setDevice_type();
        JsonModel.getInstance().setTitle();
        JsonModel.getInstance().setApp_name();
        //version couldn't be implemented yet
        JsonModel.getInstance().setName();
        JsonModel.getInstance().setDescription_desired_outcome();
        JsonModel.getInstance().setDescription_actual_outcome();
//        JsonModel.getInstance().setEvents();


    }

    public void setApp_name(){
        app_name = Globals.packageName;
    }
    public String getApp_name(){
        return app_name;
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

    public void setName(){
        name = BugReport.getInstance().getReporterName();
    }
    public String getName(){
        return name;
    }

    public void setDescription_desired_outcome(){
        description_desired_outcome = BugReport.getInstance().getDesiredOutcome();
    }
    public String getDescription_desired_outcome(){
        return description_desired_outcome;
    }

    public void setTitle(){
        title = BugReport.getInstance().getTitle();
    }
    public String getTitle(){
        return title;
    }

    public void setDescription_actual_outcome(){
        description_actual_outcome = BugReport.getInstance().getActualOutcome();
    }
    public String getDescription_actual_outcome(){
        return description_actual_outcome;
    }
    /*
    public void setReport_start_time(){
        report_start_time = RecordFloatingWidget.getReportStartTime();
    }
    public double getReport_start_time(){
        return report_start_time;
    }

    public void setReport_end_time(){
        report_end_time = RecordFloatingWidget.getReportEndTime();
    }
    public double getReport_end_time(){
        return report_end_time;
    }

    public void setAccelerometer_stream(){
        for(int i = 0; i < BugReport.getInstance().getEventList().size(); i++){
            Log.v("FOR JSON:", "" + i);
            BugReport.getInstance().getEventList().get(i).printData();
        } */


    //public void setEvents(){
    //    for(int i = 0; i < BugReport.getInstance().getEventList().size(); i++){
    //        Log.v("FOR JSON:", "" + i);
    //        BugReport.getInstance().getEventList().get(i).get/**/;
    //    }
    //}

    public int tester() {
        build_device();

        //Log Title, Reporter Name and Description
        Log.v("JSON", "os_version: " + JsonModel.getInstance().getOs_version());
        Log.v("JSON", "device_type: " + JsonModel.getInstance().getDevice_type());

        Log.v("JSON", "app_name: " + app_name);
        Log.v("JSON", "app_version: " + app_version);

        Log.v("JSON", "title: " + JsonModel.getInstance().getTitle());
        Log.v("JSON", "name: " + JsonModel.getInstance().getName());

        Log.v("JSON", "description_desired_outcome: " + JsonModel.getInstance().getDescription_desired_outcome());
        Log.v("JSON", "description_actual_outcome: " + JsonModel.getInstance().getDescription_actual_outcome());

        //Log.v("JSON", "report_start_time: " + JsonModel.getInstance().getReport_start_time());
        //Log.v("JSON", "report_end_time: " + JsonModel.getInstance().getReport_end_time());
        JsonModel.getInstance().JavatoJson();
        return 1;
    }

    public int JavatoJson(){
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.setPrettyPrinting().create();
        System.out.println(gson.toJson(JsonModel.getInstance()));
        return 1;

    }
}

////class ReportEvent {
//    String screenshot;
//    double event_start_time;
//    double event_end_time;
//    //inputs
//    String hierarchy;
//    //Orientation
//
//    //public void setScreenshot(){
//      //  screenshot = BugReport.getInstance().;
//    //}
//
//
//}