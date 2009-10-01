package ch.unifr.flowmap.util;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.List;

/**
 * Here a B-spline curve (which are not supported by Java2D)
 * is converted to a series of cubic Bézier curves that can
 * be rendered by Path2D.
 * <p>
 * Based on the BSpline example from the book
 * "Computer Graphics Using Java 2D and 3D"
 * by Hong Zhang and Y.Daniel Liang.
 * 
 * @author Ilya Boyandin
 */
public class BSplinePath extends Path2D.Double {
    
    private static final long serialVersionUID = 1L;

    public BSplinePath(List<Point2D> points) {
        int n = points.size();
        if (n < 3) {
            throw new IllegalArgumentException(
                    "BSplinePath needs at least 4 points");
        }
        
        Point2D p1 = null;
        Point2D p2 = null;
        Point2D p3 = null;
        double x1, y1, x2, y2, x3, y3, x4, y4;

        p1 = points.get(0);
        moveTo(p1.getX(), p1.getY());
        p1 = points.get(1);
        p2 = points.get(2);
        p3 = points.get(3);
        x1 = p1.getX();
        y1 = p1.getY();
        x2 = (p1.getX() + p2.getX()) / 2.0f;
        y2 = (p1.getY() + p2.getY()) / 2.0f;
        x4 = (2.0f * p2.getX() + p3.getX()) / 3.0f;
        y4 = (2.0f * p2.getY() + p3.getY()) / 3.0f;
        x3 = (x2 + x4) / 2.0f;
        y3 = (y2 + y4) / 2.0f;
        curveTo(x1, y1, x2, y2, x3, y3);
        for (int i = 2; i < n - 4; i++) {
            p1 = p2;
            p2 = p3;
            p3 = points.get(i + 2);
            x1 = x4;
            y1 = y4;
            x2 = (p1.getX() + 2.0f * p2.getX()) / 3.0f;
            y2 = (p1.getY() + 2.0f * p2.getY()) / 3.0f;
            x4 = (2.0f * p2.getX() + p3.getX()) / 3.0f;
            y4 = (2.0f * p2.getY() + p3.getY()) / 3.0f;
            x3 = (x2 + x4) / 2.0f;
            y3 = (y2 + y4) / 2.0f;
            curveTo(x1, y1, x2, y2, x3, y3);
        }
        p1 = p2;
        p2 = p3;
        p3 = points.get(n - 2);
        x1 = x4;
        y1 = y4;
        x2 = (p1.getX() + 2.0f * p2.getX()) / 3.0f;
        y2 = (p1.getY() + 2.0f * p2.getY()) / 3.0f;
        x4 = (p2.getX() + p3.getX()) / 2.0f;
        y4 = (p2.getY() + p3.getY()) / 2.0f;
        x3 = (x2 + x4) / 2.0f;
        y3 = (y2 + y4) / 2.0f;
        curveTo(x1, y1, x2, y2, x3, y3);
        p2 = p3;
        p3 = points.get(n - 1);
        x1 = x4;
        y1 = y4;
        x2 = p2.getX();
        y2 = p2.getY();
        x3 = p3.getX();
        y3 = p3.getY();
        curveTo(x1, y1, x2, y2, x3, y3);
    }
    
}
