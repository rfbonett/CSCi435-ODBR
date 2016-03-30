package csci435.csci435_odbr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by Brendan Otten on 3/25/2016.
 */
public class SnapshotReciever extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle extras = intent.getExtras();
        Boolean visibility = extras.getBoolean("visibility");
        if(!visibility){
            RecordFloatingWidget.hideForScreenshot();
        }
        else{
            RecordFloatingWidget.restoreAfterScreenshot();
        }

    }
}
