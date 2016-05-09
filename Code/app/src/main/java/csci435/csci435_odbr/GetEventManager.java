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


    public void stopRecording() {
        try {
            Log.v("GetEventManager", "Terminating Recording Processes");
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
            Log.v("GetEventTask", "Starting input collection for device: " + device);
            try {
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
                event = new ReportEvent(device);
                while (is.read(res) > 0) {
                    Globals.time_last_event = System.currentTimeMillis();
                    GetEvent getevent = new GetEvent(res);
                    event.addGetEvent(getevent);
                    Log.v("GetEvent", getevent.readable(device));
                    if (fingerDown(getevent)) {
                        if (downCount == 0) {
                            event.addScreenshot(sm.takeScreenshot());
                            event.addHierarchyDump(hdm.takeHierarchyDump());
                        }
                        ++downCount;
                        Log.v("GetEventTouch", "FingerDown");
                    }
                    else if (fingerUp(getevent)) {
                        --downCount;
                        Log.v("GetEventTouch", "FingerUp");
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

class GetEvent {
    private int seconds;
    private int microseconds;
    private short type;
    private short code;
    private int value;

    public GetEvent(byte[] vals) {
        seconds = toInt(vals[0]) + (toInt(vals[1]) << 8) + (toInt(vals[2]) << 16) + (toInt(vals[3]) << 24);
        microseconds = toInt(vals[4]) + (toInt(vals[5]) << 8) + (toInt(vals[6]) << 16) + (toInt(vals[7]) << 24);
        type = (short) (toInt(vals[8]) + (toInt(vals[9]) << 8));
        code = (short) (toInt(vals[10]) + (toInt(vals[11]) << 8));
        value = toInt(vals[12]) + (toInt(vals[13]) << 8) + (toInt(vals[14]) << 16) + (toInt(vals[15]) << 24);
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
        return (seconds * 1000) + (microseconds / 1000);
    }
}