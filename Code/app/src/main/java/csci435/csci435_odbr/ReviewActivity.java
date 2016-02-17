package csci435.csci435_odbr;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Review Activity provides the user with a summary of their input events
 * --The summary is provided as a list of screenshots and brief summaries
 * --The user can navigate between these by swiping left(next) or right(prior)
 */
public class ReviewActivity extends FragmentActivity {

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

        //Sets the initial review screenshot
        ImageView image = (ImageView) findViewById(R.id.screenshot);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile("/sdcard/img.png", options);
        image.setImageBitmap(bitmap);
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

}
