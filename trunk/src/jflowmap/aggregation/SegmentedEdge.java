package jflowmap.aggregation;

import java.util.Collections;
import java.util.List;

import jflowmap.geom.FPoint;
import prefuse.data.Edge;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * @author Ilya Boyandin
 */
public class SegmentedEdge {

    private final List<EdgeSegment> segments;
    private final Edge edge;

    public SegmentedEdge(Edge edge) {
        this.edge = edge;
        this.segments = Lists.newArrayList();
    }

    public Edge getEdge() {
        return edge;
    }

    public List<EdgeSegment> getSegments() {
        return Collections.unmodifiableList(segments);
    }

    /**
     * Adds {@code segment} to the edge. The {@code segment} must be
     * consecutive to the last segment of the edge (its start point
     * must be the same as the end point of the last segment of the edge).
     *
     * @throws IllegalArgumentException If the segment is not consecutive
     *         to the last segment of the edge.
     */
    public void addConsecutiveSegment(EdgeSegment segment) {
        if (segments.size() > 0) {
            if (!segment.isConsecutiveFor(Iterables.getLast(segments))) {
                throw new IllegalArgumentException("Segments are not consecutive");
            }
        }
//        System.out.println("Add segment " + System.identityHashCode(segment) + " to edge " + System.identityHashCode(this));
        if (!segment.getParents().contains(this)) {
            throw new IllegalArgumentException();
        }
        segments.add(segment);
    }

    public void replaceSegment(EdgeSegment oldSegment, EdgeSegment newSegment) {
        int index = indexOf(oldSegment);
//        if (index == -1) {
//            return;
//        }
        segments.set(index, newSegment);

        // update adjacent segments
        // prev
        if (index > 0) {
            EdgeSegment prev = segments.get(index - 1);
            FPoint newB = newSegment.getA();
            if (!prev.getB().equals(newB)) {
                prev.setB(newB);
                if (newSegment.getA().isFixed()) {
                    prev.setB(prev.getB().withFixed(true));
                }
            }
        }
        // next
        int size = segments.size();
        if (index < size - 1) {
            EdgeSegment next = segments.get(index + 1);
            FPoint newA = newSegment.getB();
            if (!next.getA().equals(newA)) {
                next.setA(newA);
                if (newSegment.getB().isFixed()) {
                    next.setA(next.getA().withFixed(true));
                }
            }
        }
    }

    private int indexOf(EdgeSegment segment) {
        int index = -1;
        for (int i = 0, size = segments.size(); i < size; i++) {
            if (segments.get(i) == segment) {
                index = i;
            }
        }
        return index;
    }

//    public EdgeSegment getLeftAdjacent(EdgeSegment segment) {
//        EdgeSegment prev = null;
//        for (EdgeSegment seg : segments) {
//            if (seg.equals(segment)) {
//                return prev;
//            }
//        }
//        return null;
//    }
//
//    public EdgeSegment getRightAdjacent(EdgeSegment segment) {
//        for (Iterator<EdgeSegment> it = segments.iterator(); it.hasNext(); ) {
//            EdgeSegment seg = it.next();
//            if (seg.equals(segment)) {
//                if (it.hasNext()) {
//                    return it.next();
//                } else {
//                    return null;
//                }
//            }
//        }
//        return null;
//    }

    @Override
    public String toString() {
        return "SegmentedEdge [edge=" + edge + ", segments.size=" + segments.size() + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((edge == null) ? 0 : edge.hashCode());
        result = prime * result + ((segments == null) ? 0 : segments.hashCode());
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
        SegmentedEdge other = (SegmentedEdge) obj;
        if (edge == null) {
            if (other.edge != null)
                return false;
        } else if (!edge.equals(other.edge))
            return false;
        if (segments == null) {
            if (other.segments != null)
                return false;
        } else if (!segments.equals(other.segments))
            return false;
        return true;
    }

}