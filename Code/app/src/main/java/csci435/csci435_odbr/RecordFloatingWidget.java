package csci435.csci435_odbr;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * Created by Rich on 2/16/16.
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
        sdm = new SensorDataManager();
    }

    public void hideOverlay() {
        wm.removeView(ll);
    }

    public void restoreOverlay() {
        wm.addView(ll, parameters);
        stopRecording();
    }

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


    public void recordEvents(View view){
        gem.startRecording();
        sdm.startRecording();
        Globals.time_last_event = System.currentTimeMillis();
        hideOverlay();
        handler.post(widget_timer);
    }


    public void stopRecording() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                sdm.stopRecording();
                gem.stopRecording();
            }
        });
    }

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