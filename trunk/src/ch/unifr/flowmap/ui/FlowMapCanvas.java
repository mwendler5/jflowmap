package ch.unifr.flowmap.ui;

import java.awt.Color;
import java.awt.Insets;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Tuple;
import prefuse.data.tuple.TupleSet;
import ch.unifr.flowmap.data.Stats;
import ch.unifr.flowmap.util.PiccoloUtils;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PBounds;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Ilya Boyandin
 */
public class FlowMapCanvas extends PCanvas {

    private static final int DEFAULT_NODE_SIZE = 6;
    private static final int SHORT_ANIMATION_DURATION = 500;
    private static final long serialVersionUID = 1L;
    private final PValueTooltip tooltipBox;
    private final Graph graph;
    private final String edgeValueAttr;
    private String xCoordAttr = "x";
    private String yCoordAttr = "y";
    private String labelAttr = "tooltip";
    private PBounds nodeBounds;
    
    private final PNode edgeLayer;
    private final PNode nodeLayer;

    private int edgeAlpha = 5;
    private int edgeMarkerAlpha = 50;
    private double valueFilterMin = Double.MIN_VALUE;
    private double valueFilterMax = Double.MAX_VALUE;

    private Map<Node, VisualNode> nodesToVisuals;
    private Map<Edge, VisualEdge> edgesToVisuals;
    private boolean autoAdjustEdgeColorScale;

    public FlowMapCanvas(Graph graph, String edgeValueAttrName, String labelAttrName) {
    	this.graph = graph;
    	this.edgeValueAttr = edgeValueAttrName;
    	this.labelAttr = labelAttrName;

        Stats minMax = getEdgeValueAttrStats();

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

        Stats xStats = getNodeAttrStats(xCoordAttr);
        Stats yStats = getNodeAttrStats(yCoordAttr);

        System.out.println("xStats: " + xStats);
        System.out.println("yStats: " + yStats);

	nodesToVisuals = new LinkedHashMap<Node, VisualNode>();

        for (int i = 0; i < numNodes; i++) {
            Node node = graph.getNode(i);

            VisualNode vnode = new VisualNode(this, node,
                    node.getDouble(xCoordAttr) - xStats.min,
                    node.getDouble(yCoordAttr) - yStats.min,
                    DEFAULT_NODE_SIZE);
            nodeLayer.addChild(vnode);
            nodesToVisuals.put(node, vnode);
        }
        
        
        edgeLayer = new PNode();

        for (int i = 0; i < graph.getEdgeTable().getColumnCount(); i++) {
            System.out.println("Field: " + graph.getEdgeTable().getColumnName(i));
        }

	edgesToVisuals = new LinkedHashMap<Edge, VisualEdge>();
//        Iterator<Integer> it = graph.getEdgeTable().rows();  //.rowsSortedBy(edgeValueAttrName, false);
        Iterator<Integer> it = graph.getEdgeTable().rowsSortedBy(edgeValueAttrName, true);
        while (it.hasNext()) {
            Edge edge = graph.getEdge(it.next());

            VisualNode fromNode = nodesToVisuals.get(edge.getSourceNode());
            VisualNode toNode = nodesToVisuals.get(edge.getTargetNode());

            VisualEdge ve = new VisualEdge(this, edge, fromNode, toNode);
            edgeLayer.addChild(ve);

            edgesToVisuals.put(edge, ve);
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
            final boolean visible = valueFilterMin <= value && value <= valueFilterMax;
            ve.setVisible(visible);
            ve.setPickable(visible);
            ve.setChildrenPickable(visible);
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

    private final Map<String, Stats> statsCache = new HashMap<String, Stats>();
    
    public String getEdgeValueAttr() {
        return edgeValueAttr;
    }

    public String getLabelAttr() {
        return labelAttr;
    }

    public Stats getEdgeValueAttrStats() {
        return getEdgeValueAttrStats(edgeValueAttr);
    }

    public Stats getEdgeValueAttrStats(String attrName) {
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

    public void showTooltip(PNode component, Point2D pos) {
    	if (component instanceof VisualNode) {
    	    VisualNode vnode = (VisualNode)component;
//    		tooltipBox.setText(fnode.getId(), nodeData.nodeLabel(nodeIdx), "");
		tooltipBox.setText(
			vnode.getLabel(),
			        ""
//			        "Outgoing " + selectedFlowAttrName + ": " + graph.getOutgoingTotal(fnode.getId(), selectedFlowAttrName) + "\n" +
//			        "Incoming " + selectedFlowAttrName + ": " + graph.getIncomingTotal(fnode.getId(), selectedFlowAttrName)
			        ,
			        "");
    	} else if (component instanceof VisualEdge) {
            VisualEdge edge = (VisualEdge)component;
            tooltipBox.setText(
//                    flow.getStartNodeId() + " - " + flow.getEndNodeId(), 
            		edge.getLabel(),
                    edgeValueAttr + ": ", Double.toString(edge.getValue()));
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

}
