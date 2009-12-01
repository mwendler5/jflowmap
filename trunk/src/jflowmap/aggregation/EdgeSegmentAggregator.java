package jflowmap.aggregation;

import java.util.List;

import jflowmap.geom.Point;
import jflowmap.models.FlowMapModel;

import org.apache.log4j.Logger;

import prefuse.data.Edge;
import at.fhj.utils.misc.ProgressTracker;
import ch.unifr.dmlib.cluster.ClusterNode;
import ch.unifr.dmlib.cluster.DistanceMatrix;
import ch.unifr.dmlib.cluster.DistanceMeasure;
import ch.unifr.dmlib.cluster.HierarchicalClusterer;
import ch.unifr.dmlib.cluster.Linkage;

import com.google.common.collect.Lists;

/**
 *
     Q: How to take the directions into account (not to aggregate edges going into opposite directions)?
        Maybe using edge compatibility? Add an ad hoc one? or will the one used for bundling suffice?

     Q: How to show directions with color after aggregation?

 * @author Ilya Boyandin
 */
public class EdgeSegmentAggregator {

    private static Logger logger = Logger.getLogger(EdgeSegmentAggregator.class);

    private final FlowMapModel flowMapModel;

    private List<EdgeSegment> segments;
    private List<SegmentedEdge> segmentedEdges;

    private List<EdgeSegment> aggregatedSegments;

    public EdgeSegmentAggregator(FlowMapModel flowMapModel) {
        this.flowMapModel = flowMapModel;
    }

    public void aggregate(ProgressTracker pt) {
        initSegments();
        List<ClusterNode<EdgeSegment>> nodes =
            HierarchicalClusterer
                .createWith(DISTANCE_MEASURE, LINKAGE)
                .withDistanceMatrixFactory(DISTANCE_MATRIX_FACTORY)
//                .withMaxMergeableDistance(Double.MAX_VALUE)  // disallows POSITIVE_INFINITY
//                .withMaxMergeableDistance(.9575)
                .withMaxMergeableDistance(150)
                .build()
                .cluster(segments, pt);

//        double maxDistance = MaxAllowedDistanceFinder.find(root) / 2.0;


//        List<List<EdgeSegment>> clusters = ClusterSetBuilder.getClusters(root, maxDistance);
//        logger.debug("MaxDistance: " + maxDistance);
//        logger.debug("NumOfClusters: " + clusters.size());
//        logger.debug(aggregatedSegments);
//
//        List<EdgeSegment> aggSegs = Lists.newArrayListWithCapacity(clusters.size());
//        // TODO: write a special ClusterSetBuilder for it
//        for (List<EdgeSegment> cluster : clusters) {
//            aggSegs.add(EdgeSegment.aggregate(cluster));
//        }
//        this.aggregatedSegments = aggSegs;


        List<EdgeSegment> aggSegs = Lists.newArrayListWithCapacity(nodes.size());
        for (ClusterNode<EdgeSegment> node : nodes) {
            aggSegs.add(node.getItem());    // items in the top-level nodes are already aggregated
        }
//        List<EdgeSegment> aggSegs = Lists.newArrayListWithCapacity(nodes.size());
//        for (ClusterNode<EdgeSegment> node : nodes) {
//            final List<EdgeSegment> cluster = Lists.newArrayList();
//            node.traverse(new ClusterVisitor.Adapter<EdgeSegment>() {
//                @Override
//                public void betweenChildren(ClusterNode<EdgeSegment> node) {
//                    cluster.add(node.getItem());
//                }
//            });
//            logger.debug("Cluster size: " + cluster.size());
//            aggSegs.add(EdgeSegment.aggregate(cluster));
//        }

        if (logger.isDebugEnabled()) {
            logger.debug("Num of segments: " + segments.size());
            logger.debug("Num of clusters: " + aggSegs.size());
        }
        this.aggregatedSegments = aggSegs;

        if (!pt.isCancelled()) {
            pt.processFinished();
        }
    }

    public List<EdgeSegment> getAggregatedSegments() {
        return aggregatedSegments;
    }

//    /**
//     * Finds max distance between the cluster nodes in the tree on which
//     * there are no segments of the same edge in a cluster.
//     *
//     * @author Ilya Boyandin
//     */
//    private static class MaxAllowedDistanceFinder extends ClusterVisitor.Adapter<EdgeSegment> {
//        public static double find(ClusterNode<EdgeSegment> root) {
//            MaxAllowedDistanceFinder finder = new MaxAllowedDistanceFinder();
//            root.traverse(finder);
//            return finder.maxDistance;
//        }
//        double maxDistance = Double.NaN;
//        @Override
//        public void beforeChildren(ClusterNode<EdgeSegment> cn) {
//            if (cn.getDistance() > maxDistance  ||  Double.isNaN(maxDistance)) {
//                EdgeSegment leftItem = cn.getLeftChild().getItem();
//                EdgeSegment rightItem = cn.getRightChild().getItem();
//                if (!leftItem.sharesAParentWith(rightItem)) {
//                    maxDistance = cn.getDistance();
//                }
//            }
//        }
//    }

    private void initSegments() {
        int numEdges = flowMapModel.getGraph().getEdgeCount();

        segments = Lists.newArrayList();
        segmentedEdges = Lists.newArrayListWithExpectedSize(numEdges);

        for (int i = 0; i < numEdges; i++) {
            Edge edge = flowMapModel.getGraph().getEdge(i);
            List<Point> points = flowMapModel.getEdgeSubdivisionPoints(edge);
            SegmentedEdge segmentedEdge = new SegmentedEdge(edge);
            for (int pi = 0, psize = points.size(); pi <= psize; pi++) {
                Point a;
                Point b;
                boolean aFixed;
                boolean bFixed;
                if (pi == 0) {
                    a = flowMapModel.getEdgeSourcePoint(edge);
                    aFixed = true;
                } else {
                    a = points.get(pi - 1);
                    aFixed = false;
                }
                if (pi == psize) {
                    b = flowMapModel.getEdgeTargetPoint(edge);
                    bFixed = true;
                } else {
                    b = points.get(pi);
                    bFixed = false;
                }
                EdgeSegment seg = new EdgeSegment(
                        a, aFixed, b, bFixed,
                        flowMapModel.getEdgeWeight(edge), segmentedEdge
                );
                segments.add(seg);
                segmentedEdge.add(seg);
            }
            segmentedEdges.add(segmentedEdge);
        }
    }

    private static Linkage<EdgeSegment> LINKAGE = new Linkage<EdgeSegment>() {
        @Override
        public double link(
                ClusterNode<EdgeSegment> mergedNode,
                ClusterNode<EdgeSegment> node, double distanceToLeft,
                double distanceToRight, DistanceMatrix<EdgeSegment> distances) {
//            return DISTANCE_MEASURE.distance(
//                    EdgeSegment.aggregate(mergedNode.listItems()),
//                    EdgeSegment.aggregate(node.listItems()))
//            ;
            return
                DISTANCE_MEASURE.distance(
                        mergedNode.getItem(),
                        node.getItem()
                );
        }
    };

    private static DistanceMeasure<EdgeSegment> DISTANCE_MEASURE = new DistanceMeasure<EdgeSegment>() {
        @Override
        public double distance(EdgeSegment seg1, EdgeSegment seg2) {
            if (seg1.sharesAParentWith(seg2)) {
                return Double.POSITIVE_INFINITY;
            }
            if ((seg1.isaFixed()  &&  seg2.isaFixed()) ||  ((seg1.isbFixed()  &&  seg2.isbFixed()))) {
                return Double.POSITIVE_INFINITY;
            }
//            double l_avg = (seg1.length() + seg2.length())/2;
//            double sim = l_avg / (l_avg +
//                    seg1.getA().distanceTo(seg2.getA()) +
//                    seg1.getB().distanceTo(seg2.getB()));
//            return 1.0 - sim;
            return
                seg1.getA().distanceTo(seg2.getA()) +
                seg1.getB().distanceTo(seg2.getB());
        }
    };

    private static final DistanceMatrix.Factory<EdgeSegment> DISTANCE_MATRIX_FACTORY =
        new DistanceMatrix.Factory<EdgeSegment>() {
            @Override
            public DistanceMatrix<EdgeSegment> createFor(
                    List<EdgeSegment> items,
                    Linkage<EdgeSegment> linkage,
                    DistanceMeasure<EdgeSegment> measure,
                    double maxMergeableDistance) {
                return new EdgeSegmentDistanceMatrix(items, measure, linkage, maxMergeableDistance);
            }
        };

}
