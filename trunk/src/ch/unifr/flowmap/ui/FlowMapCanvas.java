package ch.unifr.flowmap.ui;

import java.awt.Color;
import java.awt.Insets;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import ch.unifr.flowmap.data.Stats;
import ch.unifr.flowmap.util.PiccoloUtils;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PBounds;

/**
 * @author Ilya Boyandin
 */
public class FlowMapCanvas extends PCanvas {

    private static final int DEFAULT_NODE_SIZE = 6;
    private static final int SHORT_ANIMATION_DURATION = 500;
    private static final long serialVersionUID = 1L;
    private final PValueTooltip tooltipBox;
    private final Graph graph;
    private final String valueEdgeAttr;
    private final String xNodeAttr = "x";
    private final String yNodeAttr = "y";
    private String labelAttr = "tooltip";
    private final PBounds nodeBounds;
    
    private final PNode edgeLayer;
    private final PNode nodeLayer;

    private int edgeAlpha = 20;
    private int edgeMarkerAlpha = 120;

    private double valueFilterMin = Double.MIN_VALUE;
    private double valueFilterMax = Double.MAX_VALUE;

    private double edgeLengthFilterMin = Double.MIN_VALUE;
    private double edgeLengthFilterMax = Double.MAX_VALUE;

    private double maxEdgeWidth = 10.0;

    private final Map<Node, VisualNode> nodesToVisuals;
    private final Map<Edge, VisualEdge> edgesToVisuals;
    private boolean autoAdjustEdgeColorScale;

    private final GraphStats graphStats;
    
    public FlowMapCanvas(Graph graph, String valueEdgeAttrName, String labelAttrName) {
    	this.graph = graph;
    	this.valueEdgeAttr = valueEdgeAttrName;
    	this.labelAttr = labelAttrName;

        this.graphStats = new GraphStats(graph, valueEdgeAttrName, xNodeAttr, yNodeAttr);

        Stats minMax = graphStats.getValueEdgeAttrStats();

        valueFilterMin = minMax.min;
        valueFilterMax = minMax.max;


//        setBackground(new Color(47, 89, 134));
        setBackground(Color.BLACK);
        
        addInputEventListener(new ZoomHandler(.5, 50));
        setPanEventHandler(new PanHandler());
        
//        Stroke stroke = new BasicStroke(2);
//        Color STROKE_PAINT = new Color(238, 238, 0, 100);
//        Color STROKE_PAINT = new Color(255, 255, 255, 13);

        nodeLayer = new PNode();
        
        final int numNodes = graph.getNodeCount();

        Stats xStats = graphStats.getNodeAttrStats(xNodeAttr);
        Stats yStats = graphStats.getNodeAttrStats(yNodeAttr);

        System.out.println("xStats: " + xStats);
        System.out.println("yStats: " + yStats);

        nodesToVisuals = new LinkedHashMap<Node, VisualNode>();

        for (int i = 0; i < numNodes; i++) {
            Node node = graph.getNode(i);

            VisualNode vnode = new VisualNode(this, node,
                    node.getDouble(xNodeAttr) - xStats.min,
                    node.getDouble(yNodeAttr) - yStats.min,
                    DEFAULT_NODE_SIZE);
            nodeLayer.addChild(vnode);
            nodesToVisuals.put(node, vnode);
        }
        
        
        edgeLayer = new PNode();

        for (int i = 0; i < graph.getEdgeTable().getColumnCount(); i++) {
            System.out.println("Field: " + graph.getEdgeTable().getColumnName(i));
        }

        edgesToVisuals = new LinkedHashMap<Edge, VisualEdge>();
        Iterator<Integer> it = graph.getEdgeTable().rowsSortedBy(valueEdgeAttrName, true);
        while (it.hasNext()) {
            Edge edge = graph.getEdge(it.next());

            double value = edge.getDouble(valueEdgeAttrName);
            if (Double.isNaN(value)) {
                System.out.println("Warning: Omitting NaN value for edge: " + edge +
                        ": (" + edge.getSourceNode().getString(labelAttr) + " -> " +
                        edge.getTargetNode().getString(labelAttr) + ")");
            } else {
                VisualNode fromNode = nodesToVisuals.get(edge.getSourceNode());
                VisualNode toNode = nodesToVisuals.get(edge.getTargetNode());

                VisualEdge ve = new VisualEdge(this, edge, fromNode, toNode);
                edgeLayer.addChild(ve);

                edgesToVisuals.put(edge, ve);
            }
        }

        getLayer().addChild(edgeLayer);
        getLayer().addChild(nodeLayer);
        
        tooltipBox = new PValueTooltip();
        tooltipBox.setVisible(false);
        tooltipBox.setPickable(false);
        PCamera camera = getCamera();
	    camera.addChild(tooltipBox);
        
        nodeBounds = new PBounds(
        	0, 0, (xStats.max - xStats.min)/2, (yStats.max - yStats.min)/2
        );
    }

    public GraphStats getGraphStats() {
        return graphStats;
    }

    public double getMaxEdgeWidth() {
        return maxEdgeWidth;
    }
    
    public void setMaxEdgeWidth(double maxEdgeWidth) {
        this.maxEdgeWidth = maxEdgeWidth;
        updateEdgeWidths();
    }

    public boolean getAutoAdjustEdgeColorScale() {
        return autoAdjustEdgeColorScale;
    }

    public void setAutoAdjustEdgeColorScale(boolean autoAdjustEdgeColorScale) {
        this.autoAdjustEdgeColorScale = autoAdjustEdgeColorScale;
        updateEdgeColors();
        updateEdgeMarkerColors();
    }

    public double getValueFilterMax() {
        return valueFilterMax;
    }

    public void setValueFilterMax(double valueFilterMax) {
        this.valueFilterMax = valueFilterMax;
        updateEdgeVisibility();
        updateEdgeColors();
        updateEdgeMarkerColors();
    }

    public double getValueFilterMin() {
        return valueFilterMin;
    }

    public void setValueFilterMin(double valueFilterMin) {
        this.valueFilterMin = valueFilterMin;
        updateEdgeVisibility();
        updateEdgeColors();
        updateEdgeMarkerColors();
    }

    public double getEdgeLengthFilterMin() {
        return edgeLengthFilterMin;
    }

    public void setEdgeLengthFilterMin(double edgeLengthFilterMin) {
        this.edgeLengthFilterMin = edgeLengthFilterMin;
        updateEdgeVisibility();
    }

    public double getEdgeLengthFilterMax() {
        return edgeLengthFilterMax;
    }

    public void setEdgeLengthFilterMax(double edgeLengthFilterMax) {
        this.edgeLengthFilterMax = edgeLengthFilterMax;
        updateEdgeVisibility();
    }

    private void updateEdgeColors() {
        for (PNode node : (List<PNode>) edgeLayer.getChildrenReference()) {
            if (node instanceof VisualEdge) {
                ((VisualEdge) node).updateEdgeColors();
            }
        }
    }

    private void updateEdgeMarkerColors() {
        for (PNode node : (List<PNode>) edgeLayer.getChildrenReference()) {
            if (node instanceof VisualEdge) {
                ((VisualEdge) node).updateEdgeMarkerColors();
            }
        }
    }

    private void updateEdgeVisibility() {
        for (VisualEdge ve : edgesToVisuals.values()) {
            final double value = ve.getValue();
            double length = ve.getEdgeLength();
            final boolean visible =
                    valueFilterMin <= value && value <= valueFilterMax    &&
                    edgeLengthFilterMin <= length && length <= edgeLengthFilterMax
            ;
            ve.setVisible(visible);
            ve.setPickable(visible);
            ve.setChildrenPickable(visible);
        }
    }
    
    private void updateEdgeWidths() {
        for (VisualEdge ve : edgesToVisuals.values()) {
            ve.updateEdgeWidth();
        }
    }


    public int getEdgeAlpha() {
        return edgeAlpha;
    }

    public void setEdgeAlpha(int edgeAlpha) {
        this.edgeAlpha = edgeAlpha;
        updateEdgeColors();
    }

    public int getEdgeMarkerAlpha() {
        return edgeMarkerAlpha;
    }

    public void setEdgeMarkerAlpha(int edgeMarkerAlpha) {
        this.edgeMarkerAlpha = edgeMarkerAlpha;
        updateEdgeMarkerColors();
    }
    
    private static final Insets contentInsets = new Insets(10, 10, 10, 10);
    
    private Insets getContentInsets() {
        return contentInsets;
    }
    
    public void fitInCameraView(boolean animate) {
        if (nodeBounds != null) {
            Insets insets = getContentInsets();
            insets.left += 5;
            insets.top += 5;
            insets.bottom += 5;
            insets.right += 5;
            if (animate) {
                PiccoloUtils.animateViewToPaddedBounds(getCamera(), nodeBounds, insets, SHORT_ANIMATION_DURATION);
            } else {
                PiccoloUtils.setViewPaddedBounds(getCamera(), nodeBounds, insets);
            }
        }
    }

    private VisualNode selectedNode;
    
    public String getEdgeValueAttr() {
        return valueEdgeAttr;
    }

    public String getLabelAttr() {
        return labelAttr;
    }

    public void showTooltip(PNode component, Point2D pos) {
        if (component instanceof VisualNode) {
            VisualNode vnode = (VisualNode) component;
//    		tooltipBox.setText(fnode.getId(), nodeData.nodeLabel(nodeIdx), "");
            tooltipBox.setText(
                    vnode.getLabel(),
                    ""
//			        "Outgoing " + selectedFlowAttrName + ": " + graph.getOutgoingTotal(fnode.getId(), selectedFlowAttrName) + "\n" +
//			        "Incoming " + selectedFlowAttrName + ": " + graph.getIncomingTotal(fnode.getId(), selectedFlowAttrName)
                    ,
                    "");
        } else if (component instanceof VisualEdge) {
            VisualEdge edge = (VisualEdge) component;
            tooltipBox.setText(
//                    flow.getStartNodeId() + " - " + flow.getEndNodeId(), 
                    edge.getLabel(),
                    valueEdgeAttr + ": ", Double.toString(edge.getValue()));
        } else {
            return;
        }
//			Point2D pos = event.getPosition();
        final PBounds vb = getCamera().getBoundsReference();
        final PBounds tb = tooltipBox.getBoundsReference();
        double x = pos.getX() + 8;
        double y = pos.getY() + 8;
        if (x + tb.getWidth() > vb.getWidth()) {
            final double _x = pos.getX() - tb.getWidth() - 8;
            if (vb.getX() - _x < x + tb.getWidth() - vb.getMaxX()) {
                x = _x;
            }
        }
        if (y + tb.getHeight() > vb.getHeight()) {
            final double _y = pos.getY() - tb.getHeight() - 8;
            if (vb.getY() - _y < y + tb.getHeight() - vb.getMaxY()) {
                y = _y;
            }
        }
        pos = new Point2D.Double(x, y);
        getCamera().viewToLocal(pos);
        tooltipBox.setPosition(pos.getX(), pos.getY());
        tooltipBox.setVisible(true);
    }

    public void hideTooltip() {
        tooltipBox.setVisible(false);
    }

    public void setSelectedNode(VisualNode vnode) {
        if (selectedNode != null) {
            selectedNode.setSelected(false);
        }
        selectedNode = vnode;
        if (vnode != null) {
            vnode.setSelected(true);
        }
    }

}
