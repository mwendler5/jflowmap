package ch.unifr.flowmap.ui;

import ch.unifr.flowmap.data.Stats;
import prefuse.data.Graph;

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
    private double[][] edgeLengths;

    public GraphStats(Graph graph, String valueEdgeAttr, String xNodeAttr, String yNodeAttr) {
        this.graph = graph;
        this.valueEdgeAttr = valueEdgeAttr;
        this.xNodeAttr = xNodeAttr;
        this.yNodeAttr = yNodeAttr;
    }

    public Stats getValueEdgeAttrStats() {
        return getValueEdgeAttrStats(valueEdgeAttr);
    }

    public double[][] getEdgeLengths() {
        if (edgeLengths == null) {
            int numNodes = graph.getNodeCount();
            edgeLengths = new double[numNodes][numNodes];
            for (int i = 0; i < numNodes; i++) {
                double x1 = graph.getNode(i).getDouble(xNodeAttr);
                double y1 = graph.getNode(i).getDouble(yNodeAttr);
                for (int j = 0; j < numNodes/2; j++) {
                    double x2 = graph.getNode(j).getDouble(xNodeAttr);
                    double y2 = graph.getNode(j).getDouble(yNodeAttr);
                    double d = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
                    edgeLengths[i][j] = d;
                    edgeLengths[j][i] = d;
                }
            }
        }
        return edgeLengths;
    }

    public Stats getEdgeLengthStats() {
        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;

        double[][] lengths = getEdgeLengths();
        for (int i = 0; i < lengths.length; i++)
        for (int j = 0; j < lengths.length/2; j++) {
            double v = lengths[i][j];
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
