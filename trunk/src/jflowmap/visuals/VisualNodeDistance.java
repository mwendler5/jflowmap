package jflowmap.visuals;

import java.util.ArrayList;
import java.util.List;

import ch.unifr.dmlib.cluster.HierarchicalClusterer.DistanceMatrix;

/**
 * @author Ilya Boyandin
 */
public class VisualNodeDistance implements Comparable<VisualNodeDistance> {

    private final VisualNode source;
    private final VisualNode target;
    private final double distance;
    
    public VisualNodeDistance(VisualNode source, VisualNode target, double distance) {
        this.source = source;
        this.target = target;
        this.distance = distance;
    }

    public VisualNode getSource() {
        return source;
    }
    
    public VisualNode getTarget() {
        return target;
    }
    
    public double getDistance() {
        return distance;
    }
    
    public int compareTo(VisualNodeDistance o) {
        return Double.compare(distance, o.distance);
    }

    public static List<VisualNodeDistance> makeDistanceList(List<VisualNode> items,
            DistanceMatrix<VisualNode> distMatrix) {
        List<VisualNodeDistance> list = new ArrayList<VisualNodeDistance>();
        for (int i = 0; i < distMatrix.getNumOfElements(); i++)
        for (int j = 0; j < i; j++) {
            list.add(new VisualNodeDistance(items.get(i), items.get(j), distMatrix.distance(i, j)));
        }
        return list;
    }
}
