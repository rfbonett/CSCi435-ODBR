package csci435.csci435_odbr;

import org.w3c.dom.Node;

import java.util.ArrayList;

/**
 * Created by Rich on 4/22/16.
 */
public class ReportEvent {
    private long timestamp;
    private long duration = 0;
    private long timeUntilNextEvent;
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

    public void setType(int t) {
        type = t;
    }

    public void setTime(long t) {
        timestamp = t;
    }

    public void setDuration(long d) {
        duration = d;
    }

    public void setWaitTime(long t) {
        timeUntilNextEvent = t;
    }

    public String getData() {
        return "Time: " + timestamp + " | Screenshot: " + screenShot.getFilename() + " | Dump: " + dump.getFilename();
    }

    public String getEventDescription() {
        String desc = "";
        if (inputs.size() < 3) {
            int[] coords = inputs.get(0);
            Node view = dump.getViewAtCoordinates(coords[0], coords[1]);
            desc += "User clicked ";
            desc += getMeaningfulDescription(view);
            desc += " at X:" + coords[0] + " |Y: " + coords[1];
        }
        else {
            int[] start = inputs.get(0);
            int[] end = inputs.get(inputs.size() - 1);
            desc += "User swiped from X: " + start[0] + " |Y: " + start[1] + " | to X: " + end[0] + " |Y: " + end[1];
        }
        return desc;
    }

    private String getMeaningfulDescription(Node view) {
        if (view != null) {
            return view.getNodeName();
        }
        return "widget";
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


    public long getTime() {
        return timestamp;
    }

    public long getDuration() {
        return duration;
    }

    public long getWaitTime() {
        return timeUntilNextEvent;
    }

    public String getHierarchy() {
        return dump.getFilename();
    }
}
