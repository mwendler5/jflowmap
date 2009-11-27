package jflowmap.aggregation;

import java.util.List;

import jflowmap.geom.Point;

import com.google.common.collect.ImmutableList;

/**
 * @author Ilya Boyandin
 */
public class Polyline {
    private List<Point> points;

    public Polyline(Point[] points) {
        this.points = ImmutableList.of(points);
    }
}
