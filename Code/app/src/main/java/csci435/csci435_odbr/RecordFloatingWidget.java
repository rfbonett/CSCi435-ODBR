package csci435.csci435_odbr;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

/**
 * Created by Rich on 2/16/16.
 * Record Floating Widget is an overlay that is a service that is displayed over the application we are reporting.
 * The overlay allows for recording events or submitting the report. If the recording is started, it post delays to
 * a handler to see if it should reappear. Once the time since last event has been over 3 seconds it will appear again
 * and restart the process. Once it reappears it is responsible for sending notifications to each of the process managers
 * to terminate their processes.
 */
public class RecordFloatingWidget extends Service {
    WindowManager wm;
    LinearLayout ll;
    Handler handler = new Handler();

    final static WindowManager.LayoutParams parameters = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            PixelFormat.TRANSPARENT);

    private SensorDataManager sdm;
    private GetEventManager gem;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //Initialize Overlay
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        ll = new LinearLayout(this);
        ll.setGravity(Gravity.CENTER);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.floating_widget_layout, ll);
        ll.setBackgroundColor(0x8833B5E5);
        wm.addView(ll, parameters);

        // Prepare Report, start Data collection
        BugReport.getInstance().clearReport();
        gem = new GetEventManager();
        sdm = new SensorDataManager(this);
    }

    /**
     * Hides the overlay
     */
    public void hideOverlay() {
        wm.removeView(ll);
    }

    /**
     * Restores the overlay and stops the managers
     */
    public void restoreOverlay() {
        wm.addView(ll, parameters);
        stopRecording();
    }

    /**
     * Launches Record Activity and destroys itself, as the service is not associated with a specific activity
     * so the overlay would persist otherwise
     * @param v
     */
    public void submitReport(View v) {
        ll.setVisibility(View.GONE);
        Intent intent = new Intent();
        intent.setClass(this, RecordActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        onDestroy();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
    }


    /**
     * Starts the recording process for the events, called when the Record Inputs button is pressed
     * @param view
     */
    public void recordEvents(View view){
        gem.startRecording();
        sdm.startRecording();
        Globals.time_last_event = System.currentTimeMillis();
        hideOverlay();
        handler.post(widget_timer);
    }

    /**
     * Finishes the recording process for the events, called when the overlay reappears
     */
    public void stopRecording() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                sdm.stopRecording();
                gem.stopRecording();
            }
        });
    }

    /**
     * Runnable to be posted to the handler that tests whether or not we should restore the overlay
     */
    public Runnable widget_timer = new Runnable() {
        @Override
        public void run() {

            //check to see if we have reached the condition.
            if(System.currentTimeMillis() - Globals.time_last_event > 3000){
                //we dont want to loop anymore
                restoreOverlay();
            }
            else{
                //check again 1 second later
                handler.postDelayed(widget_timer, 1000);
            }

        }
    };

}