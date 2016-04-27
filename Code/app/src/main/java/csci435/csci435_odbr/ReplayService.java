package csci435.csci435_odbr;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by Brendan Otten on 4/20/2016.
 */
public class ReplayService extends IntentService {

    Process su_replay;
    OutputStream os;

    public ReplayService() {
        super("ReplayService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        //we're going to have to do an SU thing here, but for now, lets just log something every 10 seconds
        ExecutorService service = Executors.newSingleThreadExecutor();

        try {
            su_replay = Runtime.getRuntime().exec("su", null, null);
            os = su_replay.getOutputStream();
            service.submit(new ReplayEvent());

            /*  //For each event in the list we need to send that to the su, so:
            if(e.getGetEvent().get_type() == 0){
                //we have a click
                int [] coords = e.getGetEvent().get_coords().get(e.getGetEvent().get_coords().size() - 1);
                cmd = "input tap " + coords[0] + " " + coords[1];
            }
            else if(e.getGetEvent().get_type() == 1){
                //we have a long click
                Float duration = e.getGetEvent().get_duration();
                int [] coords = e.getGetEvent().get_coords().get(e.getGetEvent().get_coords().size() - 1);
                cmd = "input swipe " + coords[0] + " " + coords[1] + " " + coords[0] + " " + coords[1] + " " + (int)(duration * 1000);
            }
            else if(e.getGetEvent().get_type() == 1){
                //we have a swipe
                Float duration = e.getGetEvent().get_duration();
                int [] start_coords = e.getGetEvent().get_coords().get(0);
                int [] final_coords = e.getGetEvent().get_coords().get(e.getGetEvent().get_coords().size() - 1);
                cmd = "input swipe " + start_coords[0] + " " + start_coords[1] + " " + final_coords[0] + " " + final_coords[1] + " " + (int)(duration * 1000);
            } */
        } catch(Exception e){}
    }


    class ReplayEvent implements Runnable {
        private long wait_time = 2000; //Milliseconds before starting inputs and after returning to report
        @Override
        public void run() {
            String cmd = "";
            try {
                Thread.sleep(wait_time);
                for(int i = 0; i < BugReport.getInstance().numEvents(); i++) {
                    Log.v("Replay service", "ReportEvent: " + i);
                    ReportEvent e = BugReport.getInstance().getEventAtIndex(i);
                    int[] coords = e.getInputEvents().get(0);

                    cmd = "input tap " + String.valueOf(coords[0]) + " " + String.valueOf(coords[1]);
                    os.write((cmd + "\n").getBytes("ASCII"));
                    os.flush();
                    Thread.sleep(e.getDuration());
                }
                Thread.sleep(wait_time);
            } catch (Exception e) {Log.v("ReplayService", "Unable to replay event: " + cmd);}


            Intent record_intent = new Intent(ReplayService.this, RecordActivity.class);
            record_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            record_intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(record_intent);
        }
    }

}
