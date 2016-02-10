package csci435.csci435_odbr;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.widget.TextView;
import android.view.View;

public class EndRecordState extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_record_state);
        Intent intent = getIntent();
        TextView appName = (TextView) findViewById(R.id.appName);
        appName.setText(intent.getStringExtra("APP_NAME"));
    }

    public void stopRecording(View view) {
        Intent intent = new Intent(this, EndRecordState.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
    }
}
