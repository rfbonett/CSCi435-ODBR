package csci435.csci435_odbr;

import java.io.OutputStream;

/**
 * Created by Rich on 4/7/16.
 */
public class HierarchyDump {

    private String filename;

    public HierarchyDump(String filename) {
        this.filename = filename;
        try {
            Process process = Runtime.getRuntime().exec("su", null, null);
            OutputStream os = process.getOutputStream();
            os.write(("system/bin/uiautomator dump " + filename).getBytes("ASCII"));
        } catch (Exception e) {}
    }

    public String getFilename() {
        return filename;
    }


}
