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

        if(event.getPackageName().equals(Globals.packageName)) {

            BugReport.getInstance().addUserEvent(event);

        }
    }

    @Override
    public void onInterrupt() {

    }
}
