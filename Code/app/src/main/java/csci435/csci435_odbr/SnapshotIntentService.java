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
    static Process su_getEvent;
    static Process su_screenshots;



    public SnapshotIntentService() {
        super("SnapshotIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState())) {
            File sdCard = Environment.getExternalStorageDirectory();
            File directory = new File(sdCard.getAbsolutePath() + "/ScreenShots");
            directory.mkdirs();

            //filename = "screenshot" + Globals.screenshot_index + ".png";
            try {
                su_screenshots = Runtime.getRuntime().exec("su", null, null);

                Globals.time_last_event = System.currentTimeMillis();
                Globals.screenshot_index = 0;
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
            OutputStream os = su_screenshots.getOutputStream();
            String filename = "screenshot" + Globals.screenshot_index + ".png";

            os.write(("/system/bin/screencap -p " + "/sdcard/ScreenShots/" + filename + "\n").getBytes("ASCII"));
            os.flush();

            os.write(("exit\n").getBytes("ASCII"));
            os.flush();
            os.close();
            su_screenshots.waitFor();

            //make toast?
            CharSequence text = "Data Recorded!";
            int duration = Toast.LENGTH_SHORT;

            final Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            Log.v("Screenshot", filename);

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    toast.cancel();
                }
            }, 500);
            su_screenshots = Runtime.getRuntime().exec("su", null, null);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void endScreenshots(){
        try {
            OutputStream os = su_screenshots.getOutputStream();
            os.write(("exit\n").getBytes("ASCII"));
            os.flush();
            os.close();
            su_screenshots.waitFor();
        } catch (Exception e){}
    }

    public static void writeCheck(){

        try {

            OutputStream os = su_screenshots.getOutputStream();
            InputStream is = su_screenshots.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

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

}
