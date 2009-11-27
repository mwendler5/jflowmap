package jflowmap.geom;

import java.awt.geom.Point2D;

/**
 * Immutable Point implementation.
 * 
 * @author Ilya Boyandin
 */
public class Point {

    private final double x;
    private final double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public double distanceTo(Point point) {
        double dx = point.x() - x;
        double dy = point.y() - y;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    public Point2D asPoint2D() {
        return new Point2D.Double(x, y);
    }
    
    public static Point valueOf(Point2D point) {
        return new Point(point.getX(), point.getY());
    }

    public String toString() {
        return "Point ( "
            + "x = " + this.x + ", "
            + "y = " + this.y + ", "
            + " )";
    }

}
