package jflowmap.util;

import java.awt.geom.Point2D;

import junit.framework.TestCase;

public class GeomUtilsTest extends TestCase {

    private static final double EPS = 1e-7;

    public void testProjectPointToLine() {
        assertEquals(4, 0, GeomUtils.projectPointToLine(0, 0,  10, 0,   4, 1));
        assertEquals(-1, 0, GeomUtils.projectPointToLine(0, 0,  10, 0,   -1, 1));
        assertEquals(15, 0, GeomUtils.projectPointToLine(0, 0,  10, 0,   15, 15));
        assertEquals(3, 3, GeomUtils.projectPointToLine(1, 1,  10, 10,   2, 4));
        assertEquals(2, 4, GeomUtils.projectPointToLine(0, 2,  9, 11,   3, 3));
        assertEquals(-1, 1, GeomUtils.projectPointToLine(0, 2,  9, 11,   0, 0));
        assertEquals(7, 2, GeomUtils.projectPointToLine(3, 0,  5, 1,   6, 4));
        assertEquals(-3, 0, GeomUtils.projectPointToLine(-2, 1,  -5, -2,   -2, -1));
    }
    
    private void assertEquals(double expectedX, double expectedY, Point2D actual) {
        assertEquals(expectedX, actual.getX(), EPS);
        assertEquals(expectedY, actual.getY(), EPS);
    }

}
