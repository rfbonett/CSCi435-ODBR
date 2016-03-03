package csci435.csci435_odbr;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.view.View;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.EditText;


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


    /**
     * Hands off to the ReviewActivity to review the report
     * @param view
     */
    public void reviewReport(View view) {
        updateBugReport();
        Intent intent = new Intent(this, ReviewActivity.class);
        startActivity(intent);
        finish();
    }


    /**
     * Submits the report to the server, relaunches application
     * @param view
     */
    public void submitReport(View view) {
        updateBugReport();
        BugReport.getInstance().toJSON();
        Intent intent = new Intent(this, LaunchAppActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
        finish();
    }
}
