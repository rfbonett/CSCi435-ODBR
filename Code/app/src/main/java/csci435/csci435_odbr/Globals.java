package csci435.csci435_odbr;

import android.hardware.Sensor;
import android.hardware.SensorManager;

import java.util.ArrayList;

/**
 * Created by Rich on 2/10/16.
 */
public class Globals {
    public static String appName;
    public static String packageName;
    public static ArrayList<Sensor> sensors;
    public static SensorManager sMgr;
    public static boolean recording;
    public static boolean trackUserEvents;
    public static int width;
    public static int height;
    public static int screenshot;
    public static int availableHeightForImage;
    public static int wait;
    public static int total_screenshots;
}
