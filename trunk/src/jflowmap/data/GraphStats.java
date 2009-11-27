package jflowmap.data;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;

/**
 * @author Ilya Boyandin
 *         Date: 21-Sep-2009
 */
public class GraphStats {

    private Graph graph;

    private final Map<String, MinMax> statsCache = new HashMap<String, MinMax>();
    private String edgeWeightAttr;
    private String xNodeAttr;
    private String yNodeAttr;
    private MinMax edgeLengthStats;

    private GraphStats(Graph graph, String edgeWeightAttr, String xNodeAttr, String yNodeAttr) {
        this.graph = graph;
        this.edgeWeightAttr = edgeWeightAttr;
        this.xNodeAttr = xNodeAttr;
        this.yNodeAttr = yNodeAttr;
    }

    public static GraphStats createFor(Graph graph, String edgeWeightAttr, String xNodeAttr, String yNodeAttr) {
        return new GraphStats(graph, edgeWeightAttr, xNodeAttr, yNodeAttr);
    }

    
    public MinMax getEdgeWeightStats() {
        return getEdgeWeightStats(edgeWeightAttr);
    }

    private double[] getEdgeLengths() {
        int numEdges = graph.getEdgeCount();
        double[] edgeLengths = new double[numEdges];
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
        return edgeLengths;
    }

    public MinMax getEdgeLengthStats() {
        if (edgeLengthStats == null) {
            final double[] lengths = getEdgeLengths();
            edgeLengthStats = MinMax.createFor(new Iterator<Double>() {
                int i = 0;
                public boolean hasNext() {
                    return i < lengths.length;
                }
                public Double next() {
                    return lengths[i++];
                }
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            });
        }
        return edgeLengthStats;
    }

    public MinMax getEdgeWeightStats(String attrName) {
        String key = "edge-" + attrName;
        MinMax stats = statsCache.get(key);
        if (stats == null) {
            stats = TupleStats.createFor(graph.getEdges(), attrName);
            statsCache.put(key, stats);
        }
        return stats;
    }
    
    public MinMax getNodeAttrStats(String attrName) {
    	String key = "node-" + attrName;
    	MinMax stats = statsCache.get(key);
    	if (stats == null) {
            stats = TupleStats.createFor(graph.getNodes(), attrName);
            statsCache.put(key, stats);
    	}
    	return stats;
    }
    
    public MinMax getNodeXStats() {
        return getNodeAttrStats(xNodeAttr);
    }

    public MinMax getNodeYStats() {
        return getNodeAttrStats(yNodeAttr);
    }

    /**
     * Constructs a <code>String</code> with all attributes
     * in name = value format.
     *
     * @return a <code>String</code> representation 
     * of this object.
     */
    public String toString()
    {
        final String TAB = "    ";
        
        String retValue = "";
        
        retValue = "GraphStats ( "
            + super.toString() + TAB
            + "graph = " + this.graph + TAB
            + "statsCache = " + this.statsCache + TAB
            + "edgeWeightAttr = " + this.edgeWeightAttr + TAB
            + "xNodeAttr = " + this.xNodeAttr + TAB
            + "yNodeAttr = " + this.yNodeAttr + TAB
            + "edgeLengthStats = " + this.edgeLengthStats + TAB
            + " )";
    
        return retValue;
    }

}
