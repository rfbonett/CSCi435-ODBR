package csci435.csci435_odbr;

import android.accessibilityservice.AccessibilityService;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;
import android.content.Intent;
import android.provider.Settings;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by Brendan Otten on 2/17/2016.
 * resource: http://developer.android.com/guide/topics/ui/accessibility/services.html
 */
public class AccessService extends AccessibilityService {

    @Override
    public int onStartCommand(Intent intent, int flags, int startid) {
        Log.v("AccessService", "Starting");
        Log.v("AccessService", "Test: " + Globals.appName);
        return super.onStartCommand(intent, flags, startid);
    }


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        Log.v("AccessService", "Event: " + event.getWindowId());//event.getPackageName().equals(Globals.packageName));
        if(event.getPackageName().equals(Globals.packageName) && Globals.trackUserEvents) {
            //toggle pause and play here.
            //if(RecordFloatingWidget.pause.isChecked()) {
            //Toast.makeText(getBaseContext(), "Service: " + event.getPackageName(), Toast.LENGTH_SHORT).show();

            BugReport.getInstance().addUserEvent(event);
            //take snapshot
            //}

            try {
                takeScreenShot();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onInterrupt() {
        Log.v("AccessService", "RIP AccessService");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.v("AccessService", "RIP AccessService");
        return super.onUnbind(intent);
    }


    public void takeScreenShot() throws Exception {
        try{
            Process su = Runtime.getRuntime().exec("su");
            DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());

            outputStream.writeBytes("screencap -p " + Environment.getExternalStorageDirectory() + "/events.png");
            outputStream.flush();

            outputStream.writeBytes("exit\n");
            outputStream.flush();
            su.waitFor();

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap b = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + "/events.png", options);
            BugReport.getInstance().addScreenshot(b);
            Log.v("AccessibilityScreenshot", "Success");
        }catch(Exception e) {
            Log.v("BugReport", "Screenshot threw exception");
            throw e;
        }
    }
}
