package jflowmap.aggregation;

import java.util.List;

import jflowmap.geom.GeomUtils;
import jflowmap.geom.Point;
import jflowmap.util.Pair;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * EdgeSegment should be mutable, otherwise it's difficult to propagate
 * to the cluster tree  the changes made to the segments adjacent to the merged ones.
 *
 * @author Ilya Boyandin
 */
public class EdgeSegment {

    private Point a;
    private Point b;
    private final boolean aFixed;
    private final boolean bFixed;
    private final double weight;
    private final List<SegmentedEdge> parents;
    private final double length;

    public EdgeSegment(Point a, boolean aFixed, Point b, boolean bFixed, double weight, SegmentedEdge parent) {
        this.a = a;
        this.b = b;
        this.aFixed = aFixed;
        this.bFixed = bFixed;
        this.weight = weight;
        this.length = a.distanceTo(b);
        this.parents = ImmutableList.of(parent);
    }

    public EdgeSegment(Point a, boolean aFixed, Point b, boolean bFixed, double weight, Iterable<SegmentedEdge> parents) {
        this.a = a;
        this.b = b;
        this.aFixed = aFixed;
        this.bFixed = bFixed;
        this.weight = weight;
        this.length = a.distanceTo(b);
        this.parents = ImmutableList.copyOf(parents);
    }

    public Point getA() {
        return a;
    }

    public boolean isaFixed() {
        return aFixed;
    }

    public Point getB() {
        return b;
    }

    public boolean isbFixed() {
        return bFixed;
    }

    public void setA(Point newA) {
        if (aFixed) {
            throw new IllegalStateException("A is a fixed point");
        }
        this.a = newA;
    }

    public void setB(Point newB) {
        if (bFixed) {
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

    public void replaceWith(EdgeSegment newSegment) {
        System.out.println("Replace segment " + System.identityHashCode(this) + " with " + System.identityHashCode(newSegment));
        for (SegmentedEdge se : parents) {
            System.out.println(" >> Replace segment " + System.identityHashCode(this) + " with " + System.identityHashCode(newSegment) +
                    " in edge " + System.identityHashCode(se));
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
        return length;
    }

    public EdgeSegment aggregateWith(EdgeSegment other) {
//        return aggregate(Arrays.asList(this, other));

        Pair<Point, Boolean> newA = aggregate(a, aFixed, other.getA(), other.isaFixed());
        Pair<Point, Boolean> newB = aggregate(b, bFixed, other.getB(), other.isbFixed());

        return new EdgeSegment(
                newA.first(), newA.second(), newB.first(), newB.second(), weight + other.weight,
//                Iterables.concat(getParents(), other.getParents())
                getParentsOf(this, other)
        );
    }

    private Pair<Point, Boolean> aggregate(Point p1, boolean p1Fixed, Point p2, boolean p2Fixed) {
        if (p1Fixed  &&  p2Fixed) {
            throw new IllegalArgumentException("Both points are fixed; cannot aggregate");
        }
        Point newP;
        boolean newFixed;
        if (p1Fixed) {
            newP = p1;
            newFixed = true;
        } else if (p2Fixed) {
            newP = p2;
            newFixed = true;
        } else {
            newP = GeomUtils.midpoint(p1, p2);
            newFixed = false;
        }
        return Pair.of(newP, newFixed);
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
//        Point newA = null;
//        Point newB = null;
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

    public static final Function<EdgeSegment, Point> TRANSFORM_TO_A = new Function<EdgeSegment, Point>() {
        @Override
        public Point apply(EdgeSegment segment) {
            return segment.getA();
        }
    };

    public static final Function<EdgeSegment, Point> TRANSFORM_TO_B = new Function<EdgeSegment, Point>() {
        @Override
        public Point apply(EdgeSegment segment) {
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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((a == null) ? 0 : a.hashCode());
        result = prime * result + (aFixed ? 1231 : 1237);
        result = prime * result + ((b == null) ? 0 : b.hashCode());
        result = prime * result + (bFixed ? 1231 : 1237);
        long temp;
        temp = Double.doubleToLongBits(length);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(weight);
        result = prime * result + (int) (temp ^ (temp >>> 32));
//        result = prime * result + ((parents == null) ? 0 : parents.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        EdgeSegment other = (EdgeSegment) obj;
        if (a == null) {
            if (other.a != null)
                return false;
        } else if (!a.equals(other.a))
            return false;
        if (aFixed != other.aFixed)
            return false;
        if (b == null) {
            if (other.b != null)
                return false;
        } else if (!b.equals(other.b))
            return false;
        if (bFixed != other.bFixed)
            return false;
        if (Double.doubleToLongBits(length) != Double
                .doubleToLongBits(other.length))
            return false;
        if (Double.doubleToLongBits(weight) != Double
                .doubleToLongBits(other.weight))
            return false;
//        if (parents == null) {
//            if (other.parents != null)
//                return false;
//        } else if (!parents.equals(other.parents))
//            return false;
        return true;
    }

    @Override
    public String toString() {
        return "EdgeSegment [a=" + a + ", b=" + b + ", length=" + length
                + ", parents.size=" + parents.size() + ", weight=" + weight + "]";
    }

}