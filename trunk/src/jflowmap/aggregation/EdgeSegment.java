package jflowmap.aggregation;

import java.util.List;

import jflowmap.geom.GeomUtils;
import jflowmap.geom.FPoint;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * EdgeSegment should be mutable, otherwise it's difficult to propagate
 * to the cluster tree  the changes made to the segments adjacent to
 * those which are merged.
 *
 * @author Ilya Boyandin
 */
public class EdgeSegment {

    private FPoint a;
    private FPoint b;
    private final double weight;
    private final List<SegmentedEdge> parents;

    public EdgeSegment(FPoint a, FPoint b, double weight, SegmentedEdge parent) {
        this.a = a;
        this.b = b;
        this.weight = weight;
        this.parents = ImmutableList.of(parent);
    }

    public EdgeSegment(FPoint a, FPoint b, double weight, Iterable<SegmentedEdge> parents) {
        this.a = a;
        this.b = b;
        this.weight = weight;
        this.parents = ImmutableList.copyOf(parents);
    }

    public FPoint getA() {
        return a;
    }

    public FPoint getB() {
        return b;
    }

    public void setA(FPoint newA) {
        if (newA.equals(a)) {
            return;
        }
        if (a.isFixed()) {
            throw new IllegalStateException("A is a fixed point");
        }
        this.a = newA;
    }

    public void setB(FPoint newB) {
        if (newB.equals(b)) {
            return;
        }
        if (b.isFixed()) {
            throw new IllegalStateException("B is a fixed point");
        }
        this.b = newB;
    }

    public double getWeight() {
        return weight;
    }

    public List<SegmentedEdge> getParents() {
        return parents;
    }

    public boolean isConsecutiveFor(EdgeSegment seg) {
        return seg.getB().equals(getA());
    }

    public void replaceWith(EdgeSegment newSegment) {
//        System.out.println("Replace segment " + System.identityHashCode(this) + " with " + System.identityHashCode(newSegment));
        for (SegmentedEdge se : parents) {
//            System.out.println(" >> Replace segment " + System.identityHashCode(this) + " with " + System.identityHashCode(newSegment) +
//                    " in edge " + System.identityHashCode(se));
            se.replaceSegment(this, newSegment);
        }
    }

//    public List<EdgeSegment> getLeftAdjacentSegments() {
//        return ImmutableList.copyOf(Iterables.transform(parents, new Function<SegmentedEdge, EdgeSegment>() {
//            // TODO: remove nulls from the list of adjacent segments
//            @Override
//            public EdgeSegment apply(SegmentedEdge se) {
//                return se.getLeftAdjacent(EdgeSegment.this);
//            }
//        }));
//    }
//
//    public List<EdgeSegment> getRightAdjacentSegments() {
//        return ImmutableList.copyOf(Iterables.transform(parents, new Function<SegmentedEdge, EdgeSegment>() {
//            @Override
//            public EdgeSegment apply(SegmentedEdge se) {
//                return se.getRightAdjacent(EdgeSegment.this);
//            }
//        }));
//    }

    public boolean sharesAParentWith(EdgeSegment other) {
        List<SegmentedEdge> otherParents = other.getParents();
        for (SegmentedEdge parent : getParents()) {
            if (otherParents.contains(parent)) {
                return true;
            }
        }
        return false;
    }

    public double length() {
        return a.distanceTo(b);
    }

    public boolean canBeAggregatedWith(EdgeSegment other) {
        return  !(a.isFixed()  &&  other.a.isFixed()  &&  !a.equals(other.getA()))  &&
                !(b.isFixed()  &&  other.b.isFixed()  &&  !b.equals(other.b))  &&
//                (length() != 0  &&  other.length() != 0)  &&
                !sharesAParentWith(other);
    }

    public EdgeSegment aggregateWith(EdgeSegment other) {
//        return aggregate(Arrays.asList(this, other));
        if (!canBeAggregatedWith(other)) {
            throw new IllegalArgumentException("Segments cannot be aggregated");
        }

        return new EdgeSegment(
                aggregate(a, other.getA()), aggregate(b, other.getB()), weight + other.weight,
//                Iterables.concat(getParents(), other.getParents())
                getParentsOf(this, other)
        );
    }

    private FPoint aggregate(FPoint p1, FPoint p2) {
        if (p1.isFixed()  &&  p2.isFixed()  &&  !p1.equals(p2)) {
            throw new IllegalArgumentException("Both points are fixed; cannot aggregate");
        }
        if (p1.isFixed()) {
            return p1;
        } else if (p2.isFixed()) {
            return p2;
        } else {
            return new FPoint(GeomUtils.midpoint(p1.getPoint(), p2.getPoint()), false);
        }
    }

    private static List<SegmentedEdge> getParentsOf(EdgeSegment ... segs) {
        List<SegmentedEdge> union = Lists.newArrayList();
        for (EdgeSegment seg : segs) {
            for (SegmentedEdge se : seg.getParents()) {
                if (!union.contains(se)) {
                    union.add(se);
                }
            }
        }
        return union;
    }

//    public static EdgeSegment aggregate(List<EdgeSegment> segments) {
//        double sumWeight = 0;
//        for (EdgeSegment seg : segments) {
//            sumWeight += seg.getWeight();
//        }
//        SPoint newA = null;
//        SPoint newB = null;
//        boolean aFixed = false;
//        boolean bFixed = false;
//        for (EdgeSegment seg : segments) {
//            if (seg.isaFixed()) {
//                if (newA != null) {
//                    throw new IllegalArgumentException("More than one segments have a fixed A point");
//                }
//                newA = seg.getA();
//                aFixed = true;
//            }
//            if (seg.isbFixed()) {
//                if (newB != null) {
//                    throw new IllegalArgumentException("More than one segments have a fixed B point");
//                }
//                newB = seg.getB();
//                bFixed = true;
//            }
//        }
//        if (newA == null) {
//            newA = GeomUtils.centroid(Iterators.transform(segments.iterator(), EdgeSegment.TRANSFORM_TO_A));
//        }
//        if (newB == null) {
//            newB = GeomUtils.centroid(Iterators.transform(segments.iterator(), EdgeSegment.TRANSFORM_TO_B));
//        }
//        return new EdgeSegment(
//                newA,
//                aFixed,
//                newB,
//                bFixed,
//                sumWeight,
//                Iterables.concat(Iterables.transform(segments, TRANSFORM_TO_PARENTS))
//        );
//    }

    public static final Function<EdgeSegment, FPoint> TRANSFORM_TO_A = new Function<EdgeSegment, FPoint>() {
        @Override
        public FPoint apply(EdgeSegment segment) {
            return segment.getA();
        }
    };

    public static final Function<EdgeSegment, FPoint> TRANSFORM_TO_B = new Function<EdgeSegment, FPoint>() {
        @Override
        public FPoint apply(EdgeSegment segment) {
            return segment.getB();
        }
    };

    public static final Function<EdgeSegment, List<SegmentedEdge>> TRANSFORM_TO_PARENTS =
        new Function<EdgeSegment, List<SegmentedEdge>>() {
        @Override
        public List<SegmentedEdge> apply(EdgeSegment segment) {
            return segment.getParents();
        }
    };


    @Override
    public String toString() {
        return "EdgeSegment [" +
                "a=" + a + ", b=" + b +
                ", parents.size=" + parents.size() + ", weight=" + weight +
                "]";
    }

}
