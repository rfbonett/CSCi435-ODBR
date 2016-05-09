package csci435.csci435_odbr;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.support.v4.view.ViewPager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentManager;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.hardware.Sensor;
import android.graphics.Canvas;
import android.widget.ToggleButton;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Review Activity provides the user with a summary of their input events
 * --The summary is provided as a list of screenshots and brief summaries
 * --The user can navigate between these by swiping left(next) or right(prior)
 */
public class ReviewActivity extends FragmentActivity {

    private ViewPager viewPager;
    private ToggleButton sensorDataButton;
    private ToggleButton userEventsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_review_report);
        //Sets the title and image
        TextView appName = (TextView) findViewById(R.id.appName);
        appName.setText("Bug Report for " + Globals.appName);

        try {
            PackageManager pm = getPackageManager();
            Drawable icon = pm.getApplicationIcon(Globals.packageName);
            ImageView iv = (ImageView) findViewById(R.id.appIcon);
            iv.setImageDrawable(icon);
        } catch (PackageManager.NameNotFoundException e) {Log.e("ReviewActivity", "Package Name Not Found");}

        Point displaySize = new Point();
        getWindowManager().getDefaultDisplay().getRealSize(displaySize);
        Globals.width = displaySize.x;
        Globals.height = displaySize.y;

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        sensorDataButton = (ToggleButton) findViewById(R.id.sensorDataButton);
        userEventsButton = (ToggleButton) findViewById(R.id.userEventsButton);
        displaySensorData(sensorDataButton);

        int titleHeight = findViewById(R.id.titleLayout).getHeight();
        int toggleHeight = findViewById(R.id.pageToggle).getHeight();
        int buttonHeight = findViewById(R.id.submitButton).getHeight();

        Globals.availableHeightForImage = Globals.height - (titleHeight + toggleHeight + buttonHeight);

    }


    public void displaySensorData(View v) {
        viewPager.setAdapter(new SensorDataPageAdapter(getSupportFragmentManager()));
        userEventsButton.setChecked(false);
        sensorDataButton.setChecked(true);
    }


    public void displayUserEvents(View v) {
        viewPager.setAdapter(new UserEventPageAdapter(getSupportFragmentManager()));
        sensorDataButton.setChecked(false);
        userEventsButton.setChecked(true);
    }

    public void returnToRecordActivity(View view) {
        Intent intent = new Intent(this, RecordActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
        finish();
    }


    class UserEventPageAdapter extends FragmentStatePagerAdapter {

        private int count;

        public UserEventPageAdapter(FragmentManager manager) {
            super(manager);
            count = BugReport.getInstance().numEvents();
        }

        @Override
        public Fragment getItem(int position) {
            Fragment page = new UserEventFragment();
            Bundle args = new Bundle();
            args.putInt(UserEventFragment.ARG_OBJECT, position);
            page.setArguments(args);
            return page;
        }

        @Override
        public int getCount() {
            return count;
        }
    }

    public static class UserEventFragment extends Fragment {
        public static final String ARG_OBJECT = "object";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            int pos = getArguments().getInt(ARG_OBJECT);
            int max = BugReport.getInstance().numEvents();

            View rootView = inflater.inflate(R.layout.user_event_fragment_layout, container, false);
            TextView eventDescription = (TextView) rootView.findViewById(R.id.userEventDescription);

            ReportEvent e = BugReport.getInstance().getEventAtIndex(pos);
            eventDescription.setText("(" + (pos + 1) + "/" + max + ") " + e.getEventDescription());

            ImageView screenshot = (ImageView) rootView.findViewById(R.id.screenshot);
            Bitmap screenBitmap = e.getScreenshot().getBitmap();

            if (screenBitmap != null) {

                Canvas c = new Canvas(screenBitmap);
                Paint color = new Paint();
                color.setStyle(Paint.Style.STROKE);
                color.setStrokeWidth(5);

                SparseArray<ArrayList<int[]>> traces = BugReport.getInstance().getEventAtIndex(pos).getInputCoordinates();
                for (int trace = 0; trace < traces.size(); trace++) {
                    ArrayList<int[]> coords = traces.valueAt(trace);
                    color.setColor(BugReport.colors[trace % BugReport.colors.length]);


                    int x = scaleX(coords.get(0)[0]);
                    int y = scaleY(coords.get(0)[1]);
                    c.drawCircle(x, y, 10, color);
                    x = scaleX(coords.get(coords.size() - 1)[0]);
                    y = scaleY(coords.get(coords.size() - 1)[1]);
                    c.drawCircle(x, y, 10, color);
                    for (int i = 1; i < coords.size() - 1; i++) {
                        int xStart = scaleX(coords.get(i)[0]);
                        int yStart = scaleY(coords.get(i)[1]);
                        int xEnd = scaleX(coords.get(i + 1)[0]);
                        int yEnd = scaleY(coords.get(i + 1)[1]);
                        c.drawLine(xStart, yStart, xEnd, yEnd, color);
                    }
                }

                int scaledWidth = (int) (screenBitmap.getWidth() * ((float) Globals.availableHeightForImage / (float) screenBitmap.getHeight()));
                Bitmap bScaled = Bitmap.createScaledBitmap(screenBitmap, scaledWidth, Globals.availableHeightForImage, true);
                screenshot.setImageBitmap(bScaled);
            }
            return rootView;
        }

        private int scaleX(int val) {
            return (int) ((float) val * (float) Globals.width / (float) GetEventDeviceInfo.getInstance().getMaxX());
        }

        private int scaleY(int val) {
            return (int) ((float) val * (float) Globals.height / (float) GetEventDeviceInfo.getInstance().getMaxY());
        }

    }


    class SensorDataPageAdapter extends FragmentStatePagerAdapter {

        private int count;

        public SensorDataPageAdapter(FragmentManager manager) {
            super(manager);
            count = Globals.sensors.size();
        }

        @Override
        public Fragment getItem(int position) {
            Fragment page = new SensorDataFragment();
            Bundle args = new Bundle();
            args.putInt(SensorDataFragment.ARG_OBJECT, position);
            page.setArguments(args);
            return page;
        }

        @Override
        public int getCount() {
            return count;
        }
    }


    public static class SensorDataFragment extends Fragment {
        public static final String ARG_OBJECT = "object";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            int pos = getArguments().getInt(ARG_OBJECT);
            int max = Globals.sensors.size();
            Sensor s = Globals.sensors.get(pos);
            View rootView = inflater.inflate(R.layout.sensor_data_fragment_layout, container, false);
            TextView sensorTitle = (TextView) rootView.findViewById(R.id.sensorTitle);
            if (s != null) {
                sensorTitle.setText("(" + (pos + 1) + "/" + max + ")  " + s.getName());
                ImageView sensorGraph = (ImageView) rootView.findViewById(R.id.sensorGraph);
                sensorGraph.setImageBitmap(BugReport.getInstance().drawSensorData(s));
            }
            return rootView;
        }
    }

    public static void getGUIEventFromRawEvent(String rawEvent) {

        String[] data = rawEvent.split("#");
        int eventTypeId = Integer.parseInt(data[0]);
        String eventLabel = data[1];
        double duration = Double.parseDouble(data[3]);
        String[] initPosition = data[4].replace("(", "").replace(")", "").split(",");
        String[] finalPosition = data[5].replace("(", "").replace(")", "").split(",");

        String direction = getSwipeText(eventTypeId, Integer.parseInt(initPosition[0].trim()),
                Integer.parseInt(initPosition[1].trim()), Integer.parseInt(finalPosition[0].trim()),
                Integer.parseInt(finalPosition[1].trim()));
    }

    private static String getSwipeText(int eventTypeId, int initPositionX, int initPositionY, int finalPositionX,
                                       int finalPositionY) {
        String direction = "";
        if (eventTypeId == 2) {
            int diffX = initPositionX - finalPositionX;
            int diffY = initPositionY - finalPositionY;

            String vertical = "UP";
            String horizontal = "LEFT";

            if (diffX > 0) {
                vertical = "DOWN";
            }
            if (diffY < 0) {
                horizontal = "RIGHT";
            }

            if (Math.abs(diffX) > Math.abs(diffY)) {
                direction = horizontal + "-" + vertical;
            } else {
                direction = vertical + "-" + horizontal;
            }
        }
        return direction;
    }

}

