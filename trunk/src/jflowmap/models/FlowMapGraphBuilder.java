package jflowmap.models;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;

import jflowmap.JFlowMap;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;

/**
 * @author Ilya Boyandin
 */
public class FlowMapGraphBuilder {

    private String nodeXAttr = JFlowMap.DEFAULT_NODE_X_ATTR_NAME;
    private String nodeYAttr = JFlowMap.DEFAULT_NODE_Y_ATTR_NAME;
    private String edgeWeightAttr = JFlowMap.DEFAULT_EDGE_WEIGHT_ATTR_NAME;
    private String nodeLabelAttr = JFlowMap.DEFAULT_NODE_LABEL_ATTR_NAME;

    private Graph graph;
    private HashMap<EdgeKey, Edge> cumulatedEdges;

    public FlowMapGraphBuilder() {
        graph = new Graph();
        graph.addColumn(nodeXAttr, double.class);
        graph.addColumn(nodeYAttr, double.class);
        graph.addColumn(edgeWeightAttr, double.class);
        graph.addColumn(nodeLabelAttr, String.class);
    }
    
    public FlowMapGraphBuilder addNodeAttr(String name, Class<?> type) {
        graph.addColumn(name, type);
        return this;
    }

    public FlowMapGraphBuilder withCumulativeEdges() {
        this.cumulatedEdges = new HashMap<EdgeKey, Edge>();
        return this;
    }
    
    public FlowMapGraphBuilder withNodeXAttr(String attrName) {
        this.nodeXAttr = attrName;
        return this;
    }

    public FlowMapGraphBuilder withNodeYAttr(String attrName) {
        this.nodeYAttr = attrName;
        return this;
    }
    
    public FlowMapGraphBuilder withEdgeWeightAttr(String attrName) {
        this.edgeWeightAttr = attrName;
        return this;
    }

    public FlowMapGraphBuilder withNodeLabelAttr(String attrName) {
        this.nodeLabelAttr = attrName;
        return this;
    }
    
    public Node addNode(Point2D position, String label) {
        Node node = graph.addNode();
        node.setDouble(nodeXAttr, position.getX());
        node.setDouble(nodeYAttr, position.getY());
        node.set(nodeLabelAttr, label);
        return node;
    }

    public Edge addEdge(Node from, Node to, double weight) {
        EdgeKey key = new EdgeKey(from, to);
        double sumWeight = weight;
        Edge edge;
        if (cumulatedEdges == null) {
            edge = graph.addEdge(from, to);
        } else {
            edge = cumulatedEdges.get(key);
            if (edge == null) {
                edge = graph.addEdge(from, to);
                cumulatedEdges.put(key, edge);
            } else {
                sumWeight += edge.getDouble(edgeWeightAttr);
            }
        }
        edge.setDouble(edgeWeightAttr, sumWeight);
        return edge;
    }

    public Graph build() {
        cumulatedEdges = null;
        return graph;
    }
    
    private static class EdgeKey {
        final Node from;
        final Node to;
        public EdgeKey(Node from, Node to) {
            if (from == null  || to == null) {
                throw new IllegalArgumentException();
            }
            this.from = from;
            this.to = to;
        }
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + System.identityHashCode(from);
            result = prime * result + System.identityHashCode(to);
            return result;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            EdgeKey other = (EdgeKey) obj;
            if (from != other.from)  // identity check
                return false;
            if (to != other.to)  // identity check
                return false;
            return true;
        }
    }
}
