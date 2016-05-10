package csci435.csci435_odbr;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Brendan Otten on 5/8/2016.
 * Class is designed to house a hashmap of every ABS and KEY code on the phone / device. The device_info_hashmap
 * is the one with accessible data, the key_info_hashmap contains all the keys, the only one being added to the
 * device_info_hashmap being BTN_TOUCH. This allows for simple extendability, if there was a list of all the keys
 * you wanted to add, or if you just wanted to add all the keys to an accessible hashmap, just reroute their put
 * statement to the device_info_hashmap.
 *
 * In addition this class also determines the device type, which can either be a singleTouch device, a multitouch device
 * (A) that does not report slots in the getevent logs, or a multitouch device (B) that does report the slots. Finally, there
 * is a list of all the device locations that report event logs
 */
public class GetEventDeviceInfo {

    private HashMap<String, Integer> device_info_hashmap = new HashMap<String, Integer>();
    private HashMap<String, Integer> key_info_hashmap = new HashMap<String, Integer>();
    private static GetEventDeviceInfo ourInstance = new GetEventDeviceInfo();
    private ArrayList<String> devices = new ArrayList<String>();
    ArrayList<String> abs_name_list = new ArrayList<String>();
    ArrayList<String> key_name_list = new ArrayList<String>();
    private boolean typeSingleTouch = false;
    private boolean typeMultiA = false;
    private boolean typeMultiB = false;
    private int maxX;
    private int maxY;
    private String device_to_add;
    private boolean isConfigured = false;

    public static GetEventDeviceInfo getInstance() {
        return ourInstance;
    }
    public void add_code(String key, Integer value){
        device_info_hashmap.put(key, value);
    }
    public Integer get_code(String key){
        return device_info_hashmap.get(key);
    }
    public HashMap<String, Integer> getMap(){
        return device_info_hashmap;
    }
    public ArrayList<String> getInputDevices(){
        return devices;
    }

    /**
     * methods to determine device type
     * @return true or false based on type
     */
    public boolean isTypeSingleTouch(){
        return typeSingleTouch;
    }
    public boolean isMultiTouchA(){return typeMultiA;}
    public boolean isMultiTouchB(){return typeMultiB;}

    public int getMaxX() {
        return maxX;
    }

    public int getMaxY() {
        return maxY;
    }

    /**
     * Parser that runs through "adb shell getevent -li" and then "adb shell getevent -i" to match strings of ABS and KEY
     * to their respective codes. The amount of text changes per line based on -li or -i instance, so it does not map line
     * to line, rather, when we find a string that satisfies requirements as being a string of a code, we add it to a list
     * then we use a 'counter' to denote which codes to match to these strings using the same parameters as before
     */
    public void setDeviceData() {
        if(!isConfigured) {
            device_info_hashmap = new HashMap<String, Integer>();
            key_info_hashmap = new HashMap<String, Integer>();
            devices = new ArrayList<String>();
            abs_name_list = new ArrayList<String>();
            key_name_list = new ArrayList<String>();

            boolean btn_touch_found = false;
            try {
                //start the process
                Process su = Runtime.getRuntime().exec("su", null, null);
                OutputStream os = su.getOutputStream();
                os.write(("getevent -li\n").getBytes("ASCII"));
                os.flush();
                os.close();
                BufferedReader res = new BufferedReader(new InputStreamReader(su.getInputStream()));
                String line;

                //read until we hit end of output
                while ((line = res.readLine()) != null) {
                    String[] parts = line.split(" ");

                    //adds the devices which are on a line like: add device X: /dev/input/eventX
                    parseAdd(parts);

                    //line is of the format KEY (XXXX) where XXXX is the value for EV_KEY
                    if (line.contains("KEY") && line.contains("(") && line.contains(")")) {
                        // iterate over the first line in the key's to get all the necessary strings
                        for (int i = 0; i < parts.length; i++) {
                            if (!parts[i].equals("") && !parts[i].equals("KEY") && !parts[i].contains("(") && !parts[i].contains(")")) {
                                key_name_list.add(parts[i]);
                                if (parts[i].equals("BTN_TOUCH")) {
                                    btn_touch_found = true;
                                }
                            }
                        }

                        //go to the next line and then until we find another : which denotes the end of that section of keys
                        line = res.readLine();
                        parts = line.split(" ");
                        while (!line.contains(":")) {

                            //iterate over the parts of the string to grab the proper values
                            for (int i = 0; i < parts.length; i++) {
                                if (!parts[i].equals("") && !parts[i].equals("KEY") && !parts[i].contains("(") && !parts[i].contains(")")) {
                                    key_name_list.add(parts[i]);
                                    if (parts[i].equals("BTN_TOUCH")) {
                                        btn_touch_found = true;
                                        devices.add(device_to_add);
                                    }
                                }
                            }
                            if (res.ready()) {
                                line = res.readLine();
                                parts = line.split(" ");
                            } else {
                                break;
                            }
                        }
                    }

                    //if the line has an ABS_ in it, we add that respective code
                    if (line.contains("ABS_")) {
                        parseNameAbs(parts);
                    }
                }
                su.destroy();

                //start the process again without the -l option to begin matching
                su = Runtime.getRuntime().exec("su", null, null);
                os = su.getOutputStream();
                os.write(("getevent -i\n").getBytes("ASCII"));
                os.flush();
                os.close();
                res = new BufferedReader(new InputStreamReader(su.getInputStream()));
                int absIndex = 0;
                int keyIndex = 0;
                while ((line = res.readLine()) != null) {
                    String[] parts = line.split(" ");

                    //For our implementation we only care about this part if we found btn_touch, remove the if statement
                    //if we want to add other keys
                    if (btn_touch_found) {

                        //first line of KEY (XXXX)
                        if (line.contains("KEY") && line.contains("(") && line.contains(")")) {
                            for (int i = 0; i < parts.length; i++) {
                                if (!parts[i].equals("") && !parts[i].equals("KEY") && !parts[i].contains("(") && !parts[i].contains(")")) {
                                    key_info_hashmap.put(key_name_list.get(keyIndex), Integer.parseInt(parts[i], 16));
                                    keyIndex++;
                                }
                            }
                            if (res.ready()) {
                                line = res.readLine();
                                parts = line.split(" ");
                            } else {
                                break;
                            }

                            //Read until next section denoted with a : inside it
                            while (!line.contains(":")) {
                                for (int i = 0; i < parts.length; i++) {
                                    if (!parts[i].equals("") && !parts[i].equals("KEY") && !parts[i].contains("(") && !parts[i].contains(")")) {
                                        key_info_hashmap.put(key_name_list.get(keyIndex), Integer.parseInt(parts[i], 16));
                                        keyIndex++;
                                    }
                                }
                                if (res.ready()) {
                                    line = res.readLine();
                                    parts = line.split(" ");
                                } else {
                                    break;
                                }
                            }
                        }
                    }

                    //if line contains ABS parse the codes from there and match them to their corresponding strings
                    if (line.contains("ABS")) {
                        GetEventDeviceInfo.getInstance().add_code(abs_name_list.get(absIndex), Integer.parseInt(parts[parts.length - 15], 16));
                        if ("ABS_MT_POSITION_X".equals(abs_name_list.get(absIndex))) {
                            for (int ndx = 0; ndx < parts.length; ndx++) {
                                if ("max".equals(parts[ndx])) {
                                    maxX = Integer.parseInt(parts[ndx + 1].replace(",", ""));
                                }
                            }
                        }
                        if ("ABS_MT_POSITION_Y".equals(abs_name_list.get(absIndex))) {
                            for (int ndx = 0; ndx < parts.length; ndx++) {
                                if ("max".equals(parts[ndx])) {
                                    maxY = Integer.parseInt(parts[ndx + 1].replace(",", ""));
                                }
                            }
                        }
                        line = res.readLine();
                        absIndex++;
                        while (line.contains("resolution") && line.contains("value")) {
                            parts = line.split(" ");
                            GetEventDeviceInfo.getInstance().add_code(abs_name_list.get(absIndex), Integer.parseInt(parts[parts.length - 15], 16));

                            //extracts the maximum x and maximum y values from the lines
                            if ("ABS_MT_POSITION_X".equals(abs_name_list.get(absIndex))) {
                                for (int ndx = 0; ndx < parts.length; ndx++) {
                                    if ("max".equals(parts[ndx])) {
                                        maxX = Integer.parseInt(parts[ndx + 1].replace(",", ""));
                                    }
                                }
                            }
                            if ("ABS_MT_POSITION_Y".equals(abs_name_list.get(absIndex))) {
                                for (int ndx = 0; ndx < parts.length; ndx++) {
                                    if ("max".equals(parts[ndx])) {
                                        maxY = Integer.parseInt(parts[ndx + 1].replace(",", ""));
                                    }
                                }
                            }

                            line = res.readLine();
                            absIndex++;
                        }
                    }

                }

                //adds BTN_TOUCH to hashmap if it was found
                if (btn_touch_found) {
                    device_info_hashmap.put("BTN_TOUCH", key_info_hashmap.get("BTN_TOUCH"));
                }

                su.destroy();
                set_device_type();
                for (String s : devices) {
                    Log.v("GetEventDeviceInfo", s);
                }

                isConfigured = true;

            } catch (Exception e) {
                Log.v("Main", "Error getting input devices");
            }
        }
    }

    /**
     * Method to parse through the lines written like: add device X: /dev/input/eventX
     * @param parts: list of strings comprising the line that we are reading, split by " "
     */
    private void parseAdd(String [] parts){
        if ("add".equals(parts[0])) {
            device_to_add = parts[3];
        }
    }

    /**
     * Method to parse through the parts of a line that is split by " ", called if the line contains ABS
     * @param parts: list of strings comprising the line that we are reading, split by " "
     */
    private void parseNameAbs(String [] parts){
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].contains("ABS_")) {
                abs_name_list.add(parts[i]);
                if(parts[i].equals("ABS_MT_TRACKING_ID")){
                    devices.add(device_to_add);
                }
            }
        }
    }

    /**
     * Method to set the device type:
     * typeSingleTouch: device only supports single touch events
     * typeMultiTouchA: device supports multitouch but events are parsed differently, slightly prone to error as the
     * getevent logs do not indicate the presence of which finger/pointer is being tracked at which time
     * typeMutliTouchB: device supports multitouch and events are properly reported. Multitouch events are appropriately
     * parsed.
     *
     * These values are determined based on the following guidelines:
     * --https://source.android.com/devices/input/touch-devices.html (Single vs MultiTouch)
     * --https://www.kernel.org/doc/Documentation/input/multi-touch-protocol.txt (Multitouch A || B)
     */
    private void set_device_type(){
        if(device_info_hashmap.get("ABS_MT_POSITION_X") != null && device_info_hashmap.get("ABS_MT_POSITION_Y") != null && device_info_hashmap.get("BTN_TOUCH") == null ){
            if(device_info_hashmap.get("ABS_MT_SLOT") != null){
                typeMultiB = true;
            }
            else{
                typeMultiA = true;
            }
        }
        else{
            typeSingleTouch = true;
        }
    }

}
