package rfbonett.wireframe;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;


public class MainActivity extends Activity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        ArrayList<RowData> installedApps = new ArrayList<RowData>();
        for (int i = 0; i < 10; i++) {
            installedApps.add(new RowData());
        }

        ListView lv = (ListView) findViewById(R.id.installedAppsListView);
        final CustomAdapter adapter = new CustomAdapter(this, installedApps);
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
        prompt.setTitle("Start Recording?");
        prompt.setPositiveButton("Start", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Intent reportApp = new Intent(MainActivity.this, PlaceholderReportAppActivity.class);
                reportApp.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(reportApp);
            }
        });
        prompt.setNegativeButton("Cancel", null);
        prompt.show();
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
        return inflater.inflate(R.layout.row_view, parent, false);
    }
}


/**
 * RowData serves as a storage class for an application's icon and name
 */
class RowData{}
