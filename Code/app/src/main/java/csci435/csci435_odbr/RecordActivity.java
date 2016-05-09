package csci435.csci435_odbr;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.view.View;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.EditText;

import java.io.OutputStream;


/**
 * RecordActivity handles the visual recording and submitting the bug report
 */
public class RecordActivity extends ActionBarActivity {
    private EditText reporterName;
    private EditText reportTitle;
    private EditText desiredOutcome;
    private EditText actualOutcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_record);

        //Get handles to edit texts
        reporterName = (EditText) findViewById(R.id.reporterNameEditText);
        reportTitle = (EditText) findViewById(R.id.reportTitleEditText);
        desiredOutcome = (EditText) findViewById(R.id.desiredOutcomeEditText);
        actualOutcome = (EditText) findViewById(R.id.actualOutcomeEditText);

        fillDescriptions();

        //Set the image and title to the application icon and name
        TextView appName = (TextView) findViewById(R.id.appName);
        appName.setText("Bug Report for " + Globals.appName);

        try {
            PackageManager pm = getPackageManager();
            Drawable icon = pm.getApplicationIcon(Globals.packageName);
            ImageView iv = (ImageView) findViewById(R.id.appIcon);
            iv.setImageDrawable(icon);
        } catch (PackageManager.NameNotFoundException e) {}

        //Set up return key to change focus for multiline edit texts
        desiredOutcome.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    actualOutcome.requestFocus();
                    return true;
                }
                return false;
            }
        });
        actualOutcome.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(actualOutcome.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    findViewById(R.id.recordLayout).requestFocus();
                    return true;
                }
                return false;
            }
        });
    }


    /**
     * Updates the BugReport with the contents of the EditTexts
     * --Report Title
     * --Reporter Name
     * --"What should happen"
     * --"What does happen"
     */
    private void updateBugReport() {
        BugReport.getInstance().setReporterName(reporterName.getText().toString());
        BugReport.getInstance().setTitle(reportTitle.getText().toString());
        BugReport.getInstance().setDesiredOutcome(desiredOutcome.getText().toString());
        BugReport.getInstance().setActualOutcome(actualOutcome.getText().toString());
    }

    private void fillDescriptions(){
        reporterName.setText(BugReport.getInstance().getReporterName());
        reportTitle.setText(BugReport.getInstance().getTitle());
        desiredOutcome.setText(BugReport.getInstance().getDesiredOutcome());
        actualOutcome.setText(BugReport.getInstance().getActualOutcome());
    }

    /**
     * Hands off to the ReviewActivity to review the report
     * @param view
     */
    public void reviewReport(View view) {
        updateBugReport();
        Intent intent = new Intent(this, ReviewActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
        finish();
    }

    public void replayReport(View view){
        //method to be called by review report, we're going to have to start a service, that runs and then
        //also start the package.name app, then use the service to start inputting commands
        updateBugReport();

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

        } catch (Exception e){Log.e("RecordActivity", "Error clearing stored app data");}

        Intent intent = new Intent(this, ReplayService.class);
        startService(intent);
        Intent reportApp = getPackageManager().getLaunchIntentForPackage(Globals.packageName);
        reportApp.addCategory(Intent.CATEGORY_LAUNCHER);
        reportApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        reportApp.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(reportApp);
    }


    /**
     * Submits the report to the server, relaunches application
     * @param view
     */
    public void submitReport(View view) throws Exception {
        updateBugReport();

        //JSON model tester
        new JsonModel().tester();

        BugReport.getInstance().clearReport();
        Intent intent = new Intent(this, LaunchAppActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
        finish();
    }
}
