package csci435.csci435_odbr;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.content.Intent;


/**
 * The Splash Screen is simply something nice to look at while the app configures itself. Shows a prompt once the
 * device data has been collected and parsed based on the type of device the app is being run on.
 */
public class SplashScreen extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GetEventDeviceInfo.getInstance().setDeviceData();
        showDeviceLimitations();
    }

    private void showDeviceLimitations(){
        AlertDialog.Builder prompt = new AlertDialog.Builder(this);
        String device = "";
        String msg = "";
        if(GetEventDeviceInfo.getInstance().isTypeSingleTouch()){
            device = "Single Touch Device";
            msg = "  - Clicks\n  - Long Clicks\n  - Single Pointer Swipes";
        }
        else if(GetEventDeviceInfo.getInstance().isMultiTouchA()){
            device = "Multitouch Device Type A";
            msg = "  - Clicks\n  - Long Clicks\n  - Multiple Pointer Swipes";
        }
        else if(GetEventDeviceInfo.getInstance().isMultiTouchB()){
            device = "Multitouch Device Type B";
            msg = "  - Clicks\n  - Long Clicks\n  - Multiple Pointer Swipes";
        }
        prompt.setTitle("Your device is: " + device);
        prompt.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                continueToApp();
            }
        });
        prompt.setMessage("This means your device supports:\n" + msg);
        prompt.show();
    }

    private void continueToApp(){
        Intent intent = new Intent(this, LaunchAppActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
        finish();
    }
}
