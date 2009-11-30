package jflowmap.visuals;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import ch.unifr.dmlib.cluster.DistanceMatrix;

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
        for (int i = 0; i < distMatrix.getNumOfItems(); i++)
        for (int j = 0; j < i; j++) {
            list.add(new VisualNodeDistance(items.get(i), items.get(j), distMatrix.distance(i, j)));
        }
        return list;
    }

    public static double findMaxDistance(List<VisualNodeDistance> distances) {
        double max = Double.NaN;
        if (distances.size() > 0) {
            max = 0;
            for (VisualNodeDistance d : distances) {
                if (!Double.isInfinite(d.getDistance()) && d.getDistance() > max) max = d.getDistance();
            }
        }
        return max;
    }

    public static final Comparator<VisualNodeDistance> FROM_LABEL_COMPARATOR = new Comparator<VisualNodeDistance>() {
        public int compare(VisualNodeDistance o1, VisualNodeDistance o2) {
            String src1 = o1.getSource().getLabel();
            String src2 = o2.getSource().getLabel();
            int c = src1.compareTo(src2);
            if (c == 0) {
                String trg1 = o1.getTarget().getLabel();
                String trg2 = o2.getTarget().getLabel();
                c = trg1.compareTo(trg2);
            }
            return c;
        }
    };
}
