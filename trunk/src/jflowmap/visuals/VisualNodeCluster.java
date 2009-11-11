package jflowmap.visuals;

import java.awt.Color;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jflowmap.util.ColorUtils;
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
        
        List<List<VisualNode>> clusters = ClusterSetBuilder.getClusters(rootCluster, clusterDistanceThreshold);
//        List<List<VisualNode>> clusters = Lists.newArrayList(Iterators.filter(
//                ClusterSetBuilder.getClusters(rootCluster, clusterDistanceThreshold).iterator(),
//                new Predicate<List<VisualNode>>() {
//                    public boolean apply(List<VisualNode> nodes) {
//                        return (nodes.size() > 1);
//                    }
//                }
//        ));
        
        final int numClusters = clusters.size();
        Color[] colors = ColorUtils.createCategoryColors(numClusters, 150);

        List<VisualNodeCluster> nodeClusters = new ArrayList<VisualNodeCluster>(numClusters);
        int lastCluster = 0;
        for (int i = 0; i < numClusters; i++) {
            List<VisualNode> nodes = clusters.get(i);
            
            for (VisualNode visualNode : nodes) {
                
            }
            nodeClusters.add(VisualNodeCluster.createFor(lastCluster, colors[i], nodes.iterator()));
            lastCluster++;
        }
        
        return nodeClusters;
    }
    
    
}
