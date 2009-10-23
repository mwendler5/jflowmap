package jflowmap.clustering;

import java.util.List;

import jflowmap.util.GeomUtils;
import jflowmap.visuals.VisualEdge;
import jflowmap.visuals.VisualNode;
import ch.unifr.dmlib.cluster.DistanceMeasure;

/**
 * @author Ilya Boyandin
 */
public class NodeSimilarityDistanceMeasure implements DistanceMeasure<VisualNode> {
    
    @Override
    public double distance(VisualNode n1, VisualNode n2) {
        return (D.IN.distance(n1, n2) + D.OUT.distance(n1, n2)) / 2;
    }

    public enum D implements DistanceMeasure<VisualNode> {
        IN {
            @Override
            List<VisualEdge> getEdges(VisualNode n) {
                return n.getIncomingEdges();
            }
            @Override
            VisualNode getOppositeNode(VisualEdge e) {
                return e.getSourceNode();
            }
        },
        OUT {
            @Override
            List<VisualEdge> getEdges(VisualNode n) {
                return n.getOutgoingEdges();
            }
            @Override
            VisualNode getOppositeNode(VisualEdge e) {
                return e.getTargetNode();
            }
        };
        
        abstract List<VisualEdge> getEdges(VisualNode n);

        abstract VisualNode getOppositeNode(VisualEdge n);
            
        @Override
        public double distance(VisualNode n1, VisualNode n2) {
            double numerator = 0;
            double denominator = 0;
            for (VisualEdge e1 : getEdges(n1))
            for (VisualEdge e2 : getEdges(n2)) {
                VisualNode t1 = getOppositeNode(e1);
                VisualNode t2 = getOppositeNode(e2);
                double w = e1.getValue() * e2.getValue();
                numerator += GeomUtils.distance(
                        t1.getValueX(), t1.getValueY(), t2.getValueX(), t2.getValueY()) * w;
                denominator += w;
            }
            return numerator / denominator;
        }
    }

}
