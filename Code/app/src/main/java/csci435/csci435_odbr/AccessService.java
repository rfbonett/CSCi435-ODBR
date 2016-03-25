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

        //Log.v("AccessService", "Event: " + event.getWindowId());//event.getPackageName().equals(Globals.packageName));
        if(event.getPackageName().equals(Globals.packageName) && Globals.trackUserEvents) {
            Globals.screenshot = 1;
            Log.v("AccessService", "" + (getRootInActiveWindow() == null ? "null" : getRootInActiveWindow()));
            Log.v("AccessService", "" + event.getSource());
            //Toast.makeText(getBaseContext(), "Service: " + event.getPackageName(), Toast.LENGTH_SHORT).show();
            BugReport.getInstance().addUserEvent(event);

            //hide for screenshot
            RecordFloatingWidget.hideForScreenshot();
            //background thread is started so we can prompt the hideforscreenshot now, that will hide, and then change wait
            Intent intent = new Intent(this, SnapshotIntentService.class);
            int index = BugReport.getInstance().numEvents();
            intent.putExtra("index", index);
            startService(intent);

            //RecordFloatingWidget.restoreAfterScreenshot();

            //take the screenshot by firing off a new intent

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

    @Override
    public void onServiceConnected(){
        Log.v("AccessService", "Connected");
        Toast.makeText(getBaseContext(), "Service started", Toast.LENGTH_SHORT).show();
    }


}
