package jflowmap.data;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import jflowmap.models.FlowMapModel;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;

/**
 * @author Ilya Boyandin
 *         Date: 21-Sep-2009
 */
public class FlowMapStats {

    private final Map<String, MinMax> statsCache = new HashMap<String, MinMax>();
    private FlowMapModel flowMapModel;
    private MinMax edgeLengthStats;

    private FlowMapStats(FlowMapModel flowMapModel) {
        this.flowMapModel = flowMapModel;
        // TODO: add property change listeners
    }

    public static FlowMapStats createFor(FlowMapModel flowMapModel) {
        return new FlowMapStats(flowMapModel);
    }

    public MinMax getEdgeWeightStats() {
        return getEdgeWeightStats(flowMapModel.getEdgeWeightAttr());
    }

    private double[] getEdgeLengths() {
        Graph graph = flowMapModel.getGraph();
        int numEdges = graph.getEdgeCount();
        double[] edgeLengths = new double[numEdges];
        for (int i = 0; i < numEdges; i++) {
            Edge edge = graph.getEdge(i);
            Node src = edge.getSourceNode();
            Node target = edge.getTargetNode();
            double x1 = src.getDouble(flowMapModel.getXNodeAttr());
            double y1 = src.getDouble(flowMapModel.getYNodeAttr());
            double x2 = target.getDouble(flowMapModel.getXNodeAttr());
            double y2 = target.getDouble(flowMapModel.getYNodeAttr());
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
            stats = TupleStats.createFor(flowMapModel.getGraph().getEdges(), attrName);
            statsCache.put(key, stats);
        }
        return stats;
    }
    
    public MinMax getNodeAttrStats(String attrName) {
    	String key = "node-" + attrName;
    	MinMax stats = statsCache.get(key);
    	if (stats == null) {
            stats = TupleStats.createFor(flowMapModel.getGraph().getNodes(), attrName);
            statsCache.put(key, stats);
    	}
    	return stats;
    }
    
    public MinMax getNodeXStats() {
        return getNodeAttrStats(flowMapModel.getXNodeAttr());
    }

    public MinMax getNodeYStats() {
        return getNodeAttrStats(flowMapModel.getYNodeAttr());
    }

}
