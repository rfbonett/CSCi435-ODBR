package csci435.csci435_odbr;


import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.content.pm.PackageManager;
import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;
import android.widget.AdapterView;
import android.widget.ListView;
import android.app.AlertDialog;
import android.app.Activity;
import android.content.DialogInterface;
import android.widget.RelativeLayout;
import android.content.Intent;

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

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch_app);

        ArrayList<RowData> installedApps = new ArrayList<RowData>();
        PackageManager pm = getPackageManager();

        //Create a List of all installed applications
        List<ApplicationInfo> allApps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo app : allApps) {
            try {
                if (pm.getLaunchIntentForPackage(app.packageName) != null) {
                    String appName = (String) app.loadLabel(pm);
                    Drawable icon = pm.getApplicationIcon(app.packageName);
                    installedApps.add(new RowData(icon, appName));
                }
            } catch (PackageManager.NameNotFoundException e) {}
        }
        //Sort the list of installed applications before displaying
        Collections.sort(installedApps, new Comparator<RowData>() {
            public int compare(RowData app1, RowData app2) {
                return app1.getTitle().compareTo(app2.getTitle());
            }
        });

        //Add the list of installed applications to the ListView
        ListView lv = (ListView) findViewById(R.id.installedAppsListView);
        CustomAdapter adapter = new CustomAdapter(this, installedApps);
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                promptStart((RelativeLayout) view);
            }
        });
    }

    /**
     * promptStart launches a dialog to confirm the selected application for the report
     */
    private void promptStart(RelativeLayout data) {
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
        //Find the applicationInfo object for the given application name
        PackageManager pm = getPackageManager();
        for (ApplicationInfo app : pm.getInstalledApplications(PackageManager.GET_META_DATA)) {
            if (app.loadLabel(pm).equals(appName)) {
                Globals.appName = appName;
            }
        }
        //Launch RecordActivity
        Intent intent = new Intent(this, RecordActivity.class);
        startActivity(intent);
        finish();
    }
}

/**
 * A CustomAdapter is an adapter for a ListView that displays an icon and name for each element
 */
class CustomAdapter extends ArrayAdapter<RowData> {

    private final Context context;
    private final ArrayList<RowData> elements;

    public CustomAdapter(Context context, ArrayList<RowData> elements) {

        super(context, R.layout.row_view, elements);

        this.context = context;
        this.elements = elements;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(this.context);
        View rowView = inflater.inflate(R.layout.row_view, parent, false);

        ImageView imgView = (ImageView) rowView.findViewById(R.id.item_icon);
        TextView titleView = (TextView) rowView.findViewById(R.id.item_title);

        imgView.setImageDrawable(elements.get(position).getIcon());
        titleView.setText(elements.get(position).getTitle());

        return rowView;
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