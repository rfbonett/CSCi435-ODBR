package csci435.csci435_odbr;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
import android.view.OrientationEventListener;
import android.view.View;
import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;
import android.hardware.Sensor;

/**
 * Created by Rich on 2/11/16.
 * Manages the sensors that we are going to be listening for. It allows for the start / stop of the listening process
 * where the listening registers changes to the values. If a change is detected then the value is registered.
 */
public class SensorDataManager implements SensorEventListener {

    HashMap<Sensor, float[]> lastLoggedData = new HashMap<Sensor, float[]>();

    public SensorDataManager(Context c) {
        OrientationLogger ol = new OrientationLogger(c);
    }

    /**
     * Method to start recording of all designated sensors
     */
    public void startRecording() {
        for (Sensor s : Globals.sensors) {
            Globals.sMgr.registerListener(this, s, (SensorManager.SENSOR_DELAY_NORMAL)*10);
            lastLoggedData.put(s, new float[] {});
        }
    }

    /**
     * Method to stop recording all designated sensors
     */
    public void stopRecording() {
        Globals.sMgr.unregisterListener(this);
    }


    /**
     * Adds sensor data to the BugReport
     * @param event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!Arrays.equals(lastLoggedData.get(event.sensor), event.values)) {
            BugReport.getInstance().addSensorData(event.sensor, event);
            lastLoggedData.put(event.sensor, event.values.clone());
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {/* Nothing to do */}



}

/**
 * Listener class to register the orientation of the device, associating the orientation with times.
 */
class OrientationLogger extends OrientationEventListener {

    public OrientationLogger(Context c) {
        super(c);
    }

    @Override
    public void onOrientationChanged(int orientation) {
        BugReport.getInstance().addOrientation(System.currentTimeMillis(), orientation);
    }
}