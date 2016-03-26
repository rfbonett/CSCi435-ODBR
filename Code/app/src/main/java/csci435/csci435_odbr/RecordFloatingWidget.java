package csci435.csci435_odbr;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ToggleButton;
import android.animation.ObjectAnimator;
import android.widget.Button;
import android.widget.CompoundButton;

/**
 * Created by Rich on 2/16/16.
 */
public class RecordFloatingWidget extends Service {
    LinearLayout oView;
    static WindowManager wm;
    static LinearLayout ll;
    boolean visibility;

    static ToggleButton options;
    static Button submit;
    static ToggleButton pause;

    static Button fill;


    final int animationTime = 250;
    float animationDist;
    float animationDistLong;

    final static WindowManager.LayoutParams parameters = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSPARENT);

    final static WindowManager.LayoutParams fill_params = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT);


    private DataCollectionTask sensorDataTask;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();


        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        ll = new LinearLayout(this);


        //Inflate the linear layout containing the buttons
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.floating_widget_layout, ll);


        //Initialize Window Manager
        parameters.x = 0;
        parameters.y = 0;
        parameters.gravity = Gravity.CENTER;
        //ll.setBackgroundColor(0x88ff0000);
        wm.addView(ll, parameters);

        visibility = true;
        options = (ToggleButton) ll.findViewById(R.id.optionsButton);
        submit = (Button) ll.findViewById(R.id.submitButton);
        pause = (ToggleButton) ll.findViewById(R.id.pauseButton);
        animationDist = options.getY() - pause.getY();
        animationDistLong = options.getY() - submit.getY();
        moveDown(submit, animationDistLong);
        moveDown(pause, animationDist);
        //Allow the button to be moved around the screen
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

        // Start Sensor Data Collection AsyncTask and set up pause/resume button
        sensorDataTask = new DataCollectionTask();
        sensorDataTask.execute();
        Log.v("Screenshot", Globals.recording + "");


        pause.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sensorDataTask.togglePaused(isChecked);
                Globals.trackUserEvents = !Globals.trackUserEvents;
            }
        });

        IntentFilter statusIntentFilter = new IntentFilter("csci435.csci435_odbr.SnapshotIntentService.send");

        SnapshotReciever snapshotReciever = new SnapshotReciever();
        LocalBroadcastManager.getInstance(this).registerReceiver(snapshotReciever, statusIntentFilter);

        /*
        oView = new LinearLayout(this);
        oView.setBackgroundColor(0x88ff0000); // The translucent red color

        WindowManager wm_fill = (WindowManager) getSystemService(WINDOW_SERVICE);

        Button fill_button = new Button(this);
        fill_button.setLayoutParams(params);
        fill_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //literally do nothing
                Log.v("Button", "Pressed");
            }
        });
        fill_button.setFocusable(false);
        oView.setFocusable(false);
        oView.addView(fill_button);

        wm_fill.addView(oView, params);
        */

    }


    public static void hideForScreenshot() {
        //Hides Buttons
        options.setVisibility(View.INVISIBLE);
        pause.setVisibility(View.INVISIBLE);
        submit.setVisibility(View.INVISIBLE);

        //Fills Screen
        //ViewGroup.LayoutParams params = ll.getLayoutParams();
        //ViewGroup.LayoutParams hideParams = params;
        //hideParams.height = LinearLayout.LayoutParams.MATCH_PARENT;
        //hideParams.width = LinearLayout.LayoutParams.MATCH_PARENT;

        wm.updateViewLayout(ll, fill_params);


    }

    public static void restoreAfterScreenshot() {
        //ViewGroup.LayoutParams params = ll.getLayoutParams();
        wm.updateViewLayout(ll, parameters);
        options.setVisibility(View.VISIBLE);
        pause.setVisibility(View.VISIBLE);
        submit.setVisibility(View.VISIBLE);
    }


    private void rotate(View view) {
        ObjectAnimator anim = ObjectAnimator.ofFloat(view, "rotation", 0f, 360f);
        anim.setDuration(animationTime);
        anim.start();
    }

    private void moveDown(View view, float length) {
        view.setY(options.getY());
        ObjectAnimator anim = ObjectAnimator.ofFloat(view, "translationY", length);
        anim.setDuration(animationTime);
        anim.start();
    }


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

    private void appear(final View view) {
        view.setVisibility(View.VISIBLE);
        ObjectAnimator anim = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        anim.setDuration(animationTime);
        anim.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
    }


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

    public void stopRecording(View view) {
        //Launch RecordActivity
        Globals.recording = false;
        Intent intent = new Intent(RecordFloatingWidget.this, RecordActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
        ll.setVisibility(View.GONE);
        onDestroy();
    }
}
