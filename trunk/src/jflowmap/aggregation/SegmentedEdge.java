package jflowmap.aggregation;

import java.util.Collections;
import java.util.List;

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

    public void add(EdgeSegment segment) {
        if (segments.size() > 0) {
            if (!Iterables.getLast(segments).getB().equals(segment.getA())) {
                throw new IllegalArgumentException("Segments are not subsequent");
            }
        }
        segments.add(segment);
    }

    @Override
    public String toString() {
        return "SegmentedEdge [edge=" + edge + ", segments.size=" + segments.size() + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((edge == null) ? 0 : edge.hashCode());
        result = prime * result
                + ((segments == null) ? 0 : segments.hashCode());
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