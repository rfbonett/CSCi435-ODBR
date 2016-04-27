package csci435.csci435_odbr;

import org.w3c.dom.Node;

import java.util.ArrayList;

/**
 * Created by Rich on 4/22/16.
 */
public class ReportEvent {
    private long timestamp;
    private int type;
    private Screenshot screenShot;
    private HierarchyDump dump;
    private ArrayList<int[]> inputs; //[time, x, y]

    public ReportEvent(long time) {
        timestamp = time;
        inputs = new ArrayList<int[]>();
    }

    public void addInputEvents(int[] coords) {
        inputs.add(coords);
    }

    public void addScreenshot(Screenshot s) {
        screenShot = s;
    }

    public void addHierarchyDump(HierarchyDump d) {
        dump = d;
    }

    public String getData() {
        return "Time: " + timestamp + " | Screenshot: " + screenShot.getFilename() + " | Dump: " + dump.getFilename();
    }

    public Screenshot getScreenshot() {
        return screenShot;
    }

    public int getType() {
        return type;
    }

    public ArrayList<int[]> getInputEvents() {
        return inputs;
    }

    public String getWidgetDescription() {
        Node widget = dump.getViewAtCoordinates(inputs.get(0)[0], inputs.get(0)[1]);
        return widget.getNodeName(); //TODO improve description
    }
}
