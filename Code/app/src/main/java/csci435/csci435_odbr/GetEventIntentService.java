package csci435.csci435_odbr;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Created by Brendan Otten on 3/26/2016.
 */
public class GetEventIntentService extends IntentService{



    public GetEventIntentService() {
        super("GetEventIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        //declare what to do with a get event here
        //Start get event superuser process, this su begins when the play button is pressed basically for the first time,
        //it will terminate idk when. Otherwise we can turn this into a single event to be obtained, that is fired off multiple times




        //Post Results to the user from getEvent
        Intent localIntent = new Intent("csci435.csci435_odbr.GetEventIntentService.send").putExtra("csci435.csci435_odbr.GetEventIntentService.status", 1);
        // Broadcasts the Intent to receivers in this app.
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);

    }
}
