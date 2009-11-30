package jflowmap.aggregation;

import java.util.List;

import ch.unifr.dmlib.cluster.ClusterNode;
import ch.unifr.dmlib.cluster.DefaultDistanceMatrix;
import ch.unifr.dmlib.cluster.DistanceMeasure;
import ch.unifr.dmlib.cluster.Linkage;

/**
 * @author Ilya Boyandin
 */
class EdgeSegmentDistanceMatrix extends DefaultDistanceMatrix<EdgeSegment> {

    public EdgeSegmentDistanceMatrix(List<EdgeSegment> items,
            DistanceMeasure<EdgeSegment> distanceMeasure,
            Linkage<EdgeSegment> linkage) {
        super(items, distanceMeasure, linkage);
    }

    @Override
    protected ClusterNode<EdgeSegment> mergeClusterNodes(
            ClusterNode<EdgeSegment> left, ClusterNode<EdgeSegment> right, double dist) {
        EdgeSegment leftItem = left.getItem();
        EdgeSegment rightItem = right.getItem();
        EdgeSegment aggregatedItem = leftItem.aggregateWith(rightItem);


        // TODO: update adjacent segments (and corresponding ClusterNodes)
//        for (EdgeSegment ladj : leftItem.getLeftAdjacentSegments()) {
//            // Create new and replace
//            ladj.set
//        }


        // TODO: ? somehow let the corresponding SegmentedEdges know that the segments were merged

        return new ClusterNode<EdgeSegment>(aggregatedItem, left, right, dist);
    }

}
