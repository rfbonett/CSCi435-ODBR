package csci435.csci435_odbr;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * Created by Brendan Otten on 2/17/2016.
 * Resource: https://www.youtube.com/watch?v=kjGPE_XLmwg
 */
public class FloatingWindow extends Service {

    private WindowManager wm;
    private LinearLayout ll;
    private Button record;
    private Button stop;


    @Override
    public IBinder onBind(Intent intent){

        return null;
    }

    @Override
    public void onCreate(){

        super.onCreate();

        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        ll = new LinearLayout(this);
        record = new Button(this);
        stop = new Button(this);

        //Create buttons to have tasks that start the async task



        //Give stop button action that closes the window and the program that was launched. Should also stop reporting events

        ViewGroup.LayoutParams buttonParameters = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        record.setText("Start");
        stop.setText("Stop");

        record.setLayoutParams(buttonParameters);
        stop.setLayoutParams(buttonParameters);


        LinearLayout.LayoutParams llParameters = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        ll.setBackgroundColor(Color.argb(66, 0, 255, 0));
        ll.setLayoutParams(llParameters);

        final WindowManager.LayoutParams parameters = new WindowManager.LayoutParams(400, 150, WindowManager.LayoutParams.TYPE_PHONE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        parameters.x = 0;
        parameters.y = 0;
        parameters.gravity = Gravity.CENTER;

        //Create two buttons in the view, one for start/pause and one for stop
        ll.addView(record);
        ll.addView(stop);
        wm.addView(ll, parameters);




        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Fuck handlers

                //Toast.makeText(getBaseContext(), "Service Started", Toast.LENGTH_LONG).show();
                //new DataCollectionTask().execute("");

                //Return accessibility node object

            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wm.removeView(ll);

                Intent intent = new Intent(getBaseContext(), RecordActivity.class);
                startActivity(intent);
                //Stop accessibility service
                stopSelf();
            }
        });





    }
}
