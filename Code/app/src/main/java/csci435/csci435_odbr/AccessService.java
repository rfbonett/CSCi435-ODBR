package csci435.csci435_odbr;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

/**
 * Created by Brendan Otten on 2/17/2016.
 * resource: http://developer.android.com/guide/topics/ui/accessibility/services.html
 */
public class AccessService extends AccessibilityService{
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        if(event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            AccessibilityNodeInfo nodeInfo = event.getSource();
            Toast.makeText(getBaseContext(), "Service: " + event.getPackageName(), Toast.LENGTH_SHORT).show();
            //Where do we store this data? It all seems pertinent, only keep data that is not part of our app so we ignore things
            //That have packageName == "csci435.csci435_odbr"
            event.getEventType();
            event.getPackageName();
            event.getEventTime();
            //record event and take snapshot here?
        }
    }

    @Override
    public void onInterrupt() {

    }
}
