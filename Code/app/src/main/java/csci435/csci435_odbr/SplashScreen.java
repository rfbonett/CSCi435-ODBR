package csci435.csci435_odbr;
import java.io.OutputStream;

import android.media.Image;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.widget.ImageView;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;

/**
 * The Splash Screen is simply something nice to look at while the app configures itself.
 */
public class SplashScreen extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this, LaunchAppActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
        finish();
    }

}
