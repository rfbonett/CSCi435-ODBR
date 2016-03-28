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

        //restores the screenshot's params

        //RecordFloatingWidget.restoreAfterScreenshot();
        //Bundle bundle = intent.getExtras();
        //long timestamp = bundle.getLong("timestamp");
        //String filename = bundle.getString("filename");

        //Screenshots screenshot = new Screenshots();
        //screenshot.add_filename(filename);
        //screenshot.add_timestamp(timestamp);

        //add it to the stack
        //BugReport.getInstance().addPotentialScreenshot(screenshot);

        //Globals.total_screenshots--;

        //Globals.screenshot = 0;
    }
}
