package csci435.csci435_odbr;

import android.os.*;
import android.util.Log;

import java.io.File;
import java.io.OutputStream;
import java.lang.Process;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Rich on 4/25/16.
 */
public class GetEventManager {

    private int fileNdx;
    private String directory;
    private Process su;
    private OutputStream os;
    private boolean recording;

    public GetEventManager() {
        fileNdx = 0;
        directory = "sdcard/Events/";
        File dir = new File(directory);
        if (dir.exists()) {
            for (File f : dir.listFiles()) {
                f.delete();
            }
        }
        else {
            dir.mkdir();
        }
        recording = false;
    }

    public void startRecording() {
        if (recording) {
            return;
        }
        try {
            String filename = directory + "events.txt";
            su = Runtime.getRuntime().exec("su", null, null);
            os = su.getOutputStream();
            os.write(("/system/bin/getevent -t > " + filename + " & \n").getBytes("ASCII"));
            os.flush();
            recording = true;
        } catch (Exception e) {
            Log.v("GetEventManager", "Error starting GetEvent process.");
        }
    }

    public void stopRecording() {
        try {
            os.write(("killall getevent\n").getBytes("ASCII"));
            os.flush();
            os.write(("exit\n").getBytes("ASCII"));
            os.flush();
            os.close();
            su.waitFor();
            recording = false;
        } catch (Exception e) {
            Log.v("GetEventManager", "Error stopping GetEvent process.");
        }
    }

    public void parseEvents() {
        File file = new File(directory + "events.txt");
        GetEventParser.parse(file);
    }

}
