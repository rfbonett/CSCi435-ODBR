package csci435.csci435_odbr;

import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;

import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by Rich on 4/22/16.
 */
public class ReportEvent {
    short EV_ABS = 3;

    private long timeUntilNextEvent;
    private Screenshot screenShot;
    private HierarchyDump dump;
    private ArrayList<GetEvent> inputs; //[time, x, y]
    private String device;
    int idNum;

    public ReportEvent(String device) {
        this.device = device;
        inputs = new ArrayList<GetEvent>();
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

    /**
     * Retrieve a list of input traces
     * @return a list of lists, where each list contains the coordinates for one touch input
     */
    public SparseArray<ArrayList<int[]>> getInputCoordinates() {
        SparseArray<ArrayList<int[]>> traces = new SparseArray<ArrayList<int[]>>();
        SparseArray<int[]> coords = new SparseArray<int[]>();
        SparseIntArray slots = new SparseIntArray();
        int activeSlot = 0;

        int NOT_FOUND = -1;
        int CLEAN = 0;
        int DIRTY = 1;

        coords.put(activeSlot, new int[] {-1, -1});
        traces.put(activeSlot, new ArrayList<int[]>());

        if(GetEventDeviceInfo.getInstance().isTypeSingleTouch()){
            activeSlot = 0;
            if (slots.get(activeSlot, NOT_FOUND) == NOT_FOUND) {
                coords.put(activeSlot, new int[] {-1, -1});
                traces.put(activeSlot, new ArrayList<int[]>());
            }
        }

        for (GetEvent e : inputs) {
            if (slot(e)) {
                activeSlot = e.getValue();
                if (slots.get(activeSlot, NOT_FOUND) == NOT_FOUND) {
                    coords.put(activeSlot, new int[] {-1, -1});
                    traces.put(activeSlot, new ArrayList<int[]>());
                }
            }
            if (xPos(e)) {
                coords.get(activeSlot)[0] = e.getValue();
                slots.put(activeSlot, DIRTY);
            }
            else if (yPos(e)) {
                coords.get(activeSlot)[1] = e.getValue();
                traces.get(activeSlot).add(coords.get(activeSlot).clone());
                slots.put(activeSlot, CLEAN);
            }
            else if (slots.get(activeSlot) == DIRTY) {
                traces.get(activeSlot).add(coords.get(activeSlot).clone());
                slots.put(activeSlot, CLEAN);
            }
        }
        
        return traces;
    }

    private boolean slot(GetEvent e) {
        try{
            return e.getType() == EV_ABS && (e.getCode() == GetEventDeviceInfo.getInstance().get_code("ABS_MT_SLOT"));
        } catch (Exception exc) {return false;}
    }

    private boolean xPos(GetEvent e) {
        try {
            return e.getType() == EV_ABS && (e.getCode() == GetEventDeviceInfo.getInstance().get_code("ABS_MT_POSITION_X") || e.getCode() == GetEventDeviceInfo.getInstance().get_code("ABS_X"));
        } catch (Exception exc) {return false;}
    }

    private boolean yPos(GetEvent e) {
        try {
            return e.getType() == EV_ABS && (e.getCode() == GetEventDeviceInfo.getInstance().get_code("ABS_MT_POSITION_Y") || e.getCode() == GetEventDeviceInfo.getInstance().get_code("ABS_Y"));
        } catch (Exception exc) {return false;}
    }
}
