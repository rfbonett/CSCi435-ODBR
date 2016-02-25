package csci435.csci435_odbr;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
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

    WindowManager wm;
    LinearLayout ll;
    boolean visibility;
    //boolean toggle;

    ToggleButton options;
    Button submit;
    ToggleButton pause;

    final int animationTime = 250;
    float animationDist;
    float animationDistLong;

    final WindowManager.LayoutParams parameters = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSPARENT);

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
        pause.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sensorDataTask.togglePaused(isChecked);
            }
        });
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
