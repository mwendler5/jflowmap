package ch.unifr.flowmap.util;

import java.awt.geom.Point2D;

/**
 * @author Ilya Boyandin
 */
public class Vector2D {

    private final double x;
    private final double y;
    private double length = Double.NaN;

    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public double x() {
        return x;
    }
    
    public double y() {
        return y;
    }

    public double distanceTo(Vector2D that) {
        return this.minus(that).length();
    }
    
    public Vector2D plus(Vector2D b) {
        return new Vector2D(x + b.x, y + b.y);
    }
    
    public Vector2D minus(Vector2D b) {
        return new Vector2D(x - b.x, y - b.y);
    }

    public double length() {
        if (Double.isNaN(length)) {
            length = Math.sqrt(x * x + y * y);
        }
        return length;
    }
    
    public Vector2D times(double factor) {
        return new Vector2D(x * factor, y * factor);
    }

    public Vector2D direction() {
        return times(1.0 / length());
    }

    public static Vector2D valueOf(Point2D point) {
        return new Vector2D(point.getX(), point.getY());
    }

    public void movePoint(Point2D.Double point) {
        point.x = point.x + x;
        point.y = point.y + y;
    }
}
