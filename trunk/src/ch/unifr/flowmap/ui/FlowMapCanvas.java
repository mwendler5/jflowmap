package ch.unifr.flowmap.ui;

import java.awt.Color;
import java.awt.Insets;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

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
    private final PBounds nodeBounds;
    
    private final PNode edgeLayer;
    private final PNode nodeLayer;

    private FlowMapModel model;
    private final Map<Node, VisualNode> nodesToVisuals;
    private final Map<Edge, VisualEdge> edgesToVisuals;

    public FlowMapCanvas(FlowMapModel model) {
    	this.model = model;
        setBackground(Color.BLACK);
        
        addInputEventListener(new ZoomHandler(.5, 50));
        setPanEventHandler(new PanHandler());

        nodeLayer = new PNode();

        Graph graph = model.getGraph();

        final int numNodes = graph.getNodeCount();
        nodesToVisuals = new LinkedHashMap<Node, VisualNode>();

        Stats xStats = model.getNodeAttrStats(model.getXNodeAttr());
        Stats yStats = model.getNodeAttrStats(model.getYNodeAttr());

        for (int i = 0; i < numNodes; i++) {
            Node node = graph.getNode(i);

            VisualNode vnode = new VisualNode(this, node,
                    node.getDouble(model.getXNodeAttr()) - xStats.min,
                    node.getDouble(model.getYNodeAttr()) - yStats.min,
                    DEFAULT_NODE_SIZE);
            nodeLayer.addChild(vnode);
            nodesToVisuals.put(node, vnode);
        }
        
        
        edgeLayer = new PNode();

        for (int i = 0; i < graph.getEdgeTable().getColumnCount(); i++) {
            System.out.println("Field: " + graph.getEdgeTable().getColumnName(i));
        }

        edgesToVisuals = new LinkedHashMap<Edge, VisualEdge>();
        Iterator<Integer> it = graph.getEdgeTable().rowsSortedBy(model.getValueEdgeAttr(), true);
        while (it.hasNext()) {
            Edge edge = graph.getEdge(it.next());

            double value = edge.getDouble(model.getValueEdgeAttr());
            if (Double.isNaN(value)) {
                System.out.println("Warning: Omitting NaN value for edge: " + edge +
                        ": (" + edge.getSourceNode().getString(model.getLabelAttr()) + " -> " +
                        edge.getTargetNode().getString(model.getLabelAttr()) + ")");
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

        initModelChangeListeners(model);
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
            ve.updateEdgeVisibiliy();
        }
    }

    private void updateEdgeWidths() {
        for (VisualEdge ve : edgesToVisuals.values()) {
            ve.updateEdgeWidth();
        }
    }
}
