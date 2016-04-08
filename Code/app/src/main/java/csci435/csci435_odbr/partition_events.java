package csci435.csci435_odbr;

import android.util.Log;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class partition_events {

    public partition_events(){
        super();
    }


    public void parse() {
        int EV_SYN = 0;
        int EV_KEY = 1;
        int EV_ABS = 3;
        int ABS_X = 0;
        int ABS_Y = 1;
        int ABS_MT_POSITION_X = 53;
        int ABS_MT_POSITION_Y = 54;
        int ABS_MT_TRACKING_ID = 57;
        int SYN_REPORT = 0;
        int BTN_TOUCH = 330;
        Log.v("getEvent", "This occurs");
        double LONG_CLICK_DURATION = 0.5;
        int CLICK_RING = 20;
        int x = 0;
        int y = 0;
        int was_finger_down = 0;
        int finger_down = 0;
        ArrayList<String> events = new ArrayList<String>();
        ArrayList<int[]> coords = new ArrayList<int[]>();
        File input_file = new File("sdcard/events.txt");
        //regex pattern
        String regex_pattern = "/[/s*(/d*/./d*)/] /dev/input/event(/d):  ([0-9a-f]{4}) ([0-9a-f]{4}) ([0-9a-f]{8})";
        //start the for loop
        try {
            BufferedReader br = new BufferedReader(new FileReader(input_file));
            String line;
            int [] coord = {};
            while ((line = br.readLine()) != null) {
                // process the line.
                if (line.charAt(0) != '[') {
                    continue;
                }
                //insert regex stuff here

                Log.v("getEvent", line.substring(line.length() - 20, line.length() - 12));

                /*
                //Matcher matcher = Pattern.compile(regex_pattern).matcher(line);
                float time = Float.parseFloat(line.substring(1, 18));
                //int device_base10 = Integer.parseInt(line.substring(line.length() - 37, line.length() - 19), 16);
                //Log.v("getEvent", String.valueOf(device_base10));
                int type_base10 = Integer.parseInt(line.substring(line.length()-18, line.length()-14), 16);
                Log.v("getEvent", String.valueOf(type_base10));
                int code_base10 = Integer.parseInt(line.substring(line.length() - 13, line.length() - 9), 16);
                Log.v("getEvent", String.valueOf(code_base10));
                int value_base10 = Integer.parseInt(line.substring(line.length() - 8, line.length()), 16);
                Log.v("getEvent", String.valueOf(value_base10));
                //int device_base10 = Integer.parseInt(matcher.group(1), 16);
               // String device = Integer.toString(device_base10, 16);
                //int type_base10 = Integer.parseInt(matcher.group(2), 16);
                String type = Integer.toString(type_base10, 16);
                //int code_base10 = Integer.parseInt(matcher.group(3), 16);
                String code = Integer.toString(code_base10, 16);
                //int value_base10 = Integer.parseInt(matcher.group(4), 16);
                String value = Integer.toString(value_base10, 16);
                //button down
                */

                if(line.contains("DOWN")){
                    //we have a down event, read until we get up
                    x = 0;
                    y = 0;

                    while(br.ready() && !line.contains("UP")){
                        line = br.readLine();
                    }

                    if(br.ready()){
                        line = br.readLine();
                        if(br.ready()){
                            //these are the x values
                            line = br.readLine();
                            x = Integer.parseInt(line.substring(line.length() - 20, line.length() -12 ), 16);
                            if (br.ready()) {
                                //these are the y values
                                line = br.readLine();
                                y = Integer.parseInt(line.substring(line.length() - 20, line.length() - 12), 16);
                            }
                        }
                    }

                    //send of x & y coordinates to bugReport
                    BugReport.getInstance().addGetEvent(x, y);


                }
                /*
                if (type_base10 == EV_KEY) {
                    //if the touch screen has been toggled, let sync events handle the logic.
                    if (code_base10 == BTN_TOUCH) {
                        finger_down = value_base10;
                    }
                } else if (type_base10 == EV_ABS) {
                    if (ABS_X == code_base10 || code_base10 == ABS_MT_POSITION_X) {
                        x = value_base10;
                    } else if (ABS_Y == code_base10 || code_base10 == ABS_MT_POSITION_Y) {
                        y = value_base10;
                    } else if (code_base10 == ABS_MT_TRACKING_ID) {
                        if (value_base10 != 0xffffffff) {
                            finger_down = value_base10;
                        }

                    }
                }
                else if(type_base10 == EV_SYN || code_base10 == SYN_REPORT) {
                    // If the finger has changed:
                    if (finger_down != was_finger_down) {
                        float start_time = 0;
                        if (finger_down == 0) {
                            start_time = time;
                            coords.clear();
                            int[] coor = {x, y};
                            coords.add(coor);
                        } else {
                            float duration = time - start_time;
                            int[] initial_location = coords.get(0);
                            int[] last_location = coords.get(coords.size() - 1);
                            String event_label = "";
                            int event_type = 0;
                            double distance = Math.sqrt(
                            (Math.pow(initial_location[0] - last_location[0], 2))+
                            (Math.pow(initial_location[1] - last_location[0], 2)));


                            if (duration >= LONG_CLICK_DURATION) {
                                event_label = "LONG_CLICK";
                                event_type = 1;
                                if (distance > CLICK_RING) {
                                    event_label = "SWIPE";
                                    event_type = 2;
                                    //#print coords
                                } else {
                                    event_label = "CLICK";
                                    //#print coords
                                    if (distance > CLICK_RING) {
                                        event_label = "SWIPE";
                                        event_type = 2;
                                    }

                                }
                                Log.v("getEvent", "" + (event_type) + '#' + event_label + '#' + String.valueOf(distance) + '#' + String.valueOf(duration)+ '#' + String.valueOf(initial_location) + '#' + String.valueOf(last_location));
                                events.clear();
                                coords.clear();
                                was_finger_down = finger_down;
                            }
                            //# Append the current coordinates to the list.
                            else {
                                int [] coord = {x, y};
                                coords.add(coord);
                            }
                        }
                    }
                }*/
            }
        }
        catch (Exception e){
            //System.err.println(e.getMessage()); // handle exception
        }

    }
} 