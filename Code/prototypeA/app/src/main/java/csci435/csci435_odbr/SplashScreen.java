package csci435.csci435_odbr;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.content.Intent;

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
