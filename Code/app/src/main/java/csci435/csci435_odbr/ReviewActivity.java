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
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

/**
 * Review Activity provides the user with a summary of their input events
 * --The summary is provided as a list of screenshots and brief summaries
 * --The user can navigate between these by swiping left(next) or right(prior)
 */
public class ReviewActivity extends FragmentActivity {

    private ViewPager viewPager;
    private ToggleButton sensorDataButton;
    private ToggleButton userEventsButton;
    private int verticalSpacer = 360;

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

        Globals.availableHeightForImage = Globals.height - verticalSpacer;




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
            count = BugReport.getInstance().getNumEvents();
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
            int max = Globals.screenshot_index;

            //String viewDesc = BugReport.getInstance().getUserEvents().get(pos).getViewDesc();
            View rootView = inflater.inflate(R.layout.user_event_fragment_layout, container, false);
            TextView eventDescription = (TextView) rootView.findViewById(R.id.userEventDescription);
            eventDescription.setText("(" + (pos + 1) + "/" + max + ")  Interacted with " + BugReport.getInstance().getEventAtIndex(pos).getViewDescription());

            ImageView screenshot = (ImageView) rootView.findViewById(R.id.screenshot);
            //Bitmap screenBitmap = BugReport.getInstance().getScreenshotAtIndex(pos);
            //different way to get this bitmap
            //int size = BugReport.getInstance().getListSize();
            //Log.v("Screenshot", "size: "+ size);
            File sdCard = Environment.getExternalStorageDirectory();
            File directory = new File(sdCard.getAbsolutePath() + "/ScreenShots");
            Events event = BugReport.getInstance().getEventAtIndex(pos);
            Log.v("Screenshot", "filename: " + event.getFilename());
            File yourFile = new File(directory, event.getFilename());
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap screenBitmap = BitmapFactory.decodeFile(yourFile.getAbsolutePath(), options);
            //BugReport.getInstance().addScreenshot(screen); //screenshot is added here so shouldn't be a problem



            if (screenBitmap != null) {
                screenshot.setImageBitmap(screenBitmap);
            }

            Bitmap b = ((BitmapDrawable)screenshot.getDrawable()).getBitmap().copy(Bitmap.Config.ARGB_8888, true);
            int scaledWidth = (int) (b.getWidth() * ((float) Globals.availableHeightForImage / (float) b.getHeight()));
            Bitmap bScaled = Bitmap.createScaledBitmap(b, scaledWidth, Globals.availableHeightForImage, true);
            Canvas c = new Canvas(bScaled);
            Paint color = new Paint();
            color.setColor(Color.YELLOW);
            color.setStyle(Paint.Style.STROKE);
            color.setStrokeWidth(5);

            //HERE WE NEED GET_EVENT DATA

            //int[] bounds = BugReport.getInstance().getGetEvent(pos);
            //int[] point = getTransformedBoundsInScreen(bScaled.getWidth(), bScaled.getHeight(), bounds[0], bounds[1]);
            //c.drawCircle(point[0],point[1], 50, color);
            //c.drawRect(BugReport.getInstance().getEventAtIndex(pos).getScreenRect(), color);
            screenshot.setImageBitmap(bScaled);
            return rootView;
        }

        public int[] getTransformedBoundsInScreen(int bwidth, int bheight, int width, int height) {
            int[] center = new int[2];
            center[0] = width * bwidth / Globals.width;
            center[1] = height * bheight / Globals.height;
            return center;
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


