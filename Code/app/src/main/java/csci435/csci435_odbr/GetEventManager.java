package csci435.csci435_odbr;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.Process;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Rich on 4/25/16.
 * Manages the GetEvent tasks. Interacts with the GetEventDeviceInfo to know which areas to cat to based on the
 * device array list (/dev/input/eventX). The manager toggles them on whenever we want to record inputs from the
 * record floating widget, and toggles them off whenever the overlay reappears. It is also responsible for triggering
 * the start of the screenshot and hierarchy execution
 */
public class GetEventManager {

    private boolean recording;
    private ExecutorService service;
    private ArrayList<Process> processes;
    private ScreenshotManager sm;
    private HierarchyDumpManager hdm;
    private

    int EV_SYN = 0;
    int EV_KEY = 1;
    int EV_ABS = 3;

    int SYN_REPORT = 0;
    int TOUCH_DOWN = 1;
    int TOUCH_UP = 0;

    public GetEventManager() {
        recording = false;
        service = Executors.newCachedThreadPool();
        processes = new ArrayList<Process>();
        sm = new ScreenshotManager();
        hdm = new HierarchyDumpManager();
    }

    /**
     * Method that starts the recording process and initalizes the screenshot and hierarchy managers
     */
    public void startRecording() {
        if (recording) {
            return;
        }
        sm.initialize();
        hdm.initialize();
        try {
            for (String device : getInputDevices()) {
                service.submit(new GetEventTask(device));
            }
            recording = true;
        } catch (Exception e) {
            Log.v("GetEventManager", "Error starting GetEvent process: " + e.getMessage());
        }
    }

    /**
     * Iterates through the recording processes and stops them, also destroys the hierarchy and screenshot managers
     */
    public void stopRecording() {
        try {
            recording = false;
            for (Process p : processes) {
                p.getInputStream().close();
                p.destroy();
            }
        } catch (Exception e) {
            Log.v("GetEventManager", "Error stopping GetEvent process: " + e.getMessage());
        }
        sm.destroy();
        hdm.destroy();
    }

    private ArrayList<String> getInputDevices(){
        return GetEventDeviceInfo.getInstance().getInputDevices();
    }


    /**
     * A GetEventTask is a runnable that is responsible for creating multiple report events based on the output
     * of the cat Process. Within our implementation we account for multiple fingers to be touching if the device
     * is a multitouch device. The specific task is responsible for executing its process for cat on the specific device
     * and then that process is added to the getEvent manager
     */
    class GetEventTask implements Runnable {
        private byte[] res = new byte[16];
        private InputStream is;
        private String device;
        private ReportEvent event;
        private int id_num;
        private int downCount;

        public GetEventTask(String device) {
            this.device = device;
            downCount = 0;
            try {
                //starts cat for /dev/input/eventX
                Process su = Runtime.getRuntime().exec("su", null, null);
                OutputStream outputStream = su.getOutputStream();
                outputStream.write(("cat " + device).getBytes("ASCII"));
                outputStream.flush();
                outputStream.close();
                is = su.getInputStream();
                processes.add(su);
            } catch (Exception e) {
                Log.v("GetEventTask", "Error: " + e.getMessage());
            }
        }

        @Override
        public void run() {
            try {
                //runnable that creates new report events based on the output of the cat process
                event = new ReportEvent(device);
                while (is.read(res) > 0) {
                    //increments the time so the live data feed acts as an update, not just the start of the event
                    //otherwise events of longer than 3 seconds would pose a problem
                    Globals.time_last_event = System.currentTimeMillis();

                    //a getevent consists of the entire, reformatted, that the cat process grabs, all of these are
                    //associated with a specific ReportEvent based on the parameters below.
                    GetEvent getevent = new GetEvent(res);
                    event.addGetEvent(getevent);

                    //tracks the number of fingers down and up, if 0 are down and we get a fingerDown that's the start
                    //of one report event, it finishes once we have a fingerUp that then has 0 fingers on the screen
                    if (fingerDown(getevent)) {
                        if (downCount == 0) {
                            event.addScreenshot(sm.takeScreenshot());
                            try {
                                event.addHierarchyDump(hdm.takeHierarchyDump());
                            } catch (Exception e) {Log.e("GetEventManger", e.getMessage());}
                        }
                        ++downCount;
                    }
                    else if (fingerUp(getevent)) {
                        --downCount;
                        if (downCount == 0) {
                            do {
                                is.read(res);
                                getevent = new GetEvent(res);
                                event.addGetEvent(getevent);
                            } while (getevent.getCode() != SYN_REPORT);
                            BugReport.getInstance().addEvent(event);
                            event = new ReportEvent(device);
                        }
                    }
                }
            } catch (Exception e) {
                Log.v("GetEventTask", "Whoops! " + e.getMessage());
            }
        }

        /**
         * methods to determine if a getevent line is registering finger down/up based on the device type
         * @param e: getEvent line for interpretation
         * @return: true/false
         */

        private boolean fingerDown(GetEvent e) {

            if(GetEventDeviceInfo.getInstance().isMultiTouchA() || GetEventDeviceInfo.getInstance().isMultiTouchB()){
                Integer code = GetEventDeviceInfo.getInstance().get_code("ABS_MT_TRACKING_ID");
                if(code == null){
                    return false;
                }
                else {
                    return e.getCode() == code && e.getValue() != 0xffffffff;
                }
            }
            else {
                if (e.getType() == EV_KEY && e.getCode() == GetEventDeviceInfo.getInstance().get_code("BTN_TOUCH") && e.getValue() == TOUCH_DOWN) {
                    return true;
                }
            }
            return false;
        }

        private boolean fingerUp(GetEvent e) {
            if(GetEventDeviceInfo.getInstance().isMultiTouchA() || GetEventDeviceInfo.getInstance().isMultiTouchB()){
                Integer code = GetEventDeviceInfo.getInstance().get_code("ABS_MT_TRACKING_ID");
                if(code == null){
                    return false;
                }
                else {
                    return e.getCode() == code && e.getValue() == 0xffffffff;
                }
            }
            else {
                if (e.getType() == EV_KEY && e.getCode() == GetEventDeviceInfo.getInstance().get_code("BTN_TOUCH") && e.getValue() == TOUCH_UP) {
                    return true;
                }
            }
            return false;
        }

    }
}


/**
 * Class to convert the byte information of the cat to a replica of the getevent outputs. With this, we can then
 * parse get event lines in the same way that we initially parse get event logs.
 */
class GetEvent {
    private int seconds;
    private int microseconds;
    private short type;
    private short code;
    private int value;
    public byte[] test;

    public GetEvent(byte[] vals) {
        seconds = toInt(vals[0]) + (toInt(vals[1]) << 8) + (toInt(vals[2]) << 16) + (toInt(vals[3]) << 24);
        microseconds = toInt(vals[4]) + (toInt(vals[5]) << 8) + (toInt(vals[6]) << 16) + (toInt(vals[7]) << 24);
        type = (short) (toInt(vals[8]) + (toInt(vals[9]) << 8));
        code = (short) (toInt(vals[10]) + (toInt(vals[11]) << 8));
        value = toInt(vals[12]) + (toInt(vals[13]) << 8) + (toInt(vals[14]) << 16) + (toInt(vals[15]) << 24);
        test = vals;
    }

    private int toInt(byte b) {
        return b & 0x000000FF;
    }

    @Override
    public String toString() {
        return String.format("[%d.%06d] %04x %04x %08x", seconds, microseconds, type, code, value);
    }

    public String toString(String device) {
        return String.format("[%d.%06d] %s: %04x %04x %08x", seconds, microseconds, device, type, code, value);
    }

    public String readable(String device) {
        return String.format("[%d.%06d] %s: %d %d %d", seconds, microseconds, device, type, code, value);
    }

    public String getSendEvent(String device) {
        return String.format("sendevent %s %d %d %d", device, type, code, value);
    }


    public short getType() {
        return type;
    }

    public short getCode() {
        return code;
    }

    public int getValue() {
        return value;
    }

    public long getTimeMillis() {
        long millis = (seconds & 0x000FFFFF) * 1000;
        millis += microseconds / 1000;
        return millis;
    }
}