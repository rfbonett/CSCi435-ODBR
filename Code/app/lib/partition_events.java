import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    ArrayList<String> coords = new ArrayList<String>();
    File input_file = new File(args[1]);
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
        float time = Float.parseFloat(matcher.group(0));
        int device = Integer.parseInt(matcher.group(1));
        //String device = Integer.toString(device_base10, 16);
        int type = Integer.parseInt(matcher.group(2));
        //String type = Integer.toString(type_base10, 16);
        int code = Integer.parseInt(matcher.group(3));
        //String code = Integer.toString(code_base10, 16);
        int value = Integer.parseInt(matcher.group(4));
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
        } 

      } 
    }
    catch (Exception e){
        System.err.println(e.getMessage()); // handle exception
    }
  }
} 
/*pattern = re.compile(
        r'\[\s*(\d*\.\d*)\] /dev/input/event(\d): ' \
        r'([0-9a-f]{4}) ([0-9a-f]{4}) ([0-9a-f]{8})')

for line in input_file:
    if line[0] != '[':
        continue
    time, device, type, code, value = re.match(pattern, line).groups()
    time = float(time)
    device = int(device)
    type = int(type, 16)
    code = int(code, 16)
    value = int(value, 16)
    event = device, type, code, value
    events.append(event)
    
    # Button down.
    if type == EV_KEY:

        # If the touch screen has been toggled, let sync events
        # handle the logic.
        */
