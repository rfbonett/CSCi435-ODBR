package csci435.csci435_odbr;

import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import android.os.AsyncTask;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by danielpark on 4/21/16.
 * The JsonModel translates the data stored in the BugReport into JSON format, sending it off to
 * the FUSION server.
 */
public class JsonModel {
    private String device_type;
    private int os_version;
    private String app_version;
    private String app_name;
    private String title;
    private String name;
    private String description_desired_outcome;
    private String description_actual_outcome;
    private static int MAX_ITEMS_TO_PRINT = 10;

    private List<Accelerometer> accelerometerStream = new ArrayList<Accelerometer>();
    private List<Gyroscope> gyroscopeStream = new ArrayList<Gyroscope>();
    private List<Event> eventList = new ArrayList<Event>();

    private static JsonModel model = new JsonModel();
    public static JsonModel getInstance() {
        return model;
    }

    /**
     * Puts the reporting device's information into the model
     * @throws Exception
     */
    public void build_device() throws Exception {
        JsonModel.getInstance().setOs_version();
        JsonModel.getInstance().setDevice_type();
        JsonModel.getInstance().setApp_version();
        JsonModel.getInstance().setTitle();
        JsonModel.getInstance().setApp_name();
        JsonModel.getInstance().setName();
        JsonModel.getInstance().setDescription_desired_outcome();
        JsonModel.getInstance().setDescription_actual_outcome();
        JsonModel.getInstance().setEvents();
        JsonModel.getInstance().setSensorData();

        //for submitting json to server
        long millis = System.currentTimeMillis();
        String payload = JsonModel.getInstance().JavatoJson();
        AsyncHttpPut asyncHttpPut = new AsyncHttpPut(payload);
        asyncHttpPut.execute("http://23.92.18.210:5984/odbr/" + Long.toString(millis), payload);
        Log.v("Json submission", "Server received: " + Long.toString(millis));
    }


    /**
     * Finds the version of the application being reported and stores it in the model
     * @return
     */
    public String setApp_version(){
        //http://android.stackexchange.com/questions/2016/how-can-you-tell-which-version-of-an-app-is-on-your-android-phone
        //adb shell dumpsys package com.google.android.apps.photos | grep versionName
        try {
            Process su = Runtime.getRuntime().exec("su", null, null);
            OutputStream os = su.getOutputStream();
            os.write(("dumpsys package " + Globals.packageName + "| grep versionName\n").getBytes("ASCII"));
            os.flush();
            os.write(("exit\n").getBytes("ASCII"));
            os.flush();
            su.waitFor();
            BufferedReader res = new BufferedReader(new InputStreamReader(su.getInputStream()));
            String line;
            while ((line = res.readLine()) != null) {
                String[] parts = line.split("=");
                app_version = parts[1];
            }
        } catch (Exception e){}

        return "error occurred";
    }

    /**
     * Retrieves the sensor data and assigns it to the appropriate key for the JSON object
     * @return
     */
    public void setSensorData(){
        for (Sensor s :  BugReport.getInstance().getSensorData().keySet()) {

            SensorDataList data =  BugReport.getInstance().getSensorData().get(s);
            long timeStart = data.getTime(0);


            if (s.getType() == Sensor.TYPE_ACCELEROMETER){
                for (int i = 0; i < MAX_ITEMS_TO_PRINT && i < data.numItems(); i++) {
                    //Log.v("ACCELEROMETER", "Time: " + "Data: " + BugReport.getInstance().makeSensorDataReadable(data.getValues(i)));
                    Accelerometer accelerometer = new Accelerometer();
                    accelerometer.time = data.getTime(i) - timeStart;
                    accelerometer.x = data.getValues(i)[0];
                    accelerometer.y = data.getValues(i)[1];
                    accelerometer.z = data.getValues(i)[2];
                    JsonModel.getInstance().accelerometerStream.add(accelerometer);
                }

            }

            else if (s.getType() == Sensor.TYPE_GYROSCOPE){
                for (int i = 0; i < MAX_ITEMS_TO_PRINT && i < data.numItems(); i++) {
                    //Log.v("GYROSCOPE", "Time: " + "Data: " + BugReport.getInstance().makeSensorDataReadable(data.getValues(i)));
                    Gyroscope gyroscope = new Gyroscope();
                    gyroscope.time = data.getTime(i) - timeStart;
                    gyroscope.x = data.getValues(i)[0];
                    gyroscope.y = data.getValues(i)[1];
                    gyroscope.z = data.getValues(i)[2];
                    JsonModel.getInstance().gyroscopeStream.add(gyroscope);
                }
            }
        }
        return;
    }

    /**
     * Retrieves the name of the application that is being tested
     * @return
     */
    public void setApp_name(){
        app_name = Globals.packageName;
    }

    /**
     * Finds the build model of the device being tested on
     * @return
     */
    public void setDevice_type(){
        device_type = android.os.Build.MODEL;
    }

    /**
     * Finds the android OS version
     * @return
     */
    public void setOs_version(){
        os_version = android.os.Build.VERSION.SDK_INT;
    }

    /**
     * Sets the name of the user-inputted name
     * @return
     */
    public void setName(){
        name = BugReport.getInstance().getReporterName();
    }

    /**
     * Sets the user inputted desired outcome field
     * @return
     */
    public void setDescription_desired_outcome(){
        description_desired_outcome = BugReport.getInstance().getDesiredOutcome();
    }

    /**
     * Sets the user inputted desired title field
     * @return
     */
    public void setTitle(){
        title = BugReport.getInstance().getTitle();
    }

    /**
     * Sets the user inputted actual outcome field
     * @return
     */
    public void setDescription_actual_outcome(){
        description_actual_outcome = BugReport.getInstance().getActualOutcome();
    }

    /**
     * Sets the report start time (will always be 0)
     * @return double
     */
    public double getReport_start_time(){
        return 0;
    }

    /**
     * Sets the report end time (start time of the last event of eventList + its duration)
     * @return double
     */
    public double getReport_end_time(){
        int last_item = BugReport.getInstance().getEventList().size() - 1;
        return BugReport.getInstance().getEventList().get(last_item).getStartTime() + BugReport.getInstance().getEventList().get(last_item).getDuration();

    }

    /**
     * Sets the events and their related fields
     * @return List<Event>
     */
    public List<Event> setEvents() throws Exception {
        for(int i = 0; i < BugReport.getInstance().getEventList().size(); i++){
            Event temp = new Event();
            //byteArray http://stackoverflow.com/questions/4989182/converting-java-bitmap-to-byte-array
            try{
                Bitmap bmp = BugReport.getInstance().getEventList().get(i).getScreenshot().getBitmap();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                temp.screenshot = new String(byteArray, "iso-8859-1");
            } catch (UnsupportedEncodingException e) {
                Log.e("JsonModel", "Error encoding byte array");
            };

            temp.event_start_time = BugReport.getInstance().getEventList().get(i).getStartTime();
            temp.event_end_time = BugReport.getInstance().getEventList().get(i).getStartTime() + BugReport.getInstance().getEventList().get(i).getDuration();
            temp.inputList = BugReport.getInstance().getEventList().get(i).getInputEvents();
            temp.description = BugReport.getInstance().getEventList().get(i).getEventDescription();
            String hierarchy_filename = BugReport.getInstance().getEventList().get(i).getHierarchy().getFilename();
            //http://www.java2s.com/Code/Java/File-Input-Output/ConvertInputStreamtoString.htm
            temp.hierarchy = getStringFromFile(hierarchy_filename);
            JsonModel.getInstance().eventList.add(temp);
        }
        return JsonModel.getInstance().eventList;
    }

    /**
     * Converts an InputStream to a String
     * @return String
     */
    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    /**
     * Gets the contents of a file as a String
     * @return String
     */
    public static String getStringFromFile (String filePath) throws Exception {
        try{
            File fl = new File(filePath);
            FileInputStream fin = new FileInputStream(fl);
            String ret = convertStreamToString(fin);
            //Make sure you close all streams.
            fin.close();
            return ret;
        }
        catch(IOException ioe){
            ioe.printStackTrace();
        }
        return "Error";
    }

    /**
     * Calls the build device function and gets a JSON representation
     * @return
     */
    public void tester() throws Exception {
        build_device();
        JsonModel.getInstance().JavatoJson();
    }

    /**
     * Uses GSON to turn a java object into a JSON object
     * @return String of JSON Object
     */
    public String JavatoJson(){
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.setPrettyPrinting().create();
        System.out.println(gson.toJson(JsonModel.getInstance()));
        return gson.toJson(JsonModel.getInstance());

    }
}

/**
 * Various fields related to an Event in the application
 */
class Event {
    String screenshot;
    double event_start_time;
    double event_end_time;
    List<GetEvent> inputList;
    String description;
    String hierarchy;
    //String Orientation;
}

/**
 * Various fields for the accelerometer stream key value in the JSON object
 */
class Accelerometer {
    double time;
    double x;
    double y;
    double z;
}

/**
 * Various fields for the Gyroscope stream key value in the JSON object
 */
class Gyroscope {
    double time;
    double x;
    double y;
    double z;
}
/**
 * Used to send an async HTTP put request to the designated server
 */
//http://stackoverflow.com/questions/7860538/android-http-post-asynctask
class AsyncHttpPut extends AsyncTask<String, String, String> {
    interface Listener {
        void onResult(String result);
    }
    private Listener mListener;
    private String mData;
    /**
     * constructor
     */
    public AsyncHttpPut(String data) {
        mData = data;
    }
    public void setListener(Listener listener) {
        mListener = listener;
    }

    /**
     * background
     */
    @Override
    protected String doInBackground(String... params) {
        URL url = null;
        try {
            url = new URL(params[0]);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("PUT");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream());
            osw.write(params[1]);
            osw.flush();
            osw.close();
            Log.v("Json submission", "gets here21: ");
            System.err.println(connection.getResponseCode());
            System.err.println(params[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "1";
    }

    /**
     * on getting result
     */
    @Override
    protected void onPostExecute(String result) {
        // something...
        if (mListener != null) {
            mListener.onResult(result);
        }
    }
}
