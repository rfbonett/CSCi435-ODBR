package csci435.csci435_odbr;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


/**
 * Created by Rich on 4/24/16.
 */
public class HierarchyDumpManager {
    private ExecutorService service;
    private String directory;
    private String filename;
    private int dump_index;

    public HierarchyDumpManager() {
        dump_index = 0;
        directory = "sdcard/HierarchyDumps/";
        filename = "dump" + dump_index + ".xml";
        service = Executors.newCachedThreadPool();
        File dir = new File(directory);
        if (dir.exists()) {
            for (File f : dir.listFiles()) {
                f.delete();
            }
        }
        else {
            dir.mkdir();
        }
    }

    public HierarchyDump takeHierarchyDump() {
        AccessibilityNodeInfo root = AccessibilityNodeInfo.obtain();
        dump_index += 1;
        filename = "dump" + dump_index + ".xml";
        service.submit(new HierarchyDumpTask(root, directory + filename));
        return new HierarchyDump(directory + filename);
    }
}

class HierarchyDumpTask implements Runnable {

    private String filename;
    private AccessibilityNodeInfo node;

    public HierarchyDumpTask(AccessibilityNodeInfo node, String filename) {
        this.filename = filename;
        this.node = node;
    }

    @Override
    public void run() {
        try {
            AccessibilityNodeInfoDumper.dumpWindowToFile(node, new File(filename));
        } catch (Exception e) {
            Log.e("HierarchyDumpTask", "Error taking hierarchy dump.");
        }
    }

}

class HierarchyDump {

    private String filename;

    public HierarchyDump(String filename) {
        this.filename = filename;
    }


    public String getFilename() {
        return filename;
    }


    /**
     * Returns the deepest view containing the coordinates as a Node
     * @param x
     * @param y
     * @return
     */
    public Node getViewAtCoordinates(int x, int y) {
        Node node = null;
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document document = docBuilder.parse(new File(filename));
            node = getNodeBetweenBounds(document.getDocumentElement(), x, y);
        } catch (Exception e) {}
        return node;
    }


    private Node getNodeBetweenBounds(Node node, int x, int y) {
        boolean childBetweenBounds = true;
        while (childBetweenBounds) {
            childBetweenBounds = false;
            NodeList nodeList = node.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                if (isBetweenBounds(x, y, getBounds(nodeList.item(i)))) {
                    childBetweenBounds = true;
                    node = nodeList.item(i);
                    break;
                }
            }
        }
        return node;
    }


    private int[] getBounds(Node node) {
        String[] boundsAttr = (((Element) node).getAttribute("bounds")).replace("[","").split("[^0-9]");
        int[] bounds = new int[boundsAttr.length];
        for (int i = 0; i < boundsAttr.length; i++) {
            bounds[i] = Integer.parseInt(boundsAttr[i]);
        }
        return bounds;
    }


    private boolean isBetweenBounds(int x, int y, int[] bounds) {
        return x >= bounds[0] && x <= bounds[2] && y >= bounds[1] && y <= bounds[3];
    }
}