package jflowmap.visuals;

import java.awt.FontMetrics;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
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
import jflowmap.data.GraphStats;
import jflowmap.data.MinMax;
import jflowmap.geom.Point;
import jflowmap.models.FlowMapParams;

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
import ch.unifr.dmlib.cluster.ClusterVisitorAdapter;
import ch.unifr.dmlib.cluster.HierarchicalClusterer;
import ch.unifr.dmlib.cluster.Linkage;
import ch.unifr.dmlib.cluster.HierarchicalClusterer.DistanceMatrix;

import com.google.common.collect.Maps;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PBounds;

/**
 * @author Ilya Boyandin
 */
public class VisualFlowMap extends PNode {

    private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(VisualFlowMap.class);
    public enum Attributes {
        NODE_SELECTION
    }
//    private static final Color SINGLE_ELEMENT_CLUSTER_COLOR = new Color(100, 100, 100, 150);
    private final Tooltip tooltipBox;
    private PBounds nodeBounds;

    private final PNode edgeLayer;
    private final PNode nodeLayer;

    private final FlowMapParams params;
    private List<VisualNode> visualNodes;
    private List<VisualEdge> visualEdges;
    private Map<Node, VisualNode> nodesToVisuals;
    private Map<Edge, VisualEdge> edgesToVisuals;
    private final Graph graph; 
    private GraphStats graphStats;
    private final JFlowMap jFlowMap;

    // clustering fields
    private ClusterNode<VisualNode> rootCluster = null;
    private ClusterNode<VisualNode> euclideanRootCluster = null;
    private List<VisualNodeDistance> nodeDistanceList;
    private double maxNodeDistance;
    private double clusterDistanceThreshold;
    private double euclideanClusterDistanceThreshold;
    private List<VisualNodeCluster> visualNodeClusters;
    private Map<VisualNode, VisualNodeCluster> nodesToClusters;
    private VisualAreaMap areaMap;
    private double euclideanMaxNodeDistance;
    // endOf clustering fields
    private boolean bundled;

    public VisualFlowMap(JFlowMap jFlowMap, Graph graph, GraphStats stats,
    		FlowMapParams model) {
        this.jFlowMap = jFlowMap;
        this.graph = graph;
        this.graphStats = stats;
    	this.params = model;

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
    
    public boolean isBundled() {
        return bundled;
    }

    
    private void createNodes() {
        nodeLayer.removeAllChildren();

        final int numNodes = graph.getNodeCount();
        visualNodes = new ArrayList<VisualNode>();
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
                    node.getDouble(params.getXNodeAttr()),// - xStats.min,
                    node.getDouble(params.getYNodeAttr()),// - yStats.min,
                    nodeSize);
            nodeLayer.addChild(vnode);
            visualNodes.add(vnode);
            nodesToVisuals.put(node, vnode);
        }
    }

    public void setAreaMap(VisualAreaMap areaMap) {
        this.areaMap = areaMap;
        addChild(areaMap);
        areaMap.moveToBack();
    }

    public List<VisualNode> getVisualNodes() {
        return Collections.unmodifiableList(visualNodes);
    }
    
    public List<VisualEdge> getVisualEdges() {
        return Collections.unmodifiableList(visualEdges);
    }
    
    private void createEdges() {
        createEdges(null, false);
    }
    
    public String getLabel(Edge edge) {
        String labelAttr = params.getNodeLabelAttr();
        Node src = edge.getSourceNode();
        Node target = edge.getTargetNode();
        if (labelAttr == null) {
            return src.toString() + " -> " + target.toString();
        } else {
            return src.getString(labelAttr) + " -> " + target.getString(labelAttr);
        }
    }
    
    private void createEdges(Point[][] edgeSplinePoints, boolean showPoints) {
        edgeLayer.removeAllChildren();
        
//      for (int i = 0; i < graph.getEdgeTable().getColumnCount(); i++) {
//      if (logger.isDebugEnabled()) logger.debug("Field: " + graph.getEdgeTable().getColumnName(i));
//  }

        visualEdges = new ArrayList<VisualEdge>();
        edgesToVisuals = new LinkedHashMap<Edge, VisualEdge>();
        @SuppressWarnings("unchecked")
        
//        Iterator<Integer> it = graph.getEdgeTable().rows();
        Iterator<Integer> it = graph.getEdgeTable().rowsSortedBy(params.getEdgeWeightAttr(), true);

        while (it.hasNext()) {
            Edge edge = graph.getEdge(it.next());
            
            if (edge.getSourceNode().equals(edge.getTargetNode())) {
                logger.warn(
                        "Self-loop edge: " +
                        " [" + edge + "]"
                );
            }
            
            double value = edge.getDouble(params.getEdgeWeightAttr());
            if (Double.isNaN(value)) {
                logger.warn(
                    "Omitting NaN value for edge: " +
                    edge.getSourceNode().getString(params.getNodeLabelAttr()) + " -> " +
                    edge.getTargetNode().getString(params.getNodeLabelAttr()) + 
                    " [" + edge + "]"
                );
            } else {
                VisualNode fromNode = nodesToVisuals.get(edge.getSourceNode());
                VisualNode toNode = nodesToVisuals.get(edge.getTargetNode());

                VisualEdge visualEdge;
                if (edgeSplinePoints == null) {
                    visualEdge = new LineVisualEdge(this, edge, fromNode, toNode);
                } else {
                    visualEdge = new BSplineVisualEdge(this, edge, fromNode, toNode, edgeSplinePoints[edge.getRow()], showPoints);
                }
                visualEdge.update();
                edgeLayer.addChild(visualEdge);

                visualEdges.add(visualEdge);
                edgesToVisuals.put(edge, visualEdge);
            }
        }
    }

    private PBounds getNodesBounds() {
        if (nodeBounds == null) {
//            nodeBounds = new PBounds(
//                    0, 0, (xStats.max - xStats.min) / 2, (yStats.max - yStats.min) / 2
//            );
            PBounds b = null;
            for (VisualNode node : visualNodes) {
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

    public FlowMapParams getParams() {
        return params;
    }

    public double getMaxEdgeWidth() {
        return params.getMaxEdgeWidth();
    }

//    private static final Insets contentInsets = new Insets(10, 10, 10, 10);
    
//    private Insets getContentInsets() {
//        return contentInsets;
//    }
    
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
        logger.info("Fit in camera view: Bounding box: " + boundRect);
        boundRect = (PBounds)getCamera().globalToLocal(boundRect);
//        PiccoloUtils.setViewPaddedBounds(getCamera(), boundRect, new Insets(10, 10, 10, 10));
        getCamera().animateViewToCenterBounds(boundRect, true, 0);
    }

    public String getLabelAttr() {
        return params.getNodeLabelAttr();
    }

    public void showTooltip(PNode component, Point2D pos) {
        final PBounds cameraBounds = getCamera().getBoundsReference();
        double maxLabelWidth = cameraBounds.getWidth() - pos.getX();
        if (component instanceof VisualNode) {
            VisualNode vnode = (VisualNode) component;
            tooltipBox.setText(
                    wordWrapLabel(vnode.getFullLabel(), maxLabelWidth),
                    ""
//			        "Outgoing " + selectedFlowAttrName + ": " + graph.getOutgoingTotal(fnode.getId(), selectedFlowAttrName) + "\n" +
//			        "Incoming " + selectedFlowAttrName + ": " + graph.getIncomingTotal(fnode.getId(), selectedFlowAttrName)
                    ,
                    "");
        } else if (component instanceof VisualEdge) {
            VisualEdge edge = (VisualEdge) component;
            tooltipBox.setText(
                    wordWrapLabel(edge.getLabel(), maxLabelWidth),
                    params.getEdgeWeightAttr() + ": ", Double.toString(edge.getEdgeWeight()));
        } else {
            return;
        }
        final PBounds tooltipBounds = tooltipBox.getBoundsReference();
        double x = pos.getX();
        double y = pos.getY();
        pos = new Point2D.Double(x, y);
        getCamera().viewToLocal(pos);
        x = pos.getX();
        y = pos.getY();
        if (x + tooltipBounds.getWidth() > cameraBounds.getWidth()) {
            final double _x = pos.getX() - tooltipBounds.getWidth() - 8;
            if (cameraBounds.getX() - _x < x + tooltipBounds.getWidth() - cameraBounds.getMaxX()) {
                x = _x;
            }
        }
        if (y + tooltipBounds.getHeight() > cameraBounds.getHeight()) {
            final double _y = pos.getY() - tooltipBounds.getHeight() - 8;
            if (cameraBounds.getY() - _y < y + tooltipBounds.getHeight() - cameraBounds.getMaxY()) {
                y = _y;
            }
        }
        pos.setLocation(x + 8, y + 8);
        tooltipBox.setPosition(pos.getX(), pos.getY());
        tooltipBox.setVisible(true);
    }

    private String wordWrapLabel(String label, double maxWidth) {
        FontMetrics fm = jFlowMap.getGraphics().getFontMetrics();
        int width = SwingUtilities.computeStringWidth(fm, label);
        if (width > maxWidth) {
            StringBuilder sb = new StringBuilder();
            StringBuilder line = new StringBuilder();
            for (String word : label.split(" ")) {
                line.append(word);
                int w = SwingUtilities.computeStringWidth(fm, line.toString());
                if (w > maxWidth) {
                    int newLength = line.length() - word.length();
                    if (newLength > 0) {        // wrap line only if there are more than one words
                        line.setLength(newLength);  // remove last word
                        sb.append(line).append("\n");
                        line.setLength(0);
                        line.append(word);
                    } else {
                        sb.append(line);        // TODO: wordWrapLabel: implement hyphenation
                    }
                }
                line.append(" ");
            }
            label = sb.toString();
        }
        return label;
    }

    public void hideTooltip() {
        tooltipBox.setVisible(false);
    }
    
    public VisualNode getSelectedNode() {
        return (VisualNode) getAttribute(Attributes.NODE_SELECTION.name());
    }

    public void setSelectedNode(VisualNode visualNode) {
        VisualNode old = getSelectedNode();
        if (old != null) {
            old.setSelected(false);
        }
        if (visualNode != null) {
            visualNode.setSelected(true);
        }
        addAttribute(Attributes.NODE_SELECTION.name(), visualNode);  // will fire a propertyChange event
    }

    private void initModelChangeListeners(FlowMapParams model) {
    	model.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				String prop = evt.getPropertyName();
				if (prop.equals(FlowMapParams.PROPERTY_AUTO_ADJUST_COLOR_SCALE)  ||
					prop.equals(FlowMapParams.PROPERTY_EDGE_ALPHA)  ||
					prop.equals(FlowMapParams.PROPERTY_DIRECTION_MARKER_ALPHA)  ||
					prop.equals(FlowMapParams.PROPERTY_FILL_EDGES_WITH_GRADIENT) ||
					prop.equals(FlowMapParams.PROPERTY_SHOW_DIRECTION_MARKERS)  ||
					prop.equals(FlowMapParams.PROPERTY_USE_PROPORTIONAL_DIRECTION_MARKERS)  ||
					prop.equals(FlowMapParams.PROPERTY_DIRECTION_MARKER_SIZE)
				) {
					updateEdgeColors();
				} else if (prop.equals(FlowMapParams.PROPERTY_MAX_EDGE_WIDTH)) {
					updateEdgeWidths();
				} else if (prop.equals(FlowMapParams.PROPERTY_VALUE_FILTER_MIN) ||
						prop.equals(FlowMapParams.PROPERTY_VALUE_FILTER_MAX))
				{
	                updateEdgeVisibility();
	                updateEdgeColors();
				} else if (prop.equals(FlowMapParams.PROPERTY_EDGE_LENGTH_FILTER_MIN) ||
						prop.equals(FlowMapParams.PROPERTY_EDGE_LENGTH_FILTER_MAX))
				{
	                updateEdgeVisibility();
				} else if (prop.equals(FlowMapParams.PROPERTY_SHOW_NODES)) {
				    updateNodeVisibility();
				}
			}
		});
    }

    @SuppressWarnings("unchecked")
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
        for (VisualEdge ve : visualEdges) {
            ve.updateVisibility();
        }
//        for (VisualNode vn : visualNodes) {
//            vn.updatePickability();
//        }
    }

    private void updateNodeVisibility() {
        for (VisualNode vn : visualNodes) {
            vn.updateVisibility();
        }
    }

    private void updateEdgeWidths() {
        for (VisualEdge ve : visualEdges) {
            ve.updateEdgeWidth();
        }
    }

    public PCamera getCamera() {
        return jFlowMap.getCanvas().getCamera();
    }
    
    public void resetBundling() {
        bundled = false;
        createEdges();
        repaint();
    }

    public void bundleEdges(ForceDirectedBundlerParameters bundlerParams) {
        final ProgressTracker pt = new ProgressTracker();
        final ForceDirectedEdgeBundler bundler =
                new ForceDirectedEdgeBundler(graph, 
                        params.getXNodeAttr(), params.getYNodeAttr(), 
                        params.getEdgeWeightAttr(), 
                        bundlerParams);
        ProgressWorker worker = new ProgressWorker(pt) {
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
        };
        ProgressDialog dialog = new ProgressDialog(jFlowMap.getApp(), "Edge Bundling", worker, true);
        pt.addProgressListener(dialog);
        pt.addTaskCompletionListener(new TaskCompletionListener() {
            public void taskCompleted(int taskId) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        createEdges(bundler.getEdgePoints(), false);
                        bundled = true;
                        repaint();
                    }
                });
            }
        });
        worker.start();
        dialog.setVisible(true);
    }

//    public void aggregateBundledEdges() {
//        if (!isBundled()) {
//            return;
//        }
////        final ProgressTracker pt = new ProgressTracker();
////        AggregatedEdges agg = new AggregatedEdges();
////        agg.aggregate();
//    }

    public ClusterNode<VisualNode> getRootCluster() {
        return rootCluster;
    }

    public List<VisualNodeDistance> getNodeDistanceList() {
        return nodeDistanceList;
    }

    public double getMaxNodeDistance() {
//        if (Double.isNaN(maxNodeDistance)) {
//            maxNodeDistance = VisualNodeDistance.findMaxDistance(nodeDistanceList);
//        }
        return maxNodeDistance;
    }

    public double getEuclideanMaxNodeDistance() {
        return euclideanMaxNodeDistance;
    }
    
    public double getClusterDistanceThreshold() {
        return clusterDistanceThreshold;
    }

    public void setClusterDistanceThreshold(double value) {
        this.clusterDistanceThreshold = value;
        updateClusters();
    }

    public double getEuclideanClusterDistanceThreshold() {
        return euclideanClusterDistanceThreshold;
    }
    
    public void setEuclideanClusterDistanceThreshold(double value) {
        this.euclideanClusterDistanceThreshold = value;
        updateClusters();
    }
    
    public boolean hasClusters() {
        return rootCluster != null;
    }
    
    public boolean hasEuclideanClusters() {
        return euclideanRootCluster != null;
    }
    
    public boolean hasJoinedEdges() {
        return flowMapBeforeJoining != null;        
    }
    
    public VisualNodeCluster getNodeCluster(VisualNode node) {
        return nodesToClusters.get(node);
    }

    public void updateClusters() {
        removeClusterTags();  // to remove cluster tags for those nodes which were excluded from clustering
        
        List<VisualNodeCluster> clusters;
        if (rootCluster == null) {
            clusters = Collections.emptyList();
        } else {
            List<List<VisualNode>> nodeClusterLists =
                ClusterSetBuilder.getClusters(rootCluster, clusterDistanceThreshold);
            if (euclideanRootCluster != null) {
                nodeClusterLists = VisualNodeCluster.combineClusters( 
                        nodeClusterLists,
                        ClusterSetBuilder.getClusters(euclideanRootCluster, euclideanClusterDistanceThreshold)
                );
            }
            clusters = VisualNodeCluster.createClusters(nodeClusterLists, clusterDistanceThreshold);
        }
        this.visualNodeClusters = clusters;
        
        this.nodesToClusters = Maps.newHashMap();
        for (VisualNodeCluster cluster : clusters) {
            for (VisualNode node : cluster) {
                nodesToClusters.put(node, cluster);
            }
        }
    }
    
    public List<VisualNodeCluster> getVisualNodeClusters() {
        if (!hasClusters()) {
            throw new IllegalStateException("The flow map is not clustered");
        }
        return Collections.unmodifiableList(visualNodeClusters);
    }

    public int getNumberOfClusters() {
        if (visualNodeClusters == null) {
            return 0;
        }
        return visualNodeClusters.size();
    }

    private void removeClusterTags() {
        for (VisualNode node : visualNodes) {
            node.setClusterTag(null);
        }
    }

    public void clusterNodes(NodeDistanceMeasure distanceMeasure, Linkage linkage,
            boolean combineWithEuclideanClusters) {
        logger.info("Clustering nodes");
        HierarchicalClusterer<VisualNode> clusterer = 
//            new HierarchicalClusterer<VisualNode>(distanceMeasure, Linkage.SINGLE);
            new HierarchicalClusterer<VisualNode>(distanceMeasure, linkage);
        
        List<VisualNode> items = distanceMeasure.filterNodes(visualNodes);
 
        ProgressTracker tracker = new ProgressTracker();
        DistanceMatrix<VisualNode> distances = clusterer.makeDistanceMatrix(items, tracker);
        nodeDistanceList = VisualNodeDistance.makeDistanceList(items, distances);
        rootCluster = clusterer.cluster(items, distances, tracker);
        maxNodeDistance = findMaxClusterDist(rootCluster);
        clusterDistanceThreshold = maxNodeDistance / 2;
        if (combineWithEuclideanClusters) {
            euclideanRootCluster = HierarchicalClusterer.cluster(
                    items, NodeDistanceMeasure.EUCLIDEAN, Linkage.COMPLETE, new ProgressTracker()
            );
            euclideanMaxNodeDistance = findMaxClusterDist(euclideanRootCluster);
            euclideanClusterDistanceThreshold = euclideanMaxNodeDistance / 2;
        } else {
            euclideanRootCluster = null;
            euclideanMaxNodeDistance = Double.NaN;
            euclideanClusterDistanceThreshold = 0;
        }
        updateClusters();
    }

    private static <T> double findMaxClusterDist(ClusterNode<T> root) {
        class Finder extends ClusterVisitorAdapter<T> {
            double maxDist = Double.NaN;
            @Override
            public void betweenChildren(ClusterNode<T> cn) {
                if (Double.isNaN(maxDist)  || cn.getDistance() > maxDist) {
                    maxDist = cn.getDistance();
                }
            }
        }
        Finder finder = new Finder();
        ClusterNode.traverseClusters(root, finder);
        return finder.maxDist;
    }

    private VisualFlowMap flowMapBeforeJoining = null;
    
    public void setOriginalVisualFlowMap(VisualFlowMap originalVisualFlowMap) {
        this.flowMapBeforeJoining = originalVisualFlowMap;
    }

    public void joinClusterEdges() {
//        for (VisualNodeCluster cluster : visualNodeClusters) {
//            Point2D centroid = GeomUtils.centroid(
//                    Iterators.transform(
//                        cluster.iterator(), VisualNode.TRANSFORM_NODE_TO_POSITION
//                    )
//            );
//            PPath marker = new PPath(new Rectangle2D.Double(centroid.getX() - 4, centroid.getY() - 4, 8, 8));
//            marker.setPaint(Color.BLUE);
//            addChild(marker);
//        }
        
        Graph clusteredGraph = VisualNodeCluster.createClusteredFlowMap(visualNodeClusters);
        VisualFlowMap clusteredFlowMap = jFlowMap.createVisualFlowMap(
                JFlowMap.DEFAULT_EDGE_WEIGHT_ATTR_NAME,
                JFlowMap.DEFAULT_NODE_LABEL_ATTR_NAME,
                JFlowMap.DEFAULT_NODE_X_ATTR_NAME,
                JFlowMap.DEFAULT_NODE_Y_ATTR_NAME,
                0,
                clusteredGraph,
                areaMap == null ? null : (VisualAreaMap)areaMap.clone()
        );
        clusteredFlowMap.setOriginalVisualFlowMap(this);
        jFlowMap.setVisualFlowMap(clusteredFlowMap);
        jFlowMap.getControlPanel().loadVisualFlowMap(clusteredFlowMap);
    }

    public void resetClusters() {
        removeClusterTags();
        rootCluster = null;
        euclideanRootCluster = null;
        visualNodeClusters = null;
    }
    
    public void resetJoinedNodes() {
        if (flowMapBeforeJoining != null) {
            jFlowMap.setVisualFlowMap(flowMapBeforeJoining);
            jFlowMap.getControlPanel().loadVisualFlowMap(flowMapBeforeJoining);
        }
    }

    public void setNodeClustersToShow(List<VisualNodeCluster> nodeClustersToShow) {
        if (visualNodeClusters == null) {
            return;
        }
        for (VisualNodeCluster cluster : visualNodeClusters) {
            final boolean visible;
            if (nodeClustersToShow.isEmpty()) {
                visible = true;
            } else {
                visible = nodeClustersToShow.contains(cluster);
            }
            cluster.setVisible(visible);
        }
        updateEdgeVisibility();
    }

}
