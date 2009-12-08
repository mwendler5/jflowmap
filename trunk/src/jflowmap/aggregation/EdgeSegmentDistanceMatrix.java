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
            Linkage<EdgeSegment> linkage, double maxMergeableDistance) {
        super(items, distanceMeasure, linkage, maxMergeableDistance);
    }

    @Override
    protected ClusterNode<EdgeSegment> mergeClusterNodes(
            ClusterNode<EdgeSegment> cn1, ClusterNode<EdgeSegment> cn2, double dist) {
        EdgeSegment item1 = cn1.getItem();
        EdgeSegment item2 = cn2.getItem();
        EdgeSegment aggregate = item1.aggregateWith(item2);

//        System.out.println("Merge item " + System.identityHashCode(item1) + " with " + System.identityHashCode(item2));

        item1.replaceWith(aggregate);
        item2.replaceWith(aggregate);
        // TODO: update distances for adjacent segments which were changed after replacing with aggregate
        // Problem: how to find the cluster node which corresponds to the adjacent segments?


        return new ClusterNode<EdgeSegment>(aggregate, cn1, cn2, dist);
    }

}
