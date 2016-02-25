package csci435.csci435_odbr;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v4.view.ViewPager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentManager;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.hardware.Sensor;
import android.graphics.Canvas;

/**
 * Review Activity provides the user with a summary of their input events
 * --The summary is provided as a list of screenshots and brief summaries
 * --The user can navigate between these by swiping left(next) or right(prior)
 */
public class ReviewActivity extends FragmentActivity {

    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        Globals.width = (getResources().getConfiguration().screenWidthDp * 2) - 24;
        Globals.height = getResources().getConfiguration().screenHeightDp;
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setAdapter(new CustomPageAdapter(getSupportFragmentManager()));

        //Sets the initial review screenshot
        /*
        ImageView image = (ImageView) findViewById(R.id.screenshot);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile("/sdcard/img.png", options);
        image.setImageBitmap(bitmap); */
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


    class CustomPageAdapter extends FragmentStatePagerAdapter {

        private int count;

        public CustomPageAdapter(FragmentManager manager) {
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


    // Instances of this class are fragments representing a single
    // object in our collection.
    public static class SensorDataFragment extends Fragment {
        public static final String ARG_OBJECT = "object";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            Sensor s = BugReport.getInstance().getSensor(getArguments().getInt(ARG_OBJECT));
            View rootView = inflater.inflate(R.layout.sensor_data_fragment_layout, container, false);
            TextView sensorTitle = (TextView) rootView.findViewById(R.id.sensorTitle);
            sensorTitle.setText(s.getName());

            ImageView sensorGraph = (ImageView) rootView.findViewById(R.id.sensorGraph);
            sensorGraph.setImageBitmap(BugReport.getInstance().drawSensorData(s));
            return rootView;
        }
    }
}


