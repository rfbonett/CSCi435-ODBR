package csci435.csci435_odbr;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import android.os.AsyncTask;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.os.Environment;
import android.view.View;
import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;


/**
 * Created by Rich on 2/11/16.
 */
public class DataCollectionTask extends AsyncTask<String, Void, Void> {
    Bitmap bitmap;
    int eventNum;

    protected Void doInBackground(String... s) {




        try {
            eventNum++;
            takeScreenshot();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void takeScreenshot() throws Exception {

        try{
            Process su = Runtime.getRuntime().exec("su");
            DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());

            outputStream.writeBytes("screencap /sdcard/screen" + eventNum + ".png\n");
            outputStream.flush();

            outputStream.writeBytes("exit\n");
            outputStream.flush();
            su.waitFor();
        }catch(IOException e){
            throw new Exception(e);
        }catch(InterruptedException e){
            throw new Exception(e);
        }
    }

}
