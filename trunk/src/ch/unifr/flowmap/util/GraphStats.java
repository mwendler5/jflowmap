package ch.unifr.flowmap.util;

import ch.unifr.flowmap.util.Stats;
import prefuse.data.Graph;
import prefuse.data.Edge;
import prefuse.data.Node;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ilya Boyandin
 *         Date: 21-Sep-2009
 */
public class GraphStats {

    private Graph graph;

    private final Map<String, Stats> statsCache = new HashMap<String, Stats>();
    private String valueEdgeAttr;
    private String xNodeAttr;
    private String yNodeAttr;
    private double[] edgeLengths;

    public GraphStats(Graph graph, String valueEdgeAttr, String xNodeAttr, String yNodeAttr) {
        this.graph = graph;
        this.valueEdgeAttr = valueEdgeAttr;
        this.xNodeAttr = xNodeAttr;
        this.yNodeAttr = yNodeAttr;
    }

    public Stats getValueEdgeAttrStats() {
        return getValueEdgeAttrStats(valueEdgeAttr);
    }

    private double[] getEdgeLengths() {
        if (edgeLengths == null) {
            int numEdges = graph.getEdgeCount();
            edgeLengths = new double[numEdges];
            for (int i = 0; i < numEdges; i++) {
                Edge edge = graph.getEdge(i);
                Node src = edge.getSourceNode();
                Node target = edge.getTargetNode();
                double x1 = src.getDouble(xNodeAttr);
                double y1 = src.getDouble(yNodeAttr);
                double x2 = target.getDouble(xNodeAttr);
                double y2 = target.getDouble(yNodeAttr);
                double d = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
                edgeLengths[i] = d;
            }
        }
        return edgeLengths;
    }

    public Stats getEdgeLengthStats() {
        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;

        double[] lengths = getEdgeLengths();
        for (int i = 0; i < lengths.length; i++) {
            double v = lengths[i];
            if (v > max) {
                max = v;
            }
            if (v < min) {
                min = v;
            }
        }
        return new Stats(min, max);
    }

    public Stats getValueEdgeAttrStats(String attrName) {
        String key = "edge-" + attrName;
        Stats stats = statsCache.get(key);
        if (stats == null) {
            stats = Stats.getTupleStats(graph.getEdges(), attrName);
            statsCache.put(key, stats);
        }
        return stats;
    }

    public Stats getNodeAttrStats(String attrName) {
    	String key = "node-" + attrName;
    	Stats stats = statsCache.get(key);
    	if (stats == null) {
            stats = Stats.getTupleStats(graph.getNodes(), attrName);
            statsCache.put(key, stats);
    	}
    	return stats;
    }


}
