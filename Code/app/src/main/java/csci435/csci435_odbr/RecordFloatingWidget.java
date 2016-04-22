package csci435.csci435_odbr;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ToggleButton;
import android.widget.Button;

/**
 * Created by Rich on 2/16/16.
 */
public class RecordFloatingWidget extends Service {
    public static boolean widget_hidden = false;
    static WindowManager wm;
    static LinearLayout ll;
    boolean visibility;

    static Handler handler = new Handler();

    static ToggleButton options;
    static Button submit;
    static ToggleButton pause;

    final int animationTime = 100;
    float animationDist;
    float animationDistLong;
    private boolean firstClick = true;

    final static WindowManager.LayoutParams parameters = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            PixelFormat.TRANSPARENT);

    long report_start_time;
    long report_end_time;

    private static SensorDataLogger sensorDataLogger;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        BugReport.getInstance().clearReport();

        //get report start time
        report_start_time = System.currentTimeMillis();

        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        ll = new LinearLayout(this);
        ll.setGravity(Gravity.CENTER);

        //
        //Inflate the linear layout containing the buttons
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.floating_widget_layout, ll);


        //Initialize Window Manager
        //parameters.gravity = Gravity.CENTER;
        //parameters.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;
        ll.setBackgroundColor(0x8833B5E5);


        visibility = true;
        //options = (ToggleButton) ll.findViewById(R.id.optionsButton);
        //submit = (Button) ll.findViewById(R.id.submitButton);
        //pause = (ToggleButton) ll.findViewById(R.id.pauseButton);


        wm.addView(ll, parameters);

        //animationDist = options.getY() - pause.getY();
        //animationDistLong = options.getY() - submit.getY();
        //moveDown(submit, animationDistLong);
        //moveDown(pause, animationDist);
        //Allow the button to be moved around the screen
        /*
        options.setOnTouchListener(new View.OnTouchListener() {
            WindowManager.LayoutParams updatedParameters = parameters;
            double x;
            double y;
            double pressedX;
            double pressedY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                        x = updatedParameters.x;
                        y = updatedParameters.y;

                        pressedX = event.getRawX();
                        pressedY = event.getRawY();

                        break;

                    case MotionEvent.ACTION_MOVE:
                        updatedParameters.x = (int) (x + (event.getRawX() - pressedX));
                        updatedParameters.y = (int) (y + (event.getRawY() - pressedY));

                        wm.updateViewLayout(ll, updatedParameters);

                    default:
                        break;
                }

                return false;
            }
        });
        */
        // Start Sensor Data Collection AsyncTask and set up pause/resume button
        sensorDataLogger = new SensorDataLogger();
        Log.v("Screenshot", Globals.recording + "helapsda");
        //sensorDataLogger.togglePaused(true);

        /*
        pause.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (raw[0] == 0) {
                    raw[0] = 1;
                    //fires the first time the button is clicked
                    RecordFloatingWidget.hideForScreenshot();
                    Globals.time_last_event = System.currentTimeMillis();
                    handler.post(widget_timer);
                    startScreenshots();
                    Globals.recording = true;
                }
                sensorDataLogger.togglePaused(isChecked);
            }
        });
        */

    }

    public static void hideForScreenshot() {
        wm.removeView(ll);
        widget_hidden = true;
    }

    public static void restoreOverlay() {
        //sensorDataLogger.togglePaused(true);
        wm.addView(ll, parameters);
        widget_hidden = false;
    }

/*
    private void rotate(View view) {
        ObjectAnimator anim = ObjectAnimator.ofFloat(view, "rotation", 0f, 360f);
        anim.setDuration(animationTime);
        anim.start();
    }
*/
    /*
    private void moveDown(View view, float length) {
        view.setY(options.getY());
        ObjectAnimator anim = ObjectAnimator.ofFloat(view, "translationY", length);
        anim.setDuration(animationTime);
        anim.start();
    }
*/
/*
    private void disappear(final View view) {
        ObjectAnimator anim = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
        anim.setDuration(animationTime);
        anim.start();
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                view.setVisibility(View.GONE);
            }
        }, animationTime);
    }
*/
    /*
    private void appear(final View view) {
        view.setVisibility(View.VISIBLE);
        ObjectAnimator anim = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        anim.setDuration(animationTime);
        anim.start();
    }
    */
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
    }

/*
    public void toggleButtons(View view) {

        rotate(options);
        if (visibility) {
            disappear(pause);
            disappear(submit);
        }
        else {
            appear(pause);
            appear(submit);
            moveDown(pause, animationDist);
            moveDown(submit, animationDistLong);
        }
        visibility = !visibility;
    }
*/
    public void recordEvents(View view){

        hideForScreenshot();
        Globals.time_last_event = System.currentTimeMillis();
        handler.post(widget_timer);

        if(firstClick){
            firstClick = false;
            startScreenshots();
            Globals.recording = true;
        }

        //We shouldn't have to pause the sensor data since we are just flat recording everything
        //sensorDataLogger.togglePaused(true);
    }

    public void startScreenshots(){
        Intent intent = new Intent(this, SnapshotIntentService.class);
        startService(intent);
    }

    public void stopRecording(View view) {
        //Launch RecordActivity
        //Log.v("Event count", "number of events: " + BugReport.getInstance().numEvents());
        Globals.recording = false;
        Globals.trackUserEvents = false;
        Globals.firstEvent = false;
        //sensorDataLogger.togglePaused(false);
        GetEventIntentService.endGetEvent();
        SnapshotIntentService.endScreenshots();
        partition_events pe = new partition_events();
        pe.my_parse();
        BugReport.getInstance().matchEvents();
        BugReport.getInstance().refineEventList();


        //GENERATES ALL OF THE SCREENSHOTS AND ADDS THEM

        Intent intent = new Intent(RecordFloatingWidget.this, RecordActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
        ll.setVisibility(View.GONE);

        //report end time
        report_end_time = System.currentTimeMillis();
        onDestroy();
    }

    public static Runnable widget_timer = new Runnable() {
        @Override
        public void run() {

            //check to see if we have reached the condition.
            if(System.currentTimeMillis() - Globals.time_last_event > 6500){
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

class OrientationChangeListener extends OrientationEventListener {

    public OrientationChangeListener(Context context) {
        super(context);
    }

    @Override
    public void onOrientationChanged(int orientation) {
        BugReport.getInstance().addOrientationChange(System.currentTimeMillis(), orientation);
    }
}
