package csci435.csci435_odbr;

import java.util.List;

import android.os.AsyncTask;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.view.View;
import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

/**
 * Created by Rich on 2/11/16.
 */
public class DataCollectionTask extends AsyncTask<String, Void, Void> {

    protected Void doInBackground(String... s) {

        return null;
    }

    private Bitmap takeScreenshot(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(),
                view.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

}
