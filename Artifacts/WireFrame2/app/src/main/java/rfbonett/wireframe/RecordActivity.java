package rfbonett.wireframe;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class RecordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
    }

    public void reviewReport(View v) {
        startActivity(new Intent(this, ReviewActivity.class));
    }

    public void submitReport(View v) {
        finish();
    }
}
