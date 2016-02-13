package csci435.csci435_odbr;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.widget.TextView;
import android.view.View;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;


public class RecordActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_record_state);
        TextView appName = (TextView) findViewById(R.id.appName);
        appName.setText("Bug Report for " + Globals.appName);

        try {
            PackageManager pm = getPackageManager();
            Drawable icon = pm.getApplicationIcon(Globals.packageName);
            ImageView iv = (ImageView) findViewById(R.id.appIcon);
            iv.setImageDrawable(icon);
        } catch (PackageManager.NameNotFoundException e) {}


        Intent reportApp = getPackageManager().getLaunchIntentForPackage(Globals.packageName);
        reportApp.addCategory(Intent.CATEGORY_LAUNCHER);
        reportApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(reportApp);
    }

    public void stopRecording(View view) {
        Intent intent = new Intent(this, SubmitReportActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
        finish();
    }

    public void submitBenchmark(View view) {

    }
}
