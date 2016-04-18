package csci435.csci435_odbr;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
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
    Handler handler = new Handler();
    /*
    public Runnable widget_timer = new Runnable() {
        @Override
        public void run() {

            //check to see if we have reached the condition.
            if(System.currentTimeMillis() - Globals.time_last_event > 8000){
                //we dont want to loop anymore
                RecordFloatingWidget.restoreAfterScreenshot();
            }
            else{
                //check again 1 second later
                handler.postDelayed(widget_timer, 1000);
            }

        }
    };
    */
    @Override
    public int onStartCommand(Intent intent, int flags, int startid) {
        Log.v("AccessService", "Starting");
        Log.v("AccessService", "Test: " + Globals.appName);
        return super.onStartCommand(intent, flags, startid);
    }


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        Log.v("AccessService", "Event: " + event.getPackageName());
        if(Globals.recording) {

            BugReport.getInstance().addUserEvent(event);

            if(event.getPackageName().equals(Globals.packageName)){

                //We want to add events in an unbiased manner, but we only want to increment the screenshots
                //and fire them if they are fired on the app we are recording.

                Globals.time_last_event = System.currentTimeMillis();

                //hides until we want to reveal again
                if(!(RecordFloatingWidget.widget_hidden)){
                    RecordFloatingWidget.hideForScreenshot();
                    RecordFloatingWidget.handler.post(RecordFloatingWidget.widget_timer);
                }

                Log.v("Screenshot", "screenshot fired");
                Globals.screenshot_index++;

                Context context = getApplicationContext();
                SnapshotIntentService.writeCheck();
                SnapshotIntentService.writeScreenshot(context);
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

    @Override
    public void onServiceConnected(){
        Log.v("AccessService", "Connected");
        Toast.makeText(getBaseContext(), "Service started", Toast.LENGTH_SHORT).show();
    }


}
