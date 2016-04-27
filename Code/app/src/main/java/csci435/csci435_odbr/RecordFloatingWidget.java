package csci435.csci435_odbr;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
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
    private GetEventManager gem;

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
        SeekBar s = (SeekBar) ll.findViewById(R.id.slideBar);
        s.setOnSeekBarChangeListener(new SlideBarListener());

        // Start Sensor Data and GetEvent Data Collection
        gem = new GetEventManager();
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
        gem.startRecording();
        Globals.recording = true;
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
                gem.parseEvents();
                Intent intent = new Intent(RecordFloatingWidget.this, RecordActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
                ll.setVisibility(View.GONE);
                onDestroy();
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
                Globals.recording = false;
            }
            else{
                //check again 1 second later
                handler.postDelayed(widget_timer, 1000);
            }

        }
    };

    class SlideBarListener implements SeekBar.OnSeekBarChangeListener {

        private int maximum = 100;
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(final SeekBar seekBar) {
            if (seekBar.getProgress() < maximum) {
                ValueAnimator anim = ValueAnimator.ofInt(seekBar.getProgress(), 0);
                anim.setDuration(5*(seekBar.getProgress()));
                anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        int animProgress = (Integer) animation.getAnimatedValue();
                        seekBar.setProgress(animProgress);
                    }
                });
                anim.start();
            }
            else {
                Button recordButton = (Button) ll.findViewById(R.id.recordButton);
                TextView slideBarText = (TextView) ll.findViewById(R.id.slideBarText);
                recordButton.setClickable(false);
                remove(recordButton);
                remove(slideBarText);
                remove(seekBar);
                stopRecording();
            }
        }
    }

    private void remove(final View view) {
        ObjectAnimator anim = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
        anim.setDuration(400);
        anim.start();
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                view.setVisibility(View.GONE);
            }
        }, 400);
    }

    private void rotate(View view) {
        ObjectAnimator anim = ObjectAnimator.ofFloat(view, "rotation", 0f, 360f);
        anim.setDuration(100);
        anim.setRepeatCount(ObjectAnimator.INFINITE);
        anim.start();
    }
}