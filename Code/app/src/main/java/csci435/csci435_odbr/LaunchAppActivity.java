package csci435.csci435_odbr;


import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.view.inputmethod.InputMethodManager;
import android.view.KeyEvent;
import android.hardware.Sensor;

/**
 * LaunchAppActivity displays a list of installed applications and a search bar.
 *  --Installed applications are limited to those with a launch intent
 * The user may select an app from the list or narrow the list using the search bar.
 * Should the user select an app, a confirmation dialog will be displayed.
 *  --Upon confirmation, hand-off to RecordActivity.
 *
 *  Collaborators:
 *      --Globals: stores a handle to the application being reported
 *      --RecordActivity: launched upon selection of an app to report
 *      --CustomAdapter: Handles the display of installed applications in the list
 */
public class LaunchAppActivity extends Activity {

    private ArrayList<RowData> installedApps;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_launch_app);

        BugReport.getInstance().clearReport();

        //Create a List of all installed applications
        getInstalledApplications();
        getSensors();

        //Add the list of installed applications to the ListView
        ListView lv = (ListView) findViewById(R.id.installedAppsListView);
        final CustomAdapter adapter = new CustomAdapter(this, installedApps);
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                promptStart((RelativeLayout) view);
            }
        });

        //Add search capabilities to search EditText
        final EditText searchBar = (EditText) findViewById(R.id.searchBar);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable arg0) {
                String text = searchBar.getText().toString().toLowerCase();
                adapter.filter(text);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                                          int arg2, int arg3) {
            }

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2,
                                      int arg3) {
            }
        });

        //Close the keyboard and remove focus from searchBar on return key
        searchBar.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                adapter.filter(searchBar.getText().toString());
                if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

                    imm.hideSoftInputFromWindow(searchBar.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    searchBar.clearFocus();
                    return true;
                }
                return false;
            }
        });
    }

    private void getInstalledApplications() {
        installedApps = new ArrayList<RowData>();
        PackageManager pm = getPackageManager();
        for (ApplicationInfo app : pm.getInstalledApplications(PackageManager.GET_META_DATA)) {
            try {
                if (pm.getLaunchIntentForPackage(app.packageName) != null) {
                    String appName = (String) app.loadLabel(pm);
                    Drawable icon = pm.getApplicationIcon(app.packageName);
                    if (!(this.getPackageName().equals(app.packageName))) {
                        installedApps.add(new RowData(icon, appName));
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {}
        }

        //Sort the list of installed applications before displaying
        Collections.sort(installedApps, new Comparator<RowData>() {
            public int compare(RowData app1, RowData app2) {
                return app1.getTitle().compareTo(app2.getTitle());
            }
        });
    }

    private void getSensors() {
        SensorManager sMgr = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Globals.sMgr = sMgr;
        Globals.sensors = new ArrayList<Sensor>();
        try {
            Globals.sensors.add(sMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
            Globals.sensors.add(sMgr.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
        } catch (Exception e) {
            Log.e("LaunchAppActivity", "Could not find sensor");
        }
    }

    /**
     * promptStart launches a dialog to confirm the selected application for the report
     */
    private void promptStart(RelativeLayout data) {
        if ("".equals(((TextView) data.findViewById(R.id.item_title)).getText())) {
            return;
        }
        AlertDialog.Builder prompt = new AlertDialog.Builder(this);
        final String appName = ((TextView) data.findViewById(R.id.item_title)).getText().toString();
        Drawable icon = ((ImageView) data.findViewById(R.id.item_icon)).getDrawable();

        prompt.setTitle("Start Recording?");
        prompt.setMessage("Begin bug report for " + appName + "?");
        prompt.setIcon(icon);
        prompt.setPositiveButton("Start", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                startRecording(appName);
            }
        });
        prompt.setNegativeButton("Cancel", null);
        prompt.show();
    }


    /**
     * startRecording sets the Global application handle and launches RecordActivity
     * @param appName the name of the application
     */
    private void startRecording(String appName) {
        if ("".equals(appName)) {
            return;
        }
        //Find the applicationInfo object for the given application name
        PackageManager pm = getPackageManager();
        for (ApplicationInfo app : pm.getInstalledApplications(PackageManager.GET_META_DATA)) {
            if (app.loadLabel(pm).equals(appName)) {
                Globals.appName = appName;
                Globals.packageName = app.packageName;
            }
        }

        //launch data collection task and floating window
        Globals.recording = true;
        startService(new Intent(this, RecordFloatingWidget.class));

        //start SU process to clear the saved data within the application

        try {
            Process clear_app_data = Runtime.getRuntime().exec("su", null, null);
            String cmd = "pm clear " + Globals.packageName;
            OutputStream os = clear_app_data.getOutputStream();
            os.write((cmd + "\n").getBytes("ASCII"));
            os.flush();
            os.write(("exit\n").getBytes());
            os.flush();
            os.close();

            clear_app_data.waitFor();
            Log.v("Launch_app_activity", "data cleared");

        } catch (Exception e){}

        //Launch application to be reported
        Intent reportApp = getPackageManager().getLaunchIntentForPackage(Globals.packageName);
        reportApp.addCategory(Intent.CATEGORY_LAUNCHER);
        reportApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        reportApp.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(reportApp);
    }
}

/**
 * An InstalledApplicationsAdapter is an adapter for a ListView that displays an icon and name for each element
 */
class CustomAdapter extends BaseAdapter {

    private final Context context;
    private final ArrayList<RowData> elements;
    private final ArrayList<RowData> visibleElements;

    public CustomAdapter(Context context, ArrayList<RowData> elements) {
        this.context = context;
        this.elements = elements;
        this.visibleElements = new ArrayList<RowData>(elements);
    }

    @Override
    public int getCount() {
        return visibleElements.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(this.context);
        View rowView = inflater.inflate(R.layout.row_view, parent, false);

        if (visibleElements.size() > position) {
            ImageView imgView = (ImageView) rowView.findViewById(R.id.item_icon);
            TextView titleView = (TextView) rowView.findViewById(R.id.item_title);

            imgView.setImageDrawable(visibleElements.get(position).getIcon());
            titleView.setText(visibleElements.get(position).getTitle());
        }
        return rowView;
    }

    /**
     * Filters the list by an input prefix
     */
    public void filter(String s) {
        visibleElements.clear();
        if (s.length() == 0) {
            visibleElements.addAll(elements);
        }
        for (RowData r : elements) {
            if (r.getTitle().toLowerCase().startsWith(s)) {
                visibleElements.add(r);
            }
        }
        notifyDataSetChanged();
    }

}


/**
 * RowData serves as a storage class for an application's icon and name
 */
class RowData{
    private Drawable icon;
    private String title;

    public RowData(Drawable icon, String title) {
        super();
        this.icon = icon;
        this.title = title;
    }

    public Drawable getIcon() {
        return this.icon;
    }

    public String getTitle() {
        return this.title;
    }
}
