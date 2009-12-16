package jflowmap.aggregation;

import java.util.List;

import at.fhj.utils.misc.ProgressTracker;
import ch.unifr.dmlib.cluster.AbstractDistanceMatrix;
import ch.unifr.dmlib.cluster.ClusterNode;
import ch.unifr.dmlib.cluster.DistanceMeasure;

/**
 * @author Ilya Boyandin
 */
class EdgeSegmentDistanceMatrix extends AbstractDistanceMatrix<EdgeSegment> {

    public EdgeSegmentDistanceMatrix(List<EdgeSegment> items,
            DistanceMeasure<EdgeSegment> distanceMeasure,
            double maxMergeableDistance) {
        super(items, distanceMeasure, null, maxMergeableDistance);
    }

    @Override
    protected ClusterNode<EdgeSegment> mergeClusterNodes(
            ClusterNode<EdgeSegment> cn1, ClusterNode<EdgeSegment> cn2, double dist) {
        EdgeSegment item1 = cn1.getItem();
        EdgeSegment item2 = cn2.getItem();

//        assert item1.canBeAggregatedWith(item2);

        EdgeSegment aggregate = item1.aggregateWith(item2);

//        System.out.println("Merge item " + System.identityHashCode(item1) + " with " + System.identityHashCode(item2));

        item1.replaceWith(aggregate);
        item2.replaceWith(aggregate);
        // TODO: update distances for adjacent segments which were changed after replacing with aggregate
        // Problem: how to find the cluster node which corresponds to the adjacent segments?

        assert aggregate.checkParentEdgesSegmentConsecutivity();

        return new ClusterNode<EdgeSegment>(aggregate, cn1, cn2, dist);
    }

    @Override
    public void calc(ProgressTracker progress) {
    }

    @Override
    protected double getDistanceBetweenClusterNodes(int i, int j) {
        return getDistanceMeasure().distance(getNode(i).getItem(), getNode(j).getItem());
    }

    @Override
    protected void updateDistances(int mergedNode1, int mergedNode2,
            ClusterNode<EdgeSegment> newNode) {
    }


}
