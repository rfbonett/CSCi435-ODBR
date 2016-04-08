package csci435.csci435_odbr;

import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.OutputStream;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by Rich on 4/7/16.
 */
public class HierarchyDump {

    private String filename;

    public HierarchyDump(String filename) {
        this.filename = filename;
    }


    public void dumpToFile() {
        try {
            Process process = Runtime.getRuntime().exec("su", null, null);
            OutputStream os = process.getOutputStream();
            os.write(("system/bin/uiautomator dump " + filename).getBytes("ASCII"));
        } catch (Exception e) {}
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
