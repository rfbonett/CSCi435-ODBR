package csci435.csci435_odbr;

import android.util.Log;

import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Rich on 4/22/16.
 */
public class ReportEvent {
    short EV_ABS = 3;
    Integer ABS_X;
    Integer ABS_Y;
    Integer ABS_MT_POSITION_X;
    Integer ABS_MT_POSITION_Y;
    Integer ABS_MT_TRACKING_ID;
    Integer ABS_MT_PRESSURE;

    private long timeUntilNextEvent;
    private Screenshot screenShot;
    private HierarchyDump dump;
    private ArrayList<GetEvent> inputs; //[time, x, y]
    private String device;
    int idNum;

    public ReportEvent(String device, HashMap<String, Integer> hashmap) {
        this.device = device;
        inputs = new ArrayList<GetEvent>();
        setAbsValues(hashmap);
    }

    public void addScreenshot(Screenshot s) {
        screenShot = s;
    }

    public void addHierarchyDump(HierarchyDump d) {
        dump = d;
    }

    public void addGetEvent(GetEvent e) {
        inputs.add(e);
    }

    public void addIDNum(int i){
        idNum = i;
    }

    public void setWaitTime(long t) {
        timeUntilNextEvent = t;
    }

    public String getData() {
        return "Time: " + getStartTime() + " | Screenshot: " + screenShot.getFilename() + " | Dump: " + dump.getFilename();
    }

    public String getEventDescription() {
        String desc = "";
        /*
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
        } */
        return desc;
    }

    private String getMeaningfulDescription(Node view) {
        if (view != null) {
            /*for (int i = 0; i < view.getAttributes().getLength(); i++) {
                Log.v("ReportEvent", view.getAttributes().item(i).getTextContent());
            }
            String s;
            if ("".equals(s = view.getAttributes().getNamedItem("class").toString())) {
                return s;
            } */
            return view.getNodeName();
        }
        return "widget";
    }

    private void setAbsValues(HashMap<String, Integer> hashmap){
        ABS_MT_POSITION_X = hashmap.get("ABS_MT_POSITION_X");
        ABS_MT_POSITION_Y = hashmap.get("ABS_MT_POSITION_Y");
        ABS_MT_TRACKING_ID = hashmap.get("ABS_MT_TRACKING_ID");
        ABS_MT_PRESSURE = hashmap.get("ABS_MT_PRESSURE");
        ABS_X = hashmap.get("ABS_X");
        ABS_Y = hashmap.get("ABS_Y");

    }

    public Screenshot getScreenshot() {
        return screenShot;
    }

    public HierarchyDump getHierarchy() {
        return dump;
    }


    public ArrayList<GetEvent> getInputEvents() {
        return inputs;
    }


    public long getStartTime() {
        return inputs.get(0).getTimeMillis();
    }

    public long getWaitTime() {
        return timeUntilNextEvent;
    }

    public long getDuration() {
        return inputs.get(inputs.size() - 1).getTimeMillis() - getStartTime();
    }

    public String getDevice() {
        return device;
    }

    public ArrayList<int[]> getInputCoordinates() {
        ArrayList<int[]> coords = new ArrayList<int[]>();
        ArrayList<Integer> xCoords = new ArrayList<Integer>();
        ArrayList<Integer> yCoords = new ArrayList<Integer>();
        for (GetEvent e : inputs) {
            if (xPos(e)) {
                xCoords.add(e.getValue());
            }
            else if (yPos(e)) {
                yCoords.add(e.getValue());
            }
        }

        for (int i = 0; i < xCoords.size() && i < yCoords.size(); i++) {
            coords.add(new int[] {xCoords.get(i), yCoords.get(i)});
        }
        return  coords;
    }

    private boolean xPos(GetEvent e) {
        return e.getType() == EV_ABS && (e.getCode() == ABS_X || e.getCode() == ABS_MT_POSITION_X);
    }

    private boolean yPos(GetEvent e) {
        return e.getType() == EV_ABS && (e.getCode() == ABS_Y || e.getCode() == ABS_MT_POSITION_Y);
    }
}
