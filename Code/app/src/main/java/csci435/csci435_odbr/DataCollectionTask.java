package csci435.csci435_odbr;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import android.graphics.BitmapFactory;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.os.Environment;
import android.view.View;
import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;
import android.hardware.Sensor;

/**
 * Created by Rich on 2/11/16.
 */
public class DataCollectionTask extends AsyncTask<String, Void, Void> implements SensorEventListener {
    Process sh;
    OutputStream os;

    @Override
    protected Void doInBackground(String... params) {

        return null;
    }

    /**
     * Adds sensor data to the BugReport
     * @param event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        BugReport.getInstance().addSensorData(event.sensor, event);
    }


    /**
     * If paused, resumes recording by registering listener for all sensors.
     * If not paused, unregisters listener for all sensors.
     */
    public void togglePaused(boolean paused) {
        if (paused) {
            for (Sensor s : Globals.sensors) {
                Globals.sMgr.registerListener(this, s, SensorManager.SENSOR_DELAY_NORMAL);
            }
        }
        else {
            Globals.sMgr.unregisterListener(this);
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
