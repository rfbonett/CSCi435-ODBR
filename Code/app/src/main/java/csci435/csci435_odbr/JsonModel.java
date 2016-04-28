package csci435.csci435_odbr;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import android.hardware.Sensor;
import android.hardware.SensorEvent;

/**
 * Created by danielpark on 4/21/16.
 */
public class JsonModel {
    private String device_type;
    private int os_version;
    private String app_name;
    private String title;
    private String name;
    private String description_desired_outcome;
    private String description_actual_outcome;
    private HashMap<Sensor, SensorDataList> sensorData;
    private static int MAX_ITEMS_TO_PRINT = 10;

    private List<Accelerometer> accelerometerStream = new ArrayList<Accelerometer>();
    private List<Gyroscope> gyroscopeStream = new ArrayList<Gyroscope>();
    private List<Event> eventList = new ArrayList<Event>();

    private static JsonModel model = new JsonModel();
    public static JsonModel getInstance() {
        return model;
    }

    public void build_device() {
        JsonModel.getInstance().setOs_version();
        JsonModel.getInstance().setDevice_type();
        JsonModel.getInstance().setTitle();
        JsonModel.getInstance().setApp_name();
        JsonModel.getInstance().setName();
        JsonModel.getInstance().setDescription_desired_outcome();
        JsonModel.getInstance().setDescription_actual_outcome();
        JsonModel.getInstance().setEvents();

        //for sensor data
        sensorData = BugReport.getInstance().getSensorData();
        for (Sensor s : sensorData.keySet()) {
            Log.v("JSONModel", "||||||||||||||||");
            Log.v("JSONModel", "Data for Sensor: " + s.getName());
            Log.v("JSONModel", "Type of Sensor: " + s.getType());

            SensorDataList data = sensorData.get(s);
            long timeStart = data.getTime(0);


            if (s.getType() == Sensor.TYPE_ACCELEROMETER){
                for (int i = 0; i < MAX_ITEMS_TO_PRINT && i < data.numItems(); i++) {
                    Log.v("ACCELEROMETER", "Time: " + "Data: " + BugReport.getInstance().makeSensorDataReadable(data.getValues(i)));
                    Accelerometer accelerometer = new Accelerometer();
                    accelerometer.time = data.getTime(i) - timeStart;
                    accelerometer.x = data.getValues(i)[0];
                    accelerometer.y = data.getValues(i)[1];
                    accelerometer.z = data.getValues(i)[2];
                    accelerometerStream.add(accelerometer);
                }

            }

            else if (s.getType() == Sensor.TYPE_GYROSCOPE){
                for (int i = 0; i < MAX_ITEMS_TO_PRINT && i < data.numItems(); i++) {
                    Log.v("GYROSCOPE", "Time: " + "Data: " + BugReport.getInstance().makeSensorDataReadable(data.getValues(i)));
                    Gyroscope gyroscope = new Gyroscope();
                    gyroscope.time = data.getTime(i) - timeStart;
                    gyroscope.x = data.getValues(i)[0];
                    gyroscope.y = data.getValues(i)[1];
                    gyroscope.z = data.getValues(i)[2];
                    gyroscopeStream.add(gyroscope);
                }

            }



            for (int i = 0; i < MAX_ITEMS_TO_PRINT && i < data.numItems(); i++) {
                Log.v("JSONModel", "Time: " + (data.getTime(i) - timeStart) + "| " + "Data: " + BugReport.getInstance().makeSensorDataReadable(data.getValues(i)));
            }
            int printed = data.numItems() - MAX_ITEMS_TO_PRINT;
            Log.v("JSONModel", "And " + (printed > 0 ? printed : 0) + " more");
            Log.v("JSONModel", "|*************************************************|");
        }


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

    public double getReport_start_time(){
        return 0;
    }

    //get the last event of eventList + duration
    public double getReport_end_time(){
        int last_item = BugReport.getInstance().getEventList().size() - 1;
        //Log.v("Size of EventList", "" + BugReport.getInstance().getEventList().size());
        //Log.v("Time of last item", "" + BugReport.getInstance().getEventList().get(last_item).getTime());
        //Log.v("Duration of last item", "" + BugReport.getInstance().getEventList().get(last_item).getDuration());
        return BugReport.getInstance().getEventList().get(last_item).getTime() + BugReport.getInstance().getEventList().get(last_item).getDuration();

    }

    public List<Event> setEvents(){
        //for eventList
        for(int i = 0; i < BugReport.getInstance().getEventList().size(); i++){
            Log.v("FOR bug eventList:", "" + i);
            Log.v("FOR bug eventList:", "" + BugReport.getInstance().getEventList().get(i).getData());

            Event temp = new Event();
            temp.screenshot = BugReport.getInstance().getEventList().get(i).getScreenshot().getFilename();
            temp.event_start_time = BugReport.getInstance().getEventList().get(i).getTime();
            temp.event_end_time = BugReport.getInstance().getEventList().get(i).getTime() + BugReport.getInstance().getEventList().get(i).getDuration();
            temp.inputList = BugReport.getInstance().getEventList().get(i).getInputEvents();
            temp.description = BugReport.getInstance().getEventList().get(i).getEventDescription();
            temp.hierarchy = BugReport.getInstance().getEventList().get(i).getHierarchy();

            JsonModel.getInstance().eventList.add(temp);
        }
        return JsonModel.getInstance().eventList;
    }


    public int tester() {
        build_device();

        //Log Title, Reporter Name and Description
        Log.v("JSON", "os_version: " + JsonModel.getInstance().getOs_version());
        Log.v("JSON", "device_type: " + JsonModel.getInstance().getDevice_type());

        Log.v("JSON", "app_name: " + app_name);
//        Log.v("JSON", "app_version: " + app_version);

        Log.v("JSON", "title: " + JsonModel.getInstance().getTitle());
        Log.v("JSON", "name: " + JsonModel.getInstance().getName());

        Log.v("JSON", "description_desired_outcome: " + JsonModel.getInstance().getDescription_desired_outcome());
        Log.v("JSON", "description_actual_outcome: " + JsonModel.getInstance().getDescription_actual_outcome());

        Log.v("JSON", "report_start_time: " + JsonModel.getInstance().getReport_start_time());
        Log.v("JSON", "report_end_time: " + JsonModel.getInstance().getReport_end_time());


        for(int i = 0; i < eventList.size(); i++){
            Log.v("FOR JSON eventList:", "" + i);
            Log.v("FOR JSON eventList:", "screenshot" + eventList.get(i).screenshot);
            Log.v("FOR JSON eventList:", "starttime" + eventList.get(i).event_start_time);
            Log.v("FOR JSON eventList:", "endtime" + eventList.get(i).event_end_time);
            Log.v("FOR JSON eventList:", "inputList" + eventList.get(i).inputList);
            Log.v("FOR JSON eventList:", "desc" + eventList.get(i).description);
            Log.v("FOR JSON eventList:", "hierarchy" + eventList.get(i).hierarchy);
        }
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

class Event {
    String screenshot;
    double event_start_time;
    double event_end_time;
    List<int[]> inputList;
    String description;
    String hierarchy;
    //String Orientation;
}

class Accelerometer {
    double time;
    double x;
    double y;
    double z;
}

class Gyroscope {
    double time;
    double x;
    double y;
    double z;
}


