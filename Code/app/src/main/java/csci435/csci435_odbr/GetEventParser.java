package csci435.csci435_odbr;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;


public class GetEventParser {

    public static void parse(File input_file) {
        int EV_SYN = 0;
        int EV_KEY = 1;
        int EV_ABS = 3;
        int ABS_X = 0;
        int ABS_Y = 1;
        int ABS_MT_POSITION_X = 53;
        int ABS_MT_POSITION_Y = 54;
        int ABS_MT_TRACKING_ID = 58;
        int SYN_REPORT = 0;
        int BTN_TOUCH = 330;
        float time;
        float start_time = 0;
        float duration;
        double LONG_CLICK_DURATION = 0.5;
        int CLICK_RING = 20;
        int x = 0;
        int y = 0;
        int was_finger_down = 0;
        boolean finger_down = false;
        boolean up_occurred = false;
        ArrayList<int[]> coords = new ArrayList<int[]>();
        int [] initial_location;
        int [] last_location;
        double distance;
        int type;
        int code;
        int value;
        int event_type = 0;
        String event_label;
        int i = 0;
        String s = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(input_file));
            while (br.ready()) {
                i++;
                Log.v("Numloops", i + "");
                s = br.readLine();
                if (s.charAt(0) != '[') {
                    continue;
                }
                String[] values = s.split(" ");
                //partition the line right here, use the spaces as a delimiter
                //values should now contain the time, device, type, code, and value
                //values[0] = [time]
                //values[1] = device
                //values[2] = type
                //values[3] = code
                //values[4] = value

                //we're only going to keep the last 9999 seconds, so we're going to just take care of
                //those digits

                int time_start_index;

                if(values[0].indexOf('.') != -1 && values[0].indexOf('.') >= 6){
                    time_start_index = values[0].indexOf('.') - 4;
                }
                else{
                    time_start_index = 1;
                }

                time = Float.parseFloat(values[0].substring(time_start_index, values[0].length() - 1));

                type = Integer.parseInt(values[2], 16);
                code = Integer.parseInt(values[3], 16);
                value = Integer.parseInt(values[4], 16);
                Log.v("Value", "time: " + time);
                Log.v("Value", "type: " + type);
                Log.v("Value", "code: " + code);
                Log.v("Value", "value: " + value);


                if(type == EV_KEY){
                    //some key has been made
                    if(code == BTN_TOUCH){
                        if(value == 1){
                            //button down
                            finger_down = true;
                            start_time = time;
                        }
                        else if(value == 0){
                            //button up
                            up_occurred = true;
                        }
                    }
                    else if(value == 0){
                        //some key that wasn't button but value is up
                    }
                    else if(value == 1){
                        //some key that wasn't button but value is down
                    }
                }
                else if(type == EV_ABS){
                    if (code == ABS_X || code == ABS_MT_POSITION_X) {
                        x = value;
                        if(up_occurred){
                            finger_down = false;
                        }
                    } else if (code == ABS_Y || code == ABS_MT_POSITION_Y) {
                        y = value;
                    }
                }
                else if(type == EV_SYN && code == SYN_REPORT){
                    if(finger_down){
                        int[] coord = {x, y};
                        coords.add(coord);
                    }
                    else{
                        if(up_occurred){
                            int[] coord = {x, y};
                            coords.add(coord);
                            up_occurred = false;
                        }

                        duration = time - start_time;
                        initial_location = coords.get(0);
                        last_location = coords.get(coords.size() - 1);
                        event_label = "";

                        distance = Math.sqrt(Math.pow((initial_location[0] - last_location[0]), 2) + Math.pow(initial_location[1] - last_location[1], 2));

                        if (duration >= LONG_CLICK_DURATION) {
                            event_label = "LONG_CLICK";
                            event_type = 1;
                            if (distance > CLICK_RING) {
                                event_label = "SWIPE";
                                event_type = 2;
                            }
                        } else {
                            event_label = "CLICK";
                            if (distance > CLICK_RING) {
                                event_label = "SWIPE";
                                event_type = 2;
                            }
                        }

                        //Initialize a getEvent, add its attributes, then send to bugreport
                        //Convert time to millis to match with System.currentTimeMillis();
                        GetEvent get_event = new GetEvent();
                        get_event.setDuration(Float.valueOf(duration).longValue());
                        get_event.setLabel(event_label);
                        get_event.setType(event_type);
                        get_event.setDistance(distance);
                        get_event.setStart(Float.valueOf(start_time * 1000).longValue());

                        for(i = 0; i < coords.size(); i++){
                            get_event.addCoords(coords.get(i));
                        }

                        BugReport.getInstance().addGetEvent(get_event);
                        //Log.v("getEvent", event_type + "#" + event_label + "#" + distance + "#" + duration + "#" + "x: " + initial_location[0] + " y: " + initial_location[1] + "#" + "x: " + last_location[0] + " y: " + last_location[1]);
                        coords.clear();
                    }
                }
            }
            } catch(Exception e){}
        BugReport.getInstance().matchGetEventsToReportEvents();
        BugReport.getInstance().printGetEvents();
    }
}

class GetEvent{

    ArrayList<int[]>coords = new ArrayList<int[]>();
    int event_type;
    String event_label;
    double distance;
    long start_time;
    long duration;

    public void setType(int num){
        event_type = num;
    }
    public void setLabel(String s){
        event_label = s;
    }
    public void setDistance(double d){
        distance = d;
    }
    public void setDuration(long t){
        duration = t;
    }
    public void setStart(long t){start_time = t;}
    public void addCoords(int[] coord){
        coords.add(coord);
    }
    public int get_type(){
        return event_type;
    }
    public String get_label(){
        return event_label;
    }
    public double get_distance(){
        return distance;
    }
    public long getDuration(){
        return duration;
    }
    public long getStart(){return start_time;}
    public ArrayList<int[]> get_coords(){
        return coords;
    }
    public void printValues() {
        String inputs = "";
        for (int[] coord : coords) {
            inputs += " " + coord[0] + "|" + coord[1];
        }
        Log.v("GetEvent", "Time: " + start_time + " | Coords: " + inputs);
    }

}