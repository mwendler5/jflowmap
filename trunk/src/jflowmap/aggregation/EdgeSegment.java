package jflowmap.aggregation;

import java.util.List;

import jflowmap.geom.GeomUtils;
import jflowmap.geom.Point;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

/**
 * @author Ilya Boyandin
 */
public class EdgeSegment {

    private final Point a;
    private final Point b;
    private final double weight;
    private final List<SegmentedEdge> parents;
    private final double length;

    public EdgeSegment(Point a, Point b, double weight, SegmentedEdge parent) {
        this.a = a;
        this.b = b;
        this.weight = weight;
        this.length = a.distanceTo(b);
        this.parents = ImmutableList.of(parent);
    }

    public EdgeSegment(Point a, Point b, double weight, Iterable<SegmentedEdge> parents) {
        this.a = a;
        this.b = b;
        this.weight = weight;
        this.length = a.distanceTo(b);
        this.parents = ImmutableList.copyOf(parents);
    }

    public Point getA() {
        return a;
    }

    public Point getB() {
        return b;
    }

    public double getWeight() {
        return weight;
    }

    public List<SegmentedEdge> getParents() {
        return parents;
    }

    public List<EdgeSegment> getLeftAdjacentSegments() {
        return ImmutableList.copyOf(Iterables.transform(parents, new Function<SegmentedEdge, EdgeSegment>() {
            // TODO: remove nulls from the list of adjacent segments
            @Override
            public EdgeSegment apply(SegmentedEdge se) {
                return se.getLeftAdjacent(EdgeSegment.this);
            }
        }));
    }

    public List<EdgeSegment> getRightAdjacentSegments() {
        return ImmutableList.copyOf(Iterables.transform(parents, new Function<SegmentedEdge, EdgeSegment>() {
            @Override
            public EdgeSegment apply(SegmentedEdge se) {
                return se.getRightAdjacent(EdgeSegment.this);
            }
        }));
    }

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
        return new EdgeSegment(
                GeomUtils.midpoint(a, other.getA()),
                GeomUtils.midpoint(b, other.getB()),
                weight + other.weight,
                Iterables.concat(getParents(), other.getParents())
        );
    }

    public static EdgeSegment aggregate(List<EdgeSegment> segments) {
        double sumWeight = 0;
        for (EdgeSegment seg : segments) {
            sumWeight += seg.getWeight();
        }
        return new EdgeSegment(
                GeomUtils.centroid(Iterators.transform(segments.iterator(), EdgeSegment.TRANSFORM_TO_A)),
                GeomUtils.centroid(Iterators.transform(segments.iterator(), EdgeSegment.TRANSFORM_TO_B)),
                sumWeight,
                Iterables.concat(Iterables.transform(segments, TRANSFORM_TO_PARENTS))
        );
    }

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
    public String toString() {
        return "EdgeSegment [a=" + a + ", b=" + b + ", length=" + length
                + ", parents.size=" + parents.size() + ", weight=" + weight + "]";
    }

}