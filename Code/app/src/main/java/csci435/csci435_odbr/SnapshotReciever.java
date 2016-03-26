package csci435.csci435_odbr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Brendan Otten on 3/25/2016.
 */
public class SnapshotReciever extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        //restores the screenshot's params
        RecordFloatingWidget.restoreAfterScreenshot();

        Globals.screenshot = 0;
    }
}
