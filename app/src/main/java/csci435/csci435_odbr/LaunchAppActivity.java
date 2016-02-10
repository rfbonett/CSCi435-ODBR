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
                String appName = (String) app.loadLabel(pm);
                Drawable icon = pm.getApplicationIcon(app.packageName);
                installedApps.add(new RowData(icon, appName));
            } catch (PackageManager.NameNotFoundException e) {}
        }
        //Sort the list of installed applications before displaying
        Collections.sort(installedApps, new Comparator<RowData>() {
            public int compare(RowData app1, RowData app2) {
                return app1.getTitle().compareTo(app2.getTitle());
            }
        });

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

    private void startRecording(String appName) {
        Intent intent = new Intent(this, EndRecordState.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.putExtra("APP_NAME", appName);
        startActivity(intent);
    }
}

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