package jflowmap.aggregation;

import java.util.List;

import jflowmap.geom.Point;
import jflowmap.geom.Segment;
import prefuse.data.Graph;

import com.google.common.collect.Multimap;


/**
 * @author Ilya Boyandin
 */
public class AggregatedEdges {
    
    Multimap<Segment, SegmentedEdge> m;
    
    public static class SegmentedEdge {
        private List<Polyline> polylines;
    }

//    private List<SegmentedEdge> edges;
    
    
    private AggregatedEdges() {
        
    }
    
    /*
     Q: How to take the directions into account (not to aggregate edges going into opposite directions)? 
        Maybe using edge compatibility? Add an ad hoc one? or will the one used for bundling suffice?  
     
     Q: How to show directions with color after aggregation?
     */

    public static AggregatedEdges createFrom(Graph graph, Point[][] edgePoints) {
        return null;
    }

}
