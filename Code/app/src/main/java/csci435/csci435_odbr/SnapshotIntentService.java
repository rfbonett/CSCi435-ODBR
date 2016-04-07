package csci435.csci435_odbr;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
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
public class SnapshotIntentService extends IntentService {

    String filename;
    static OutputStream os;
    Process sh;
    static InputStream is;
    static BufferedReader br;


    public SnapshotIntentService() {
        super("SnapshotIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if (Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState())) {
            File sdCard = Environment.getExternalStorageDirectory();
            File directory = new File(sdCard.getAbsolutePath() + "/ScreenShots");
            directory.mkdirs();

            //filename = "screenshot" + Globals.screenshot_index + ".png";
            try {
                sh = Runtime.getRuntime().exec("su", null, null);
                os = sh.getOutputStream();
                is = sh.getInputStream();
                br = new BufferedReader(new InputStreamReader(is));
                Globals.time_last_event = System.currentTimeMillis();
                Globals.screenshot_index = 0;
                startGetEvent();
                writeCheck();
                writeScreenshot(getApplicationContext());
            } catch (Exception e) {
                e.printStackTrace();
            }


        } else {
            Log.v("Screenshot", "ERROR");

        }

    }

    public static void writeScreenshot(Context context){

        try {
            String filename = "screenshot" + Globals.screenshot_index + ".png";
            os.write(("/system/bin/screencap -p " + "/sdcard/ScreenShots/" + filename + "\n").getBytes("ASCII"));
            os.flush();


            //need something that will wait until the screenshot has finished

            //make toast?
            CharSequence text = "Hello toast!";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            Log.v("Screenshot", filename);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void writeCheck(){

        try {
            String s = "";
            //Log.v("Screenshot", "hanging in check");
            while(!s.contains("APP_STATE_IDLE")) {
                os.write(("dumpsys window -a | grep 'mAppTransitionState'\n").getBytes("ASCII"));
                os.flush();
                s = br.readLine();
                Log.v("Screenshot", s);
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void startGetEvent(){
        try {
            //Start getevent in background, note the ampersand
            Log.v("Screenshot", "tried to start");
            os.write(("/system/bin/getevent -t > sdcard/events.txt & \n").getBytes("ASCII"));

        } catch (Exception e) {}

    }

    public static void stopGetEvent(){

        //Kill the getevent process
        try {
            Log.v("Screenshot", "tried to kill");
            os.write(("kill $(pidof getevent)\n").getBytes("ASCII"));
        } catch (Exception e) {}
    }


    public static void finishWriting() {
        try {
            stopGetEvent();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
