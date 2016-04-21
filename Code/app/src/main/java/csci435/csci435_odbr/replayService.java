package csci435.csci435_odbr;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.view.View;

/**
 * Created by Brendan Otten on 4/20/2016.
 */
public class ReplayService extends IntentService {

    public ReplayService() {
        super("ReplayService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        //we're going to have to do an SU thing here, but for now, lets just log something every 10 seconds

        for(int i = 0; i < 10; i++){
            try {
                Log.v("Replay", "Time: " + System.currentTimeMillis());
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Intent record_intent = new Intent(this, RecordActivity.class);
        record_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        record_intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(record_intent);
    }




}
