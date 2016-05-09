package csci435.csci435_odbr;

import android.app.IntentService;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
        } catch(Exception e){}
    }


    class ReplayEvent implements Runnable {
        private long wait_time = 2000; //Milliseconds before starting inputs and after returning to report
        @Override
        public void run() {
            String cmd = "";
            try {
                Thread.sleep(wait_time);
                long time = BugReport.getInstance().getEventAtIndex(0).getInputEvents().get(0).getTimeMillis();
                for (ReportEvent event : BugReport.getInstance().getEventList()) {
                    String device = event.getDevice();
                    for (GetEvent e : event.getInputEvents()) {
                        long waitfor = System.currentTimeMillis() + (e.getTimeMillis() - time);
                        //Inefficient to be sure, but more accurate results than Thread.sleep(), and
                        //ScheduledThreadExecutor was not playing well, ToDo: replace while block
                        while (System.currentTimeMillis() < waitfor) {}

                        Log.v("ReplayService", e.getSendEvent(device));
                        os.write((e.getSendEvent(device) + "\n").getBytes("ASCII"));
                        os.flush();
                        time = e.getTimeMillis();
                    }
                }
                os.close();
                su_replay.waitFor();
                Thread.sleep(wait_time);
            } catch (Exception e) {Log.v("ReplayService", "Unable to replay event: " + cmd);}


            Intent record_intent = new Intent(ReplayService.this, RecordActivity.class);
            record_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            record_intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(record_intent);
        }
    }


}
