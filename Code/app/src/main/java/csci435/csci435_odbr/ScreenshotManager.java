package csci435.csci435_odbr;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by Rich on 4/23/16.
 *
 * The ScreenshotManager provides functionality for taking a screenshot. To take a screenshot,
 * simply call takeScreenshot() and receive a Screenshot Object (below)
 */
public class ScreenshotManager {
    private ExecutorService service;
    private Future currentTask;
    private String directory;
    private String filename;
    private int screenshot_index;

    public ScreenshotManager() {
        screenshot_index = 0;
        directory = "sdcard/Screenshots/";
        filename = "screenshot" + screenshot_index + ".png";
        service = Executors.newSingleThreadExecutor();
        File dir = new File(directory);
        dir.mkdir();
    }

    /**
     * If already busy processing a Screenshot, returns a reference to the in process Screenshot.
     * Otherwise, starts to process a new Screenshot, returning a reference to this new one.
     * @return Most recently processed Screenshot object
     */
    public Screenshot takeScreenshot() {
        if (currentTask == null || currentTask.isDone()) {
            screenshot_index += 1;
            filename = "screenshot" + screenshot_index + ".png";
            currentTask = service.submit(new ScreenshotTask(directory + filename));
        }
        return new Screenshot(directory + filename);
    }
}

class ScreenshotTask implements Runnable {

    private Process process;
    private String filename;

    public ScreenshotTask(String filename) {
        this.filename = filename;
        try {
            process = Runtime.getRuntime().exec("su", null, null);
        } catch (Exception e) {Log.e("ScreenshotTask", "Could not start process! Check su permissions.");}
    }

    @Override
    public void run() {
        OutputStream os = process.getOutputStream();
        try {
            os.write(("/system/bin/screencap -p " + filename + "\n").getBytes("ASCII"));
            os.flush();
            os.write(("exit\n").getBytes("ASCII"));
            os.flush();
            os.close();
            process.waitFor();
        } catch (Exception e) {
            Log.e("ScreenshotTask", "Error taking screenshot.");
        }
    }

}

/**
 * A Screenshot object consists of a filename and functionality to access the image
 */
class Screenshot {
    private String filename;

    public Screenshot(String saveFile) {
        filename = saveFile;
    }


    public Bitmap getBitmap() {
        File screenshotFile = new File(filename);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return BitmapFactory.decodeFile(screenshotFile.getAbsolutePath(), options);
    }


    public String getFilename() {
        return filename;
    }


}