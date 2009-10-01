package ch.unifr.flowmap.visuals;

import java.awt.Insets;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import ch.unifr.flowmap.models.FlowMapModel;
import ch.unifr.flowmap.util.Stats;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PBounds;

/**
 * @author Ilya Boyandin
 */
public class VisualFlowMap extends PNode {

    private static Logger logger = Logger.getLogger(VisualFlowMap.class);
    private static final int DEFAULT_NODE_SIZE = 4;
    private static final int SHORT_ANIMATION_DURATION = 500;
    private final Tooltip tooltipBox;
    private PBounds nodeBounds;

    private final PNode edgeLayer;
    private final PNode nodeLayer;

    private FlowMapModel model;
    private final Map<Node, VisualNode> nodesToVisuals;
    private final Map<Edge, VisualEdge> edgesToVisuals;
    private PCanvas canvas;

    public VisualFlowMap(PCanvas canvas, FlowMapModel model) {
        this.canvas = canvas;
    	this.model = model;

        nodeLayer = new PNode();

        Graph graph = model.getGraph();

        final int numNodes = graph.getNodeCount();
        nodesToVisuals = new LinkedHashMap<Node, VisualNode>();

        Stats xStats = model.getNodeAttrStats(model.getXNodeAttr());
        Stats yStats = model.getNodeAttrStats(model.getYNodeAttr());

        for (int i = 0; i < numNodes; i++) {
            Node node = graph.getNode(i);

            VisualNode vnode = new VisualNode(this, node,
                    node.getDouble(model.getXNodeAttr()),// - xStats.min,
                    node.getDouble(model.getYNodeAttr()),// - yStats.min,
                    DEFAULT_NODE_SIZE);
            nodeLayer.addChild(vnode);
            nodesToVisuals.put(node, vnode);
        }
        
        
        edgeLayer = new PNode();

        for (int i = 0; i < graph.getEdgeTable().getColumnCount(); i++) {
            if (logger.isDebugEnabled()) logger.debug("Field: " + graph.getEdgeTable().getColumnName(i));
        }

        edgesToVisuals = new LinkedHashMap<Edge, VisualEdge>();
        Iterator<Integer> it = graph.getEdgeTable().rowsSortedBy(model.getValueEdgeAttr(), true);
        while (it.hasNext()) {
            Edge edge = graph.getEdge(it.next());

            double value = edge.getDouble(model.getValueEdgeAttr());
            if (Double.isNaN(value)) {
                if (logger.isDebugEnabled()) logger.debug("Warning: Omitting NaN value for edge: " + edge +
                        ": (" + edge.getSourceNode().getString(model.getLabelAttr()) + " -> " +
                        edge.getTargetNode().getString(model.getLabelAttr()) + ")");
            } else {
                VisualNode fromNode = nodesToVisuals.get(edge.getSourceNode());
                VisualNode toNode = nodesToVisuals.get(edge.getTargetNode());

                VisualEdge ve = new BSplineVisualEdge(this, edge, fromNode, toNode);
                ve.update();
                edgeLayer.addChild(ve);

                edgesToVisuals.put(edge, ve);
            }
        }

        addChild(edgeLayer);
        addChild(nodeLayer);

        tooltipBox = new Tooltip();
        tooltipBox.setVisible(false);
        tooltipBox.setPickable(false);
        PCamera camera = getCamera();
	    camera.addChild(tooltipBox);
        
        initModelChangeListeners(model);
//        fitInCameraView();
//        fitInCameraView(false);
    }

    private PBounds getNodesBounds() {
        if (nodeBounds == null) {
//            nodeBounds = new PBounds(
//                    0, 0, (xStats.max - xStats.min) / 2, (yStats.max - yStats.min) / 2
//            );
            PBounds b = null;
            for (VisualNode node : nodesToVisuals.values()) {
                if (b == null) {
                    b = node.getBounds();
                } else {
                    Rectangle2D.union(b, node.getBoundsReference(), b);
                }
            }
            nodeBounds = b;
        }
        return nodeBounds;
    }

    public FlowMapModel getModel() {
        return model;
    }

    public double getMaxEdgeWidth() {
        return model.getMaxEdgeWidth();
    }

    private static final Insets contentInsets = new Insets(10, 10, 10, 10);
    
    private Insets getContentInsets() {
        return contentInsets;
    }
    
//    public void fitInCameraView(boolean animate) {
//        if (nodeBounds != null) {
//            Insets insets = getContentInsets();
//            insets.left += 5;
//            insets.top += 5;
//            insets.bottom += 5;
//            insets.right += 5;
//            if (animate) {
//                PiccoloUtils.animateViewToPaddedBounds(getCamera(), nodeBounds, insets, SHORT_ANIMATION_DURATION);
//            } else {
//                PiccoloUtils.setViewPaddedBounds(getCamera(), nodeBounds, insets);
//            }
//        }
//    }

    public void fitInCameraView() {
        PBounds boundRect = getNodesBounds();
//        PPath boundRectPath = new PPath(boundRect);
//        addChild(boundRectPath);
//        boundRectPath.setStrokePaint(Color.red);
        boundRect = (PBounds)getCamera().globalToLocal(boundRect);
//        PiccoloUtils.setViewPaddedBounds(getCamera(), boundRect, new Insets(10, 10, 10, 10));
        getCamera().animateViewToCenterBounds(boundRect, true, 0);
    }

    private VisualNode selectedNode;
    
    public String getLabelAttr() {
        return model.getLabelAttr();
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
                    model.getValueEdgeAttr() + ": ", Double.toString(edge.getValue()));
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

    private void initModelChangeListeners(FlowMapModel model) {
        model.addPropertyChangeListener(FlowMapModel.PROPERTY_AUTO_ADJUST_COLOR_SCALE, new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                updateEdgeColors();
                updateEdgeMarkerColors();
            }
        });

        model.addPropertyChangeListener(FlowMapModel.PROPERTY_MAX_EDGE_WIDTH, new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                updateEdgeWidths();
            }
        });

        model.addPropertyChangeListener(FlowMapModel.PROPERTY_EDGE_ALPHA, new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                updateEdgeColors();
            }
        });

        model.addPropertyChangeListener(FlowMapModel.PROPERTY_EDGE_MARKER_ALPHA, new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                updateEdgeMarkerColors();
            }
        });

        PropertyChangeListener valueFilterListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                updateEdgeVisibility();
                updateEdgeColors();
                updateEdgeMarkerColors();
            }
        };
        model.addPropertyChangeListener(FlowMapModel.PROPERTY_VALUE_FILTER_MIN, valueFilterListener);
        model.addPropertyChangeListener(FlowMapModel.PROPERTY_VALUE_FILTER_MAX, valueFilterListener);

        PropertyChangeListener edgeLengthFilterListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                updateEdgeVisibility();
            }
        };
        model.addPropertyChangeListener(FlowMapModel.PROPERTY_EDGE_LENGTH_FILTER_MIN, edgeLengthFilterListener);
        model.addPropertyChangeListener(FlowMapModel.PROPERTY_EDGE_LENGTH_FILTER_MAX, edgeLengthFilterListener);
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
            ve.updateVisibiliy();
        }
        for (VisualNode vn : nodesToVisuals.values()) {
            vn.updatePickability();
        }
    }

    private void updateEdgeWidths() {
        for (VisualEdge ve : edgesToVisuals.values()) {
            ve.updateEdgeWidth();
        }
    }

    public PCamera getCamera() {
        return canvas.getCamera();
    }
}
