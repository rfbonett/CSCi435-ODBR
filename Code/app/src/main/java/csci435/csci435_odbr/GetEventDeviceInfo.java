package csci435.csci435_odbr;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Brendan Otten on 5/8/2016.
 */
public class GetEventDeviceInfo {

    private HashMap<String, Integer> device_info_hashmap = new HashMap<String, Integer>();
    private HashMap<String, Integer> key_info_hashmap = new HashMap<String, Integer>();
    private static GetEventDeviceInfo ourInstance = new GetEventDeviceInfo();
    private ArrayList<String> devices = new ArrayList<String>();
    ArrayList<String> abs_name_list = new ArrayList<String>();
    ArrayList<String> key_name_list = new ArrayList<String>();
    private boolean typeA = false;
    private boolean typeMultiA = false;
    private boolean typeMultiB = false;

    public static GetEventDeviceInfo getInstance() {
        return ourInstance;
    }
    //add key codes, this is just a class for a hashmap really
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

    public boolean isTypeA(){
        return typeA;
    }
    public boolean isMultitouchA(){return typeMultiA;}
    public boolean isMultitouchB(){return typeMultiB;}

    public void setDeviceData() {
        Log.v("GetEventDeviceInfo", "settingInfo");
        boolean adding = false;
        boolean btn_touch_found = false;
        try {
            Process su = Runtime.getRuntime().exec("su", null, null);
            OutputStream os = su.getOutputStream();
            os.write(("getevent -li\n").getBytes("ASCII"));
            os.flush();
            os.close();
            BufferedReader res = new BufferedReader(new InputStreamReader(su.getInputStream()));
            String line;
            while ((line = res.readLine()) != null) {
                String[] parts = line.split(" ");

                if(line.contains("KEY") && line.contains("(") && line.contains(")")){
                    //we have the start of a key line
                    if (line.contains("BTN_TOUCH")) {
                        for (int i = 0; i < parts.length; i++) {
                            if (parts[i].equals("BTN_TOUCH")) {
                                btn_touch_found = true;
                                Log.v("GetEventDeviceInfo", "Button touch found");
                                break;
                            }
                        }
                    }
                    for(int i = 0; i < parts.length; i++){
                        if(!parts[i].equals("") && !parts[i].equals("KEY") && !parts[i].contains("(") && !parts[i].contains(")")){
                            key_name_list.add(parts[i]);
                        }
                    }
                    line = res.readLine();
                    parts = line.split(" ");
                    while(!line.contains(":")){

                        if (line.contains("BTN_TOUCH")) {
                            for (int i = 0; i < parts.length; i++) {
                                if (parts[i].equals("BTN_TOUCH")) {
                                    btn_touch_found = true;
                                    Log.v("GetEventDeviceInfo", "Button touch found");
                                    break;
                                }
                            }
                        }

                        for(int i = 0; i < parts.length; i++ ){
                            if(!parts[i].equals("") && !parts[i].equals("KEY") && !parts[i].contains("(") && !parts[i].contains(")")){
                                key_name_list.add(parts[i]);
                                //Log.v("GetEventDeviceInfo", "Added: " + parts[i]);
                            }
                        }
                        if(res.ready()){
                            line = res.readLine();
                            parts = line.split(" ");
                        }
                        else{
                            break;
                        }
                    }

                }

                if ("add".equals(parts[0])) {
                    //transition to adding stage
                    devices.add(parts[3]);
                    Log.v("GetEventManager", "device: " + parts[3]);
                }

                if (line.contains("ABS_")) {
                    for (int i = 0; i < parts.length; i++) {
                        if (parts[i].contains("ABS_")) {
                            abs_name_list.add(parts[i]);
                            //Log.v("ABS", "Added: "+ parts[i]);
                        }
                    }
                }
            }
            su.destroy();
            //we have to read to get the ABS values
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
                //comment?
                if (btn_touch_found) {
                    //we have the start of a key line
                    if(line.contains("KEY") && line.contains("(") && line.contains(")")){
                        for(int i = 0; i < parts.length; i++){
                            if(!parts[i].equals("") && !parts[i].equals("KEY") && !parts[i].contains("(") && !parts[i].contains(")")){
                                key_info_hashmap.put(key_name_list.get(keyIndex), Integer.parseInt(parts[i], 16));
                                keyIndex++;
                            }
                        }
                        if(res.ready()){
                            line = res.readLine();
                            parts = line.split(" ");
                        }
                        else{
                            break;
                        }
                        while(!line.contains(":")) {
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
                if (line.contains("ABS")) {
                    GetEventDeviceInfo.getInstance().add_code(abs_name_list.get(absIndex), Integer.parseInt(parts[parts.length - 15], 16));
                    Log.v("ABS", abs_name_list.get(absIndex) + " : " + Integer.valueOf(parts[parts.length - 15], 16));
                    line = res.readLine();
                    absIndex++;
                    while (line.contains("resolution") && line.contains("value")) {
                        parts = line.split(" ");
                        GetEventDeviceInfo.getInstance().add_code(abs_name_list.get(absIndex), Integer.parseInt(parts[parts.length - 15], 16));
                        Log.v("ABS", abs_name_list.get(absIndex) + " : " + Integer.parseInt(parts[parts.length - 15], 16));
                        line = res.readLine();
                        absIndex++;
                    }
                }

            }
            Log.v("GetEventDeviceInfo", "Button Touch: " + key_info_hashmap.get("BTN_TOUCH"));
            device_info_hashmap.put("BTN_TOUCH", key_info_hashmap.get("BTN_TOUCH"));
            su.destroy();

            if(device_info_hashmap.get("ABS_MT_POSITION_X") != null && device_info_hashmap.get("ABS_MT_POSITION_Y") != null && device_info_hashmap.get("BTN_TOUCH") == null ){
                typeA = true;
            }
            else{
                if(device_info_hashmap.get("ABS_MT_SLOT") != null){
                    typeMultiB = true;
                }
                else{
                    typeMultiA = true;
                }
            }

        } catch (Exception e) {
            Log.v("Main", "Error getting input devices");
        }
    }
}
