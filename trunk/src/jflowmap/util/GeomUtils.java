package jflowmap.util;

import java.awt.geom.Point2D;

/**
 * @author Ilya Boyandin
 */
public final class GeomUtils {
    
    private GeomUtils() {
    }
    
    public static Point2D projectPointToLine(Point2D line1, Point2D line2, Point2D point) {
        return projectPointToLine(
                line1.getX(), line1.getY(), line2.getX(), line2.getY(),
                point.getX(), point.getY());
    }

    /**
     * See http://www.exaflop.org/docs/cgafaq/cga1.html
     */
    public static Point2D projectPointToLine(double x1, double y1, double x2, double y2, double x, double y) {
        double L = Math.sqrt((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1));
        double r = ((y1-y)*(y1-y2) - (x1-x)*(x2-x1)) / (L * L);
        return new Point2D.Double(x1 + r * (x2-x1), y1 + r * (y2-y1));
    }

    public static Point2D midpoint(Point2D a, Point2D b) {
        return between(a, b, 0.5);
    }

    /**
     * Returns a point on a segment between the two points
     * @param alpha Between 0 and 1
     */
    public static Point2D between(Point2D a, Point2D b, double alpha) {
        return new Point2D.Double(
                a.getX() + (b.getX() - a.getX()) * alpha,
                a.getY() + (b.getY() - a.getY()) * alpha
        );
    }
    
}
