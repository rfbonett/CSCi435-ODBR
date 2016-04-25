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
 */
public class RecordFloatingWidget extends Service {
    static WindowManager wm;
    static LinearLayout ll;
    static Handler handler = new Handler();

    final static WindowManager.LayoutParams parameters = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            PixelFormat.TRANSPARENT);

    private SensorDataManager sdm;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        BugReport.getInstance().clearReport();

        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        ll = new LinearLayout(this);
        ll.setGravity(Gravity.CENTER);

        //Inflate the linear layout containing the buttons
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.floating_widget_layout, ll);
        ll.setBackgroundColor(0x8833B5E5);

        wm.addView(ll, parameters);

        // Start Sensor Data Collection
        sdm = new SensorDataManager();
        sdm.startRecording();
    }

    public static void hideOverlay() {
        wm.removeView(ll);
    }

    public static void restoreOverlay() {
        wm.addView(ll, parameters);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
    }


    public void recordEvents(View view){
        hideOverlay();
        handler.post(widget_timer);
        Globals.recording = true;
        Globals.time_last_event = System.currentTimeMillis();
    }


    public void stopRecording(View view) {
        sdm.stopRecording();
        GetEventIntentService.endGetEvent();
        while (Globals.recording) {}
        partition_events pe = new partition_events();
        pe.my_parse();


        Intent intent = new Intent(RecordFloatingWidget.this, RecordActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
        ll.setVisibility(View.GONE);
        onDestroy();
    }

    public static Runnable widget_timer = new Runnable() {
        @Override
        public void run() {

            //check to see if we have reached the condition.
            if(System.currentTimeMillis() - Globals.time_last_event > 3000){
                //we dont want to loop anymore
                RecordFloatingWidget.restoreOverlay();
            }
            else{
                //check again 1 second later
                handler.postDelayed(widget_timer, 1000);
            }

        }
    };
}