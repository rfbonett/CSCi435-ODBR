package csci435.csci435_odbr;

import android.app.IntentService;
import android.app.UiAutomation;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * Created by Brendan Otten on 3/23/2016.
 */
public class GetEventIntentService extends IntentService {

    String filename;
    static Process su_getEvent;
    static OutputStream os;



    public GetEventIntentService() {
        super("SnapshotIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        try {
            su_getEvent = Runtime.getRuntime().exec("su", null, null);
            os = su_getEvent.getOutputStream();
            File sdCard = Environment.getExternalStorageDirectory();
            File fp = new File(sdCard+"/events.txt");
            if(fp.exists()){
                fp.delete();
            }

            startGetEvent();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void startGetEvent(){
        try {

            //Start getevent in background, note the ampersand
            Log.v("Getevent", "tried to start");
            os.write(("/system/bin/getevent -t > sdcard/events.txt & \n").getBytes("ASCII"));
            os.flush();
        } catch (Exception e) {}

    }

    public static void endGetEvent(){
        //Kill the getevent process
        try {
            Log.v("Getevent", "tried to kill");
            os.write(("fflush(stdout)\n").getBytes("ASCII"));
            os.flush();
            os.write(("kill $(pidof getevent)\n").getBytes("ASCII"));
            os.flush();
            os.write(("exit\n").getBytes("ASCII"));
            os.flush();
            os.close();
            su_getEvent.getInputStream().close();
            su_getEvent.waitFor();
            Globals.recording = false;
        } catch (Exception e) {}
    }


}
