package jflowmap.visuals;

import java.awt.Color;
import java.awt.Paint;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jflowmap.models.FlowMapGraphBuilder;
import jflowmap.util.ColorUtils;
import jflowmap.util.GeomUtils;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import ch.unifr.dmlib.cluster.ClusterNode;
import ch.unifr.dmlib.cluster.ClusterSetBuilder;

import com.google.common.collect.Iterators;

/**
 * @author Ilya Boyandin
 */
public class VisualNodeCluster implements Iterable<VisualNode> {
    
    private ClusterTag tag;
    private List<VisualNode> nodes;
    
    private VisualNodeCluster(int id, Paint color, Iterator<VisualNode> it) {
        this.nodes = new ArrayList<VisualNode>();
        this.tag = ClusterTag.createFor(id, color);
        Iterators.addAll(nodes, it);
        updateClusterTags();
    }
    
    private void updateClusterTags() {
        for (VisualNode node : nodes) {
            node.setClusterTag(tag);
        }
    }
    
    public ClusterTag getTag() {
        return tag;
    }
    
    public List<VisualEdge> getIncomingEdges() {
        List<VisualEdge> edges = new ArrayList<VisualEdge>(); 
        for (VisualNode node : nodes) {
            edges.addAll(node.getIncomingEdges());
        }
        return edges;
    }

    public List<VisualEdge> getOutgoingEdges() {
        List<VisualEdge> edges = new ArrayList<VisualEdge>(); 
        for (VisualNode node : nodes) {
            edges.addAll(node.getIncomingEdges());
        }
        return edges;
    }
    
    public Iterator<VisualNode> iterator() {
        return nodes.iterator();
    }
    
    public int size() {
        return nodes.size();
    }
    
    public static VisualNodeCluster createFor(int id, Paint color, Iterator<VisualNode> it) {
        return new VisualNodeCluster(id, color, it);
    }

    public static List<VisualNodeCluster> createClusters(
            ClusterNode<VisualNode> rootCluster, double clusterDistanceThreshold) {
        
        List<List<VisualNode>> nodeClusterLists = ClusterSetBuilder.getClusters(rootCluster, clusterDistanceThreshold);
//        List<List<VisualNode>> clusters = Lists.newArrayList(Iterators.filter(
//                ClusterSetBuilder.getClusters(rootCluster, clusterDistanceThreshold).iterator(),
//                new Predicate<List<VisualNode>>() {
//                    public boolean apply(List<VisualNode> nodes) {
//                        return (nodes.size() > 1);
//                    }
//                }
//        ));
        
        final int numClusters = nodeClusterLists.size();
        
        Color[] colors = ColorUtils.createCategoryColors(numClusters, 150);
        List<VisualNodeCluster> nodeClusters = new ArrayList<VisualNodeCluster>(numClusters);
        for (int i = 0; i < numClusters; i++) {
            List<VisualNode> nodes = nodeClusterLists.get(i);
            nodeClusters.add(VisualNodeCluster.createFor(i + 1, colors[i], nodes.iterator()));
        }
        return nodeClusters;
    }
    
    
    public static Graph createClusteredFlowMap(List<VisualNodeCluster> clusters) {
        
        FlowMapGraphBuilder builder =
            new FlowMapGraphBuilder().withCumulativeEdges();
        
        // Create (visualNode->cluster node) mapping
        Map<VisualNode, Node> visualToNode = new HashMap<VisualNode, Node>();
        for (VisualNodeCluster cluster : clusters) {
            Point2D centroid = GeomUtils.centroid(
                    Iterators.transform(
                        cluster.iterator(), VisualNode.TRANSFORM_NODE_TO_POSITION
                    )
            );
            Node node = builder.addNode(centroid);
            for (VisualNode visualNode : cluster) {
                visualToNode.put(visualNode, node);
            }
        }

        // Edges between clusters 
        for (VisualNodeCluster cluster : clusters) {
            for (VisualNode node : cluster) {
                for (VisualEdge visualEdge : node.getEdges()) {
                    builder.addEdge(
                            visualToNode.get(visualEdge.getSourceNode()),
                            visualToNode.get(visualEdge.getTargetNode()),
                            visualEdge.getEdgeWeight());
                    
                }
            }
        }
        
        return builder.build();
    }
    

    
}
