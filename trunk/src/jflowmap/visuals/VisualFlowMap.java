package jflowmap.visuals;

import java.awt.Color;
import java.awt.Insets;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import jflowmap.JFlowMap;
import jflowmap.bundling.ForceDirectedBundlerParameters;
import jflowmap.bundling.ForceDirectedEdgeBundler;
import jflowmap.clustering.NodeDistanceMeasure;
import jflowmap.models.FlowMapParamsModel;
import jflowmap.util.ColorUtils;
import jflowmap.util.GraphStats;
import jflowmap.util.MinMax;

import org.apache.log4j.Logger;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import at.fhj.utils.misc.ProgressTracker;
import at.fhj.utils.misc.TaskCompletionListener;
import at.fhj.utils.swing.ProgressDialog;
import at.fhj.utils.swing.ProgressWorker;
import ch.unifr.dmlib.cluster.ClusterNode;
import ch.unifr.dmlib.cluster.ClusterSetBuilder;
import ch.unifr.dmlib.cluster.HierarchicalClusterer;
import ch.unifr.dmlib.cluster.Linkage;
import ch.unifr.dmlib.cluster.HierarchicalClusterer.DistanceMatrix;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PBounds;

/**
 * @author Ilya Boyandin
 */
public class VisualFlowMap extends PNode {

    private static final Color SINGLE_ELEMENT_CLUSTER_COLOR = new Color(100, 100, 100, 150);
    private static Logger logger = Logger.getLogger(VisualFlowMap.class);
    private final Tooltip tooltipBox;
    private PBounds nodeBounds;

    private final PNode edgeLayer;
    private final PNode nodeLayer;

    private final FlowMapParamsModel model;
    private Map<Node, VisualNode> nodesToVisuals;
    private Map<Edge, VisualEdge> edgesToVisuals;
    private final PCanvas canvas;
    private final Graph graph;
    private GraphStats graphStats;
    private final JFlowMap jFlowMap;

    public VisualFlowMap(JFlowMap jFlowMap, PCanvas canvas, Graph graph,
    		GraphStats stats, FlowMapParamsModel model) {
        this.jFlowMap = jFlowMap;
        this.graph = graph;
        this.graphStats = stats;
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

//        MinMax xStats = graphStats.getNodeXStats();
//        MinMax yStats = graphStats.getNodeYStats();
//        double nodeSize = (Math.min(xStats.getMax() - xStats.getMin(), yStats.getMax() - yStats.getMin()) / 250);
        MinMax lenStats = graphStats.getEdgeLengthStats();
//        double nodeSize = lenStats.getMin() / 10;
        double nodeSize = lenStats.getAvg() / 50;
//        double nodeSize = Math.max(
//                lenStats.getAvg() / 70,
//                Math.min(xStats.getMax() - xStats.getMin(), yStats.getMax() - yStats.getMin()) / 100);

        for (int i = 0; i < numNodes; i++) {
            Node node = graph.getNode(i);

            VisualNode vnode = new VisualNode(this, node,
                    node.getDouble(model.getXNodeAttr()),// - xStats.min,
                    node.getDouble(model.getYNodeAttr()),// - yStats.min,
                    nodeSize);
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
        
//        Iterator<Integer> it = graph.getEdgeTable().rows();
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
                    ve = new LineVisualEdge(this, edge, fromNode, toNode, false);
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

    public GraphStats getGraphStats() {
		return graphStats;
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
    	model.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				String prop = evt.getPropertyName();
				if (prop.equals(FlowMapParamsModel.PROPERTY_AUTO_ADJUST_COLOR_SCALE)  ||
					prop.equals(FlowMapParamsModel.PROPERTY_EDGE_ALPHA)  ||
					prop.equals(FlowMapParamsModel.PROPERTY_DIRECTION_MARKER_ALPHA)  ||
					prop.equals(FlowMapParamsModel.PROPERTY_FILL_EDGES_WITH_GRADIENT) ||
					prop.equals(FlowMapParamsModel.PROPERTY_SHOW_DIRECTION_MARKERS)  ||
					prop.equals(FlowMapParamsModel.PROPERTY_USE_PROPORTIONAL_DIRECTION_MARKERS)  ||
					prop.equals(FlowMapParamsModel.PROPERTY_DIRECTION_MARKER_SIZE)
				) {
					updateEdgeColors();
				} else if (prop.equals(FlowMapParamsModel.PROPERTY_MAX_EDGE_WIDTH)) {
					updateEdgeWidths();
				} else if (prop.equals(FlowMapParamsModel.PROPERTY_VALUE_FILTER_MIN) ||
						prop.equals(FlowMapParamsModel.PROPERTY_VALUE_FILTER_MAX))
				{
	                updateEdgeVisibility();
	                updateEdgeColors();
				} else if (prop.equals(FlowMapParamsModel.PROPERTY_EDGE_LENGTH_FILTER_MIN) ||
						prop.equals(FlowMapParamsModel.PROPERTY_EDGE_LENGTH_FILTER_MAX))
				{
	                updateEdgeVisibility();
				}
			}
		});
    }

    private void updateEdgeColors() {
        for (PNode node : (List<PNode>) edgeLayer.getChildrenReference()) {
            if (node instanceof VisualEdge) {
                ((VisualEdge) node).updateEdgeColors();
            }
        }
    }

//    private void updateEdgeMarkerColors() {
//        for (PNode node : (List<PNode>) edgeLayer.getChildrenReference()) {
//            if (node instanceof VisualEdge) {
//                ((VisualEdge) node).updateEdgeMarkerColors();
//            }
//        }
//    }

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

    public void bundleEdges(ForceDirectedBundlerParameters params) {
        ProgressTracker pt = new ProgressTracker();
        final ForceDirectedEdgeBundler bundler =
                new ForceDirectedEdgeBundler(graph, 
                        model.getXNodeAttr(), model.getYNodeAttr(), 
                        model.getValueEdgeAttr(), 
                        params);
        EdgeBundlerWorker worker = new EdgeBundlerWorker(pt, bundler);
        ProgressDialog dialog = new ProgressDialog(jFlowMap.getApp(), "Edge Bundling", worker, true);
        pt.addProgressListener(dialog);
        pt.addTaskCompletionListener(new TaskCompletionListener() {
            public void taskCompleted(int taskId) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        createEdges(bundler.getEdgePoints(), false);
                        repaint();
                    }
                });
            }
        });
        worker.start();
        dialog.setVisible(true);
    }
    
    private class EdgeBundlerWorker extends ProgressWorker {

        private final ForceDirectedEdgeBundler bundler;

        public EdgeBundlerWorker(ProgressTracker progress, ForceDirectedEdgeBundler bundler) {
            super(progress);
            this.bundler = bundler;
        }

        @Override
        public Object construct() {
            try {
                bundler.bundle(getProgressTracker());
            } catch (Throwable th) {
                logger.error("Bundling error", th);
                JOptionPane.showMessageDialog(jFlowMap,
                        "Bundling error: [" + th.getClass().getSimpleName()+ "] " + th.getMessage()
                );
            }
            return null;
        }
    }

    private ClusterNode<VisualNode> rootCluster = null;
    private List<VisualNodeDistance> nodeDistanceList;
    private double maxNodeDistance;
    private double clusterDistanceThreshold;

    public ClusterNode<VisualNode> getRootCluster() {
        return rootCluster;
    }

    public List<VisualNodeDistance> getNodeDistanceList() {
        return nodeDistanceList;
    }

    public double getMaxNodeDistance() {
        if (Double.isNaN(maxNodeDistance)) {
            List<VisualNodeDistance> distances = getNodeDistanceList();
            double max = Double.NaN;
            if (distances.size() > 0) {
                max = 0;
                for (VisualNodeDistance d : distances) {
                    if (!Double.isInfinite(d.getDistance()) && d.getDistance() > max) max = d.getDistance();
                }
            }
            maxNodeDistance = max;
        }
        return maxNodeDistance;
    }

    public void setClusterDistanceThreshold(double value) {
        this.clusterDistanceThreshold = value;
        updateClusters();
    }
    
    public void updateClusters() {
        if (rootCluster != null) {
            List<List<VisualNode>> clusters = ClusterSetBuilder.getClusters(rootCluster, clusterDistanceThreshold);
            
            hideClusterMarkers();
            int numClusters = 0;
            for (List<VisualNode> cluster : clusters) {
                if (cluster.size() > 1) {
                    numClusters++; 
                }
            }
            Color[] colors = ColorUtils.createCategoryColors(numClusters, 150);
            int lastColor = 0;
            for (int i = 0, size = clusters.size(); i < size; i++) {
                List<VisualNode> cluster = clusters.get(i);
                for (VisualNode node : cluster) {
                    if (cluster.size() > 1) {
                        node.showClusterMarker(Integer.toString(i), colors[lastColor]);
                    } else {
                        node.showClusterMarker(null, SINGLE_ELEMENT_CLUSTER_COLOR);
                    }
                }
                if (cluster.size() > 1) {
                    lastColor++;
                }
            }
        }
    }

    private void hideClusterMarkers() {
        for (VisualNode node : nodesToVisuals.values()) {
            node.hideClusterMarker();
        }
    }

    public void clusterNodes(NodeDistanceMeasure distanceMeasure) {
        logger.info("Clustering nodes");
        doCluster(distanceMeasure, new ArrayList<VisualNode>(nodesToVisuals.values()), new ProgressTracker());
//        if (logger.isDebugEnabled()) {
//        	logger.debug("\n" + rootCluster.dumpToTreeString());
//        }
    }

    private void doCluster(NodeDistanceMeasure distanceMeasure, List<VisualNode> items, ProgressTracker tracker) {
        HierarchicalClusterer<VisualNode> clusterer = 
            new HierarchicalClusterer<VisualNode>(distanceMeasure, Linkage.SINGLE);
        
        items = distanceMeasure.filterNodes(items);

        DistanceMatrix<VisualNode> distances = clusterer.makeDistanceMatrix(items, tracker);
        nodeDistanceList = VisualNodeDistance.makeDistanceList(items, distances);
        maxNodeDistance = Double.NaN;
        rootCluster = clusterer.cluster(items, distances, tracker);
    }

}
