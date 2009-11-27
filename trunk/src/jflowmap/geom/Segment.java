package jflowmap.geom;

/**
 * @author Ilya Boyandin
 */
public class Segment {
    
    private final Point a;
    private final Point b;

    public Segment(Point a, Point b) {
        this.a = a;
        this.b = b;
    }

    public Point getA() {
        return a;
    }

    public Point getB() {
        return b;
    }
    
    public double length() {
        return a.distanceTo(b);
    }

    public String toString() {
        return "Segment ( "
            + "a = " + this.a + ", "
            + "b = " + this.b + ", "
            + " )";
    }
}
