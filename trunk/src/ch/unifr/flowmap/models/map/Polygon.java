package ch.unifr.flowmap.models.map;

import java.awt.geom.Point2D;
import java.util.Arrays;

/**
 * @author Ilya Boyandin
 *         Date: 25-Sep-2009
 */
public class Polygon {

    private final Point2D[] points;

    public Polygon(Point2D[] points) {
        this.points = points.clone();
    }

    public Point2D[] getPoints() {
        return points.clone();
    }
    
}
