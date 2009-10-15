package jflowmap.visuals;

import java.awt.Insets;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import jflowmap.JFlowMap;
import jflowmap.bundling.ForceDirectedBundlerParameters;
import jflowmap.bundling.ForceDirectedEdgeBundler;
import jflowmap.models.FlowMapParamsModel;

import org.apache.log4j.Logger;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import at.fhj.utils.misc.ProgressTracker;
import at.fhj.utils.swing.ProgressDialog;
import at.fhj.utils.swing.ProgressWorker;
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

    private final FlowMapParamsModel model;
    private Map<Node, VisualNode> nodesToVisuals;
    private Map<Edge, VisualEdge> edgesToVisuals;
    private final PCanvas canvas;
    private final Graph graph;
    private final JFlowMap jFlowMap;

    public VisualFlowMap(JFlowMap jFlowMap, PCanvas canvas, Graph graph, FlowMapParamsModel model) {
        this.jFlowMap = jFlowMap;
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
        createEdges(null, false);
    }
    
    private void createEdges(Point2D[][] edgeSplinePoints, boolean showPoints) {
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
                    ve = new BSplineVisualEdge(this, edge, fromNode, toNode, edgeSplinePoints[edge.getRow()], showPoints);
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
    
    public void resetBundling() {
        createEdges();
        repaint();
    }

    public void bundleEdges(int numCycles, ForceDirectedBundlerParameters params) {
        ProgressTracker pt = new ProgressTracker();
        final ForceDirectedEdgeBundler bundler =
                new ForceDirectedEdgeBundler(graph, model.getXNodeAttr(), model.getYNodeAttr(), params);
        EdgeBundlerWorker worker = new EdgeBundlerWorker(pt, bundler, numCycles);
        ProgressDialog dialog = new ProgressDialog(jFlowMap.getApp(), "Edge bundling", worker, true);
        pt.addProgressListener(dialog);
        worker.start();
        dialog.setVisible(true);
    }
    
    private class EdgeBundlerWorker extends ProgressWorker {

        private final int numCycles;
        private ForceDirectedEdgeBundler bundler;

        public EdgeBundlerWorker(ProgressTracker progress, ForceDirectedEdgeBundler bundler, int numCycles) {
            super(progress);
            this.bundler = bundler;
            this.numCycles = numCycles;
        }

        @Override
        public Object construct() {
            try {
                ProgressTracker pt = getProgressTracker();
                pt.startTask("Initializing", .05);
                bundler.init(pt);
                if (pt.isCancelled()) {
                    return null;
                }
                pt.taskCompleted();
                
                // TODO: move this to ForceDirectedEdgeBundler and add a TaskCompletionListener 
                for (int cycle = 0; cycle < numCycles; cycle++) {
                    pt.startTask("Bundling cycle " + (cycle + 1) + " of " + numCycles, .95 / numCycles);
                    bundler.nextCycle();
                    pt.taskCompleted();
                    if (pt.isCancelled()) {
                        return null;
                    }
                    final boolean showPoints = (cycle < numCycles - 1);
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            createEdges(bundler.getEdgePoints(), showPoints);
                            repaint();
                        }
                    });
                }
    
                pt.processFinished();
            } catch (Throwable th) {
                logger.error("Bundling error", th);
                JOptionPane.showMessageDialog(jFlowMap,
                        "Bundling error: [" + th.getClass().getSimpleName()+ "] " + th.getMessage()
                );
            }
            return null;
        }

    }

}
    
