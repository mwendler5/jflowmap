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

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import ch.unifr.flowmap.bundling.ForceDirectedEdgeBundler;
import ch.unifr.flowmap.models.FlowMapParamsModel;
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

    private FlowMapParamsModel model;
    private Map<Node, VisualNode> nodesToVisuals;
    private Map<Edge, VisualEdge> edgesToVisuals;
    private PCanvas canvas;
    private Graph graph;

    public VisualFlowMap(PCanvas canvas, Graph graph, FlowMapParamsModel model) {
        this.graph = graph;
        this.canvas = canvas;
    	this.model = model;

        nodeLayer = new PNode();
        createNodes();
        
        edgeLayer = new PNode();
        createEdges();

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

    private void createNodes() {
        nodeLayer.removeAllChildren();

        final int numNodes = graph.getNodeCount();
        nodesToVisuals = new LinkedHashMap<Node, VisualNode>();

//        Stats xStats = model.getNodeAttrStats(model.getXNodeAttr());
//        Stats yStats = model.getNodeAttrStats(model.getYNodeAttr());

        for (int i = 0; i < numNodes; i++) {
            Node node = graph.getNode(i);

            VisualNode vnode = new VisualNode(this, node,
                    node.getDouble(model.getXNodeAttr()),// - xStats.min,
                    node.getDouble(model.getYNodeAttr()),// - yStats.min,
                    DEFAULT_NODE_SIZE);
            nodeLayer.addChild(vnode);
            nodesToVisuals.put(node, vnode);
        }
    }

    
    private void createEdges() {
        createEdges(null);
    }
    
    private void createEdges(Point2D[][] edgeSplinePoints) {
        edgeLayer.removeAllChildren();
        
//      for (int i = 0; i < graph.getEdgeTable().getColumnCount(); i++) {
//      if (logger.isDebugEnabled()) logger.debug("Field: " + graph.getEdgeTable().getColumnName(i));
//  }

        edgesToVisuals = new LinkedHashMap<Edge, VisualEdge>();
        @SuppressWarnings("unchecked")
        
        Iterator<Integer> it = graph.getEdgeTable().rowsSortedBy(model.getValueEdgeAttr(), true);

        while (it.hasNext()) {
            Edge edge = graph.getEdge(it.next());
            
            if (edge.getSourceNode().equals(edge.getTargetNode())) {
                logger.warn(
                        "Self-loop edge: " +
                        edge.getSourceNode().getString(model.getLabelAttr()) + " -> " +
                        edge.getTargetNode().getString(model.getLabelAttr()) + 
                        " [" + edge + "]"
                );
            }
            
            double value = edge.getDouble(model.getValueEdgeAttr());
            if (Double.isNaN(value)) {
                logger.warn(
                    "Omitting NaN value for edge: " +
                    edge.getSourceNode().getString(model.getLabelAttr()) + " -> " +
                    edge.getTargetNode().getString(model.getLabelAttr()) + 
                    " [" + edge + "]"
                );
            } else {
                VisualNode fromNode = nodesToVisuals.get(edge.getSourceNode());
                VisualNode toNode = nodesToVisuals.get(edge.getTargetNode());

                VisualEdge ve;
                if (edgeSplinePoints == null) {
                    ve = new LineVisualEdge(this, edge, fromNode, toNode);
                } else {
                    ve = new BSplineVisualEdge(this, edge, fromNode, toNode, edgeSplinePoints[edge.getRow()]);
                }
                ve.update();
                edgeLayer.addChild(ve);

                edgesToVisuals.put(edge, ve);
            }
        }
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

    public FlowMapParamsModel getModel() {
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
        double x = pos.getX();
        double y = pos.getY();
        pos = new Point2D.Double(x, y);
        getCamera().viewToLocal(pos);
        x = pos.getX();
        y = pos.getY();
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
        pos.setLocation(x + 8, y + 8);
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

    private void initModelChangeListeners(FlowMapParamsModel model) {
        model.addPropertyChangeListener(FlowMapParamsModel.PROPERTY_AUTO_ADJUST_COLOR_SCALE, new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                updateEdgeColors();
                updateEdgeMarkerColors();
            }
        });

        model.addPropertyChangeListener(FlowMapParamsModel.PROPERTY_MAX_EDGE_WIDTH, new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                updateEdgeWidths();
            }
        });

        model.addPropertyChangeListener(FlowMapParamsModel.PROPERTY_EDGE_ALPHA, new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                updateEdgeColors();
            }
        });

        model.addPropertyChangeListener(FlowMapParamsModel.PROPERTY_EDGE_MARKER_ALPHA, new PropertyChangeListener() {
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
        model.addPropertyChangeListener(FlowMapParamsModel.PROPERTY_VALUE_FILTER_MIN, valueFilterListener);
        model.addPropertyChangeListener(FlowMapParamsModel.PROPERTY_VALUE_FILTER_MAX, valueFilterListener);

        PropertyChangeListener edgeLengthFilterListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                updateEdgeVisibility();
            }
        };
        model.addPropertyChangeListener(FlowMapParamsModel.PROPERTY_EDGE_LENGTH_FILTER_MIN, edgeLengthFilterListener);
        model.addPropertyChangeListener(FlowMapParamsModel.PROPERTY_EDGE_LENGTH_FILTER_MAX, edgeLengthFilterListener);
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
    
    private ForceDirectedEdgeBundler bundler = null;

    public void resetBundling() {
        createEdges();
        bundler = null;
    }

    public void bundlingCycle() {
        if (bundler == null) {
            initBundler(10);
        }
        bundler.nextCycle();
        createEdges(bundler.getEdgePoints());
    }

    private void initBundler(int numCycles) {
        bundler = new ForceDirectedEdgeBundler(graph, model.getXNodeAttr(), model.getYNodeAttr());
//        bundler.init(numCycles, .04);
        bundler.init(numCycles, 4);
    }
    
    public void bundleEdges(int numCycles) {
        initBundler(numCycles);
        for (int cycle = 0; cycle < numCycles; cycle++) {
            bundlingCycle();
        }
        bundler = null;
    }
}
