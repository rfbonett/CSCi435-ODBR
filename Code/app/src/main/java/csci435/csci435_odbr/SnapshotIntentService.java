package csci435.csci435_odbr;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Brendan Otten on 3/23/2016.
 */
public class SnapshotIntentService extends IntentService {

    String filename;
    static OutputStream os;
    Process sh;


    public SnapshotIntentService() {
        super("SnapshotIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //Take screenshot, allows us to define other scenarios just incase we need to here

        //implemented for getEvent
        Bundle extras = intent.getExtras();
        long timestamp = 0;
        if (Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState())) {
            File sdCard = Environment.getExternalStorageDirectory();
            File directory = new File(sdCard.getAbsolutePath() + "/ScreenShots");
            directory.mkdirs();

            filename = "screenshot" + Globals.screenshot_index + ".png";
            try {
                sh = Runtime.getRuntime().exec("su", null, null);
                os = sh.getOutputStream();
                Globals.time_last_event = System.currentTimeMillis();
                writeBytes();
            } catch (Exception e) {
                e.printStackTrace();
            }


        } else {
            Log.v("Screenshot", "ERROR");

        }

    }


    private void takeScreenShot(int i) {
        Log.v("Snapshot", "Started");

        //Run SU process here, we are in background thread.
        //Log.v("Screenshot", "Screenshot async occuring");
        long timestamp = 0;
        if (Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState())) {
            File sdCard = Environment.getExternalStorageDirectory();
            File directory = new File(sdCard.getAbsolutePath() + "/ScreenShots");
            directory.mkdirs();

            filename = "screenshot" + Globals.screenshot_index + ".png";
            try {
                sh = Runtime.getRuntime().exec("su", null, null);
                os = sh.getOutputStream();
                os.write(("/system/bin/screencap -p " + "/sdcard/ScreenShots/" + filename +"\n").getBytes("ASCII"));
                os.flush();

                timestamp = System.currentTimeMillis();
                Screenshots screenshots = new Screenshots(filename, timestamp);
                BugReport.getInstance().addPotentialScreenshot(screenshots);
            } catch (Exception e) {
                e.printStackTrace();
            }


        } else {
            Log.v("Screenshot", "ERROR");

        }
        //Tell program to restore the widget
        Intent localIntent = new Intent("csci435.csci435_odbr.SnapshotIntentService.send").putExtra("timestamp", timestamp);
        localIntent.putExtra("filename", filename);
        // Broadcasts the Intent to receivers in this app.
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
        //Globals.screenshot = 0;
    }

    public static void writeBytes(){
        try {
            String filename = "screenshot" + Globals.screenshot_index + ".png";
            os.write(("/system/bin/screencap -p " + "/sdcard/ScreenShots/" + filename + "\n").getBytes("ASCII"));
            os.flush();
            long timestamp = System.currentTimeMillis();
            Screenshots screenshots = new Screenshots(filename, timestamp);
            BugReport.getInstance().addPotentialScreenshot(screenshots);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static void finishWriting() {
        try {
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
