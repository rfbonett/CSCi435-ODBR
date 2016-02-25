package csci435.csci435_odbr;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;
import android.content.Intent;

/**
 * Created by Brendan Otten on 2/17/2016.
 * resource: http://developer.android.com/guide/topics/ui/accessibility/services.html
 */
public class AccessService extends AccessibilityService {

    @Override
    protected void onServiceConnected() {
        Log.v("AccessService", "Connected");
    }


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        if(event.getPackageName().equals(Globals.packageName)) {

            //toggle pause and play here.
            //if(RecordFloatingWidget.pause.isChecked()) {
                //Toast.makeText(getBaseContext(), "Service: " + event.getPackageName(), Toast.LENGTH_SHORT).show();

                BugReport.getInstance().getUserEvents().add(new Events(event));
                //take snapshot
            //}
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
}
