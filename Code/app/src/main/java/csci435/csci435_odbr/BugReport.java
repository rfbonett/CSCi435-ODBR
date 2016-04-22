package csci435.csci435_odbr;

import java.lang.reflect.Array;
import java.util.AbstractQueue;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import org.json.JSONObject;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.provider.Settings;
import android.view.accessibility.AccessibilityEvent;
import android.graphics.Bitmap;
import android.util.SparseArray;
import android.util.Log;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.Rect;
import android.view.accessibility.AccessibilityNodeInfo;


/**
 * Created by Rich on 2/11/16.
 */
public class BugReport {
    private static int colors[] = {Color.BLUE, Color.GREEN, Color.RED, Color.CYAN, Color.YELLOW, Color.MAGENTA};
    private static int MAX_ITEMS_TO_PRINT = 10;

    private HashMap<Sensor, SensorDataList> sensorData = new HashMap<Sensor, SensorDataList>();
    private HashMap<Sensor, Bitmap> sensorGraphs = new HashMap<Sensor, Bitmap>();
    private ArrayList<Sensor> sensorList = new ArrayList<Sensor>();
    private ArrayList<GetEvent> getEventList = new ArrayList<GetEvent>();
    private List<Events> eventList = new ArrayList<Events>();
    private String title = "";
    private String reporterName = "";
    private String desiredOutcome = "";
    private String actualOutcome = "";
    private int getEventIndex = 0;

    private static BugReport ourInstance = new BugReport();

    public static BugReport getInstance() {
        return ourInstance;
    }

    private BugReport() {}

    public void clearReport() {
        sensorData.clear();
        sensorGraphs.clear();
        sensorList.clear();
        eventList.clear();
        getEventList.clear();
        title = "";
        reporterName = "";
        desiredOutcome = "";
        actualOutcome = "";
        getEventIndex = 0;
    }

    public void refineEventList(){

        Log.v("deleting", Globals.packageName);
        for(int i = eventList.size() - 1; i >= 0; i--){
            //check to see if the package name matches the one for the app we are recording
            if(!eventList.get(i).getPackageName().equals(Globals.packageName)){
                //delete the event from the list
                Log.v("deleting", eventList.get(i).getPackageName());
                eventList.remove(i);
            }
        }
    }

    public void addGetEvent(GetEvent get_event){
        //throw this into a getEventQueue
        getEventList.add(get_event);
        getEventIndex++;
    }

    public void matchEvents(){

        //iterate over the eventList
        boolean condition = true;
        GetEvent get_event_to_add = new GetEvent();
        int j = 0;
        for(int i = 0; i < eventList.size(); i++) {
            while(condition){
                if (j < getEventList.size()) {
                    GetEvent get_event = getEventList.get(j);
                    Float time_dif_get_event = get_event.get_start() - Globals.GetEventStart;
                    Float time_dif_accessibility = (eventList.get(i).getTimeStamp() - Globals.AccessibilityStart) / 1000f;

                    if (time_dif_get_event >= time_dif_accessibility) {
                        Log.v("GetEventQueue", "Reassigns getEvent");
                        get_event_to_add = get_event;
                        condition = false;
                    }
                    else{
                        Log.v("GetEventQueue", "GE "+ time_dif_get_event + " : A " + time_dif_accessibility);
                    }
                }
                else{
                    Log.v("GetEventQueue", "We hit null");
                    condition = false;
                }
                j++;
            }
            eventList.get(i).addGetEvent(get_event_to_add);
            condition = true;
        }
    }

    public int numGetEvents(){
        return getEventIndex;
    }

    public void addUserEvent(AccessibilityEvent e) {
        eventList.add(new Events(e));
}

    public int getNumEvents(){
        return eventList.size();
    }

    public void addSensorData(Sensor s, SensorEvent e) {
        if (!sensorList.contains(s)) {
            sensorList.add(s);
            sensorData.put(s, new SensorDataList());
        }
        sensorData.get(s).addData(e.timestamp, e.values.clone());
    }

    public void setDesiredOutcome(String s) { desiredOutcome = s;}

    public void setActualOutcome(String s) {actualOutcome = s;}

    public void setTitle(String s) {title = s;}

    public void setReporterName(String s) {reporterName = s;}

    /**
     * Returns its data as a formatted JSON file; currently outputs data to LogCat
     * @return
     */
    public JSONObject toJSON() {

        //Log Title, Reporter Name and Description
        Log.v("BugReport", "Reporter: " + reporterName);
        Log.v("BugReport", "Title: " + title);
        Log.v("BugReport", "What Should Happen: " + desiredOutcome);
        Log.v("BugReport", "What Does Happen: " + actualOutcome);

        //Log Sensor Data, each sensor capped at MAX_ITEMS_TO_PRINT
        for (Sensor s : sensorData.keySet()) {
            Log.v("BugReport", "|*************************************************|");
            Log.v("BugReport", "Data for Sensor: " + s.getName());
            SensorDataList data = sensorData.get(s);
            long timeStart = data.getTime(0);
            for (int i = 0; i < MAX_ITEMS_TO_PRINT && i < data.numItems(); i++) {
                Log.v("BugReport", "Time: " + (data.getTime(i) - timeStart) + "| " + "Data: " + makeSensorDataReadable(data.getValues(i)));
            }
            int printed = data.numItems() - MAX_ITEMS_TO_PRINT;
            Log.v("BugReport", "And " + (printed > 0 ? printed : 0) + " more");
            Log.v("BugReport", "|*************************************************|");
        }

        //Log UserEvent data
        for(int i = 0; i < eventList.size(); i++){
            Log.v("Event Number:", "" + i);
            eventList.get(i).printData();
        }
        return new JSONObject();
    }

    private String makeSensorDataReadable(float[] input) {
        String s = "";
        for (float f : input) {
            s +=  f + " | ";
        }
        return s;
    }


    public Bitmap drawSensorData(Sensor s) {
        if (sensorGraphs.containsKey(s)) {
            return sensorGraphs.get(s);
        }

        Globals.height = Globals.height / 2;
        Bitmap b = Bitmap.createBitmap(Globals.width, Globals.height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);

        SensorDataList data = sensorData.get(s);
        Paint color = new Paint();
        color.setColor(Color.BLACK);
        color.setStrokeWidth(5);
        c.drawARGB(255, 200, 200, 200);
        c.drawLine(0, 0, 0, Globals.height, color);
        c.drawLine(0, Globals.height / 2, Globals.width, Globals.height / 2, color);
        color.setStrokeWidth(3);

        long timeMod = data.getElapsedTime(data.numItems() - 1) / Globals.width;
        timeMod = timeMod > 0 ? timeMod : 1;
        for (int k = 0; k < data.sizeOfValueArray() && k < colors.length; k++) {
            float valueMod = data.meanValue(k) / (Globals.height / 2);
            valueMod = valueMod > 0 ? valueMod : 1;
            color.setColor(colors[k]);
            float startX = data.getElapsedTime(0) / timeMod;
            float startY = data.getValues(0)[k] / valueMod;
            for (int i = 1; i < data.numItems(); i++) {
                float endX = data.getElapsedTime(i) / timeMod;
                float endY = data.getValues(i)[k] / valueMod;
                c.drawLine(startX, startY, endX, endY, color);
                startX = endX;
                startY = endY;
            }
        }
        sensorGraphs.put(s, b);
        Globals.height = Globals.height * 2;
        return b;
    }


    /* Getters */
    public List<Events> getUserEvents() {
        return eventList;
    }
    public Events getEventAtIndex(int ndx) {
        return eventList.get(ndx);
    }

    public String getReporterName() {
        return reporterName;
    }
    public String getTitle() {
        return title;
    }
    public String getDesiredOutcome(){
        return desiredOutcome;
    }
    public String getActualOutcome(){
        return actualOutcome;
    }
    public int numSensors() {return sensorData.keySet().size();}
    public Sensor getSensor(int pos) {return sensorList.get(pos);}

}




class Events {
    private long timeStamp;
    private int eventType;
    private CharSequence packageName;
    private CharSequence className;
    private CharSequence contentDescription;
    private CharSequence text;
    private int screenshotIndex;
    private GetEvent getEvent;

    public Events(AccessibilityEvent e){
        packageName = e.getPackageName();
        eventType = e.getEventType();
        timeStamp = e.getEventTime();
        //className = e.getSource().getClassName();
        //contentDescription = e.getSource().getContentDescription();
        //text = e.getSource().getText();
        screenshotIndex = Globals.screenshot_index;
    }
    public String getEventType(){
        if(eventType == 2){
            return "view long clicked";
        }
        else{
            return "view clicked";
        }
    }
    public String getFilename(){
        return "screenshot" + screenshotIndex + ".png";
    }
    public String getPackageName(){
        return packageName + "";
    }
    public String getViewDescription() {
        char stopChar = '.';
        int start = className.length() - 1;
        while (start > 0 && !(stopChar == className.charAt(start))) {
            start--;
        }
        return (String) className.subSequence(start + 1, className.length()) + " " + contentDescription;
    }

    public long getTimeStamp(){
        return timeStamp;
    }

    public void printData(){
        Log.v("Event: time", "" + timeStamp);
        Log.v("Event: type", "" + eventType);
        Log.v("Event: package name", "" + packageName);
        Log.v("Event: class name", "" + className);
        Log.v("Event: description", "" + contentDescription);
        Log.v("Event: text", "" + text);
    }

    public void addGetEvent(GetEvent get_event) {
        //we are adding the getEvent to the desired object
        getEvent = get_event;
    }

    public GetEvent getGetEvent(){
        return getEvent;
    }
}


class SensorDataList {
    private ArrayList<Long> timestamps;
    private ArrayList<float[]> values;
    private float[] valueSums;
    private int numItems;

    public SensorDataList() {
        timestamps = new ArrayList<Long>();
        values = new ArrayList<float[]>();
        numItems = 0;
    }

    public void addData(long timestamp, float[] value) {
        ++numItems;
        timestamps.add(timestamp);
        values.add(value);
        if (numItems == 1) {
            valueSums = new float[value.length];
        }
        for (int i = 0; i < value.length; i++) {
            valueSums[i] += value[i];
        }
    }

    public long getTime(int index) {
        return timestamps.get(index);
    }

    public long getElapsedTime(int index) {
        return timestamps.get(index) - timestamps.get(0);
    }


    public float meanValue(int index) {
        return valueSums[index] / numItems;
    }

    public float stDev(int index) {
        float stdev = 0;
        float mean = meanValue(index);
        for (int i = 0; i < numItems; i++) {
            float f = values.get(i)[index];
            stdev += (f - mean) * (f - mean);
        }
        return (float) Math.sqrt(stdev / numItems);
    }

    public float[] getValues(int index) {
        return values.get(index);
    }

    public int numItems() {
        return numItems;
    }

    public int sizeOfValueArray() {
        return values.get(0).length;
    }
}