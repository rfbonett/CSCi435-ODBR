package csci435.csci435_odbr;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;
import android.content.Intent;

import java.io.File;


/**
 * Created by Brendan Otten on 2/17/2016.
 * resource: http://developer.android.com/guide/topics/ui/accessibility/services.html
 */
public class AccessService extends AccessibilityService {

    @Override
    public int onStartCommand(Intent intent, int flags, int startid) {
        return super.onStartCommand(intent, flags, startid);
    }


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.v("AccessService", "Event: " + AccessibilityEvent.eventTypeToString(event.getEventType()));
        if (Globals.recording) {
            if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_HOVER_EXIT) {
                Globals.exited = true;
            }
            else if (Globals.exited) {
                Globals.exited = false;
                Globals.time_last_event = System.currentTimeMillis();
                Log.v("AccessService", "ReportEvent: " + event.getPackageName());
                ReportEvent e = new ReportEvent(event.getEventTime());

                if (event.getPackageName().equals(Globals.packageName)) {
                    Log.v("AccessService", "Adding and adjusting event");
                    BugReport.getInstance().addEvent(e, getRootInActiveWindow());
                }
            }
        }
    }

    @Override
    public void onInterrupt() {}

    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }

    @Override
    public void onServiceConnected(){
        Toast.makeText(getBaseContext(), "Service started", Toast.LENGTH_SHORT).show();
    }


}
