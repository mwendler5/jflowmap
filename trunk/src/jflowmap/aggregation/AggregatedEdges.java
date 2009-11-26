package jflowmap.aggregation;

import java.awt.geom.Point2D;
import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * @author Ilya Boyandin
 */
public class AggregatedEdges {
    
    public static class Segment {
        private List<Point2D> points;
        
        public Segment(Point2D[] points) {
            this.points = ImmutableList.of(points);
        }
    }

    public static class SegmentedEdge {
        private List<Segment> segments;
    }

    private List<SegmentedEdge> edges;
    
    private AggregatedEdges() {
    }
    
    /*
     Q: How to take into account the directions (not to aggregate edges going into opposite directions)? 
        Maybe using edge compatibility? Add an ad hoc one? or will the one used for bundling suffice?  
     
     Q: How to show directions with color after aggregation?
     */

    public static AggregatedEdges createFrom(Point2D[][] edgePoints) {
        
        return null;
    }

}
