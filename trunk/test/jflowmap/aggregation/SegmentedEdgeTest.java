package jflowmap.aggregation;

import static org.junit.Assert.assertEquals;
import jflowmap.geom.FPoint;

import org.junit.Test;

import prefuse.data.tuple.TableEdge;


public class SegmentedEdgeTest {

    private static final double EPS = 1e-7;

    @Test
    public void testReplaceSegment() {
        SegmentedEdge edge1 = new SegmentedEdge(new TableEdge());
        SegmentedEdge edge2 = new SegmentedEdge(new TableEdge());
        EdgeSegment
        seg1_1,seg1_2,seg1_3,
        seg2_1,seg2_2,seg2_3,
        agg;

        seg1_1 = new EdgeSegment(new FPoint(0, 1, false), new FPoint(1, 0, false), 1.0, edge1);
        seg1_2 = new EdgeSegment(new FPoint(1, 0, false), new FPoint(2, 0, false), 1.0, edge1);
        seg1_3 = new EdgeSegment(new FPoint(2, 0, false), new FPoint(3, 0, false), 1.0, edge1);
        edge1.addConsecutiveSegment(seg1_1);
        edge1.addConsecutiveSegment(seg1_2);
        edge1.addConsecutiveSegment(seg1_3);

        seg2_1 = new EdgeSegment(new FPoint(0.5, 1, false), new FPoint(1, 1, false), 1.0, edge2);
        seg2_2 = new EdgeSegment(new FPoint(1, 1, false), new FPoint(1.5, 1, false), 2.0, edge2);
        seg2_3 = new EdgeSegment(new FPoint(1.5, 1, false), new FPoint(3, 2, false), 1.0, edge2);
        edge2.addConsecutiveSegment(seg2_1);
        edge2.addConsecutiveSegment(seg2_2);
        edge2.addConsecutiveSegment(seg2_3);


        agg = seg1_2.aggregateWith(seg2_2);
        assertEquals(3.0, agg.getWeight(), EPS);
        assertEquals(new FPoint(1, 0.5, false), agg.getA());
        assertEquals(new FPoint(1.75, 0.5, false), agg.getB());


        // replace
        edge1.replaceSegment(seg1_2, agg);
        edge2.replaceSegment(seg2_2, agg);


        // test that adjacent segments were properly changed
        assertEquals(new FPoint(1, 0.5, false), seg1_1.getB());
        assertEquals(new FPoint(1, 0.5, false), seg2_1.getB());

        assertEquals(new FPoint(1.75, 0.5, false), seg1_3.getA());
        assertEquals(new FPoint(1.75, 0.5, false), seg2_3.getA());
    }

}
