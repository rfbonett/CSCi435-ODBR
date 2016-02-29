package csci435.csci435_odbr;

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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.ImageView;
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
        } catch (PackageManager.NameNotFoundException e) {}

        Point displaySize = new Point();
        getWindowManager().getDefaultDisplay().getRealSize(displaySize);
        Globals.width = displaySize.x;
        Globals.height = displaySize.y;
        viewPager = (ViewPager) findViewById(R.id.viewPager);

        sensorDataButton = (ToggleButton) findViewById(R.id.sensorDataButton);
        userEventsButton = (ToggleButton) findViewById(R.id.userEventsButton);
        displaySensorData(sensorDataButton);
        /*
        Switch pageSwitch = (Switch) findViewById(R.id.pageSwitch);
        pageSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    viewPager.setAdapter(new UserEventPageAdapter(getSupportFragmentManager()));
                }
                else {
                    viewPager.setAdapter(new SensorDataPageAdapter(getSupportFragmentManager()));
                }

            }
        }); */

        //Sets the initial review screenshot
        /*
        ImageView image = (ImageView) findViewById(R.id.screenshot);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile("/sdcard/img.png", options);
        image.setImageBitmap(bitmap); */
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

    /**
     * Submits the report to the server, relaunches application
     * @param view
     */
    public void submitReport(View view) {
        BugReport.getInstance().toJSON();
        Intent intent = new Intent(this, LaunchAppActivity.class);
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
            String viewDesc = BugReport.getInstance().getUserEvents().get(pos).getViewDesc();
            View rootView = inflater.inflate(R.layout.user_event_fragment_layout, container, false);
            TextView eventDescription = (TextView) rootView.findViewById(R.id.userEventDescription);
            eventDescription.setText("(" + (pos + 1) + "/" + max + ")  Interacted with " + viewDesc);

            ImageView screenshot = (ImageView) rootView.findViewById(R.id.screenshot);
            screenshot.setImageBitmap(BugReport.getInstance().getScreenshotAtIndex(pos));
            Bitmap b = ((BitmapDrawable)screenshot.getDrawable()).getBitmap().copy(Bitmap.Config.ARGB_8888, true);;
            //Bitmap b = BugReport.getInstance().getScreenshotAtIndex(pos);
            Canvas c = new Canvas(b);
            Paint color = new Paint();
            color.setColor(Color.YELLOW);
            color.setStyle(Paint.Style.STROKE);
            color.setStrokeWidth(5);
            int[] bounds = BugReport.getInstance().getEventAtIndex(pos).getTransformedBoundsInScreen(b.getWidth(), b.getHeight());
            c.drawCircle(bounds[0], bounds[1], 60, color);
            screenshot.setImageBitmap(b);
            return rootView;
        }
    }


    class SensorDataPageAdapter extends FragmentStatePagerAdapter {

        private int count;

        public SensorDataPageAdapter(FragmentManager manager) {
            super(manager);
            count = BugReport.getInstance().numSensors();
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
            int max = BugReport.getInstance().numSensors();
            Sensor s = BugReport.getInstance().getSensor(pos);
            View rootView = inflater.inflate(R.layout.sensor_data_fragment_layout, container, false);
            TextView sensorTitle = (TextView) rootView.findViewById(R.id.sensorTitle);
            sensorTitle.setText("(" + (pos + 1) + "/" + max + ")  " + s.getName());

            ImageView sensorGraph = (ImageView) rootView.findViewById(R.id.sensorGraph);
            sensorGraph.setImageBitmap(BugReport.getInstance().drawSensorData(s));
            return rootView;
        }
    }
}


