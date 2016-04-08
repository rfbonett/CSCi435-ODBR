import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.awt.Point;

public class partition_events {
  public static void main(String[] args) {
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

    double LONG_CLICK_DURATION = 0.5;
    int CLICK_RING = 20;
    int x = 0;
    int y = 0;
    //the "finger_down" variables are booleans, using ints 0 for false and 1 for true
    int was_finger_down = 0;
    int finger_down = 0;
    ArrayList<String> events = new ArrayList<String>();
    Point coords = new Point();
    File input_file = new File(args[1]);

    //other typecastings 
    float time;
    int device;
    int type;
    int code;
    int value;
    float start_time; 
    double duration;
    double initial_location;
    int event_type;
    int distance;
    
    //regex pattern
    String regex_pattern = "/[/s*(/d*/./d*)/] /dev/input/event(/d):  ([0-9a-f]{4}) ([0-9a-f]{4}) ([0-9a-f]{8})";
    //start the for loop
    try (BufferedReader br = new BufferedReader(new FileReader(input_file))) {
      String line;
      while ((line = br.readLine()) != null) {
        // process the line.
        if (line.charAt(0) != '['){
          continue;
        }
        //insert regex stuff here
        Matcher matcher = Pattern.compile(regex_pattern).matcher(line);  
        time = Float.parseFloat(matcher.group(0));
        device = Integer.parseInt(matcher.group(1));
        //String device = Integer.toString(device_base10, 16);
        type = Integer.parseInt(matcher.group(2));
        //String type = Integer.toString(type_base10, 16);
        code = Integer.parseInt(matcher.group(3));
        //String code = Integer.toString(code_base10, 16);
        value = Integer.parseInt(matcher.group(4));
        //String value = Integer.toString(value_base10, 16);
        //Java doesnt have tuples so...
        Object[] event_tuple = new Object[5]; 
        ArrayList<Object[]> events_list = new ArrayList<Object[]>();
        event_tuple[0] = time; 
        event_tuple[1] = device; 
        event_tuple[2] = type; 
        event_tuple[3] = code; 
        event_tuple[4] = value;
        events_list.add(event_tuple); 
        //button down
        if (type == EV_KEY){
          //if the touch screen has been toggled, let sync events handle the logic.
          if (code == BTN_TOUCH){
            finger_down = value;
          }
          //for any other button, print the action after the button has been released. 
          else if (value == 0){
            duration = time - start_time;
            System.out.println(Double.toString(duration) + ',' + Double.toString(start_time) + ',' + Double.toString(time));
          }
          else if (value == 1){
            start_time = time;
          }
        }
        else if (type == EV_ABS){
          if (code == ABS_X || code == ABS_MT_POSITION_X){
            x = value;
          }
          else if (code == ABS_Y || code == ABS_MT_POSITION_Y){
            y = value;
          }
          else if (code == ABS_MT_TRACKING_ID){
            //what is this doing
            //finger_down = value != 0xffffffff;
          }
        } 
        //Sync
        else if (type == EV_ABS && code == SYN_REPORT){
          //if the finger has changed
          if (finger_down != was_finger_down){
            //restart coordinate list
            if (finger_down == 1){
              start_time = time;
              coords.move(x, y);
            }
            //If the finger is removed from the touchscreen, end the action and print the events.
            else{
              duration = time - start_time;
              initial_location = coords.getX();
              //what is this trying to do
              //int last_location = coords[len(coords) - 1];
              String event_label = "";
              event_type = 0;
              //distance formula here
              //distance = yet to be implemented
              distance = 1; 
              if (duration >= LONG_CLICK_DURATION){
                event_label = "LONG_CLICK";
                event_type = 1;
                if (distance > CLICK_RING){
                  event_label = "SWIPE";
                  event_type = 1;
                  if (distance > CLICK_RING){
                    event_label = "SWIPE";
                    event_type = 2;
                    //print coords here
                  }
                }
              }
              else{
                event_label = "CLICK";
                //print coords
                if (distance > CLICK_RING){
                  event_label = "SWIPE";
                  event_type = 2;
                }
              }
              System.out.println(Integer.toString(event_type) + '#' + event_label + '#' + Integer.toString(distance) + '#' + Double.toString(duration) + '#' + Double.toString(initial_location) + '#' );
              events.clear();
              coords.move(0,0);
            }
            was_finger_down = finger_down;
          }
          //Append the current coordinates to the list
          else{
            //move coordinates
          } 
        }
      } 
    }
    catch (Exception e){
        System.err.println("unrecognized event: " + e.getMessage()); // handle exception
    }
  }
} 
