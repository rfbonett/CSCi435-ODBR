package csci435.csci435_odbr;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;
import android.content.Intent;

import java.io.DataOutputStream;
import java.io.IOException;

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
    public int onStartCommand(Intent intent, int flags, int startid) {
        Log.v("AccessService", "Starting");
        Log.v("AccessService", "Test: " + Globals.appName);
        return super.onStartCommand(intent, flags, startid);
    }


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        if(event.getPackageName().equals(Globals.packageName)) {

            //toggle pause and play here.
            //if(RecordFloatingWidget.pause.isChecked()) {
            //Toast.makeText(getBaseContext(), "Service: " + event.getPackageName(), Toast.LENGTH_SHORT).show();

            BugReport.getInstance().getUserEvents().add(new Events(event));
            //take snapshot

            try {
                takeScreenShot();
            } catch (Exception e) {
                e.printStackTrace();
            }
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

    public void takeScreenShot() throws Exception {
        try{
            Log.v("AccessibilityScreenshot", "Success");
            Process su = Runtime.getRuntime().exec("su");
            DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());

            outputStream.writeBytes("screencap -p " + "/storage/sdcard/events.png");
            outputStream.flush();

            outputStream.writeBytes("exit\n");
            outputStream.flush();
            su.waitFor();
        }catch(IOException e){
            throw new Exception(e);
        }catch(InterruptedException e){
            throw new Exception(e);
        }
    }
}
