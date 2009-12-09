package jflowmap.aggregation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import jflowmap.geom.FPoint;

import org.junit.Test;

import prefuse.data.tuple.TableEdge;

public class EdgeSegmentTest {

    private static final double EPS = 1e-7;

    @Test
    public void testCanBeAggregatedWith() {
        SegmentedEdge edge = new SegmentedEdge(new TableEdge());
        SegmentedEdge edge1 = new SegmentedEdge(new TableEdge());
        SegmentedEdge edge2 = new SegmentedEdge(new TableEdge());
        EdgeSegment seg1, seg2;

        seg1 = new EdgeSegment(new FPoint(0, 1, false) , new FPoint(1, 0, false) , 1.0, edge);
        seg2 = new EdgeSegment(new FPoint(0, 1.5, false) , new FPoint(1.5, 0, false) , 2.0, edge);
        assertTrue(!seg1.canBeAggregatedWith(seg2));  // seg1 and seg2 share the same parent edge


        seg1 = new EdgeSegment(new FPoint(0, 1, true), new FPoint(1, 0, false) , 1.0, edge1);
        seg2 = new EdgeSegment(new FPoint(0, 1.5, true), new FPoint(1.5, 0, false) , 2.0, edge2);
        assertTrue(!seg1.canBeAggregatedWith(seg2));  // both A points are fixed


        seg1 = new EdgeSegment(new FPoint(0, 1, false) , new FPoint(1, 0, true), 1.0, edge1);
        seg2 = new EdgeSegment(new FPoint(0, 1.5, false) , new FPoint(1.5, 0, true), 2.0, edge2);
        assertTrue(!seg1.canBeAggregatedWith(seg2));  // both B points are fixed


        seg1 = new EdgeSegment(new FPoint(0, 1, false) , new FPoint(1.5, 0, true), 1.0, edge1);
        seg2 = new EdgeSegment(new FPoint(0, 1.5, false) , new FPoint(1.5, 0, true), 2.0, edge2);
        assertTrue(seg1.canBeAggregatedWith(seg2));  // both B points are fixed; but also equal

        seg1 = new EdgeSegment(new FPoint(0, 1, false) , new FPoint(1.5, 0, true), 1.0, edge1);
        seg2 = new EdgeSegment(new FPoint(0, 1.5, true), new FPoint(1.5, 0, false) , 2.0, edge2);
        assertTrue(seg1.canBeAggregatedWith(seg2));  // cross-points fixed

    }

    @Test
    public void testAggregateWith() {
        SegmentedEdge edge1 = new SegmentedEdge(new TableEdge());
        SegmentedEdge edge2 = new SegmentedEdge(new TableEdge());
        EdgeSegment seg1, seg2, agg;

        seg1 = new EdgeSegment(new FPoint(0, 1, false) , new FPoint(1, 0, false) , 1.0, edge1);
        seg2 = new EdgeSegment(new FPoint(0, 1.5, false) , new FPoint(1.5, 0, false) , 2.0, edge2);
        agg = seg1.aggregateWith(seg2);
        assertEquals(3.0, agg.getWeight(), EPS);
        assertEquals(new FPoint(0, 1.25, false), agg.getA());
        assertEquals(new FPoint(1.25, 0, false), agg.getB());
    }

    @Test
    public void testReplaceWithAggregate() {
        SegmentedEdge edge1 = new SegmentedEdge(new TableEdge());
        SegmentedEdge edge2 = new SegmentedEdge(new TableEdge());
        EdgeSegment
        seg1_1,seg1_2,seg1_3,
        seg2_1,seg2_2,seg2_3,
        agg;

        seg1_1 = new EdgeSegment(new FPoint(0, 1, false) , new FPoint(1, 0, false) , 1.0, edge1);
        seg1_2 = new EdgeSegment(new FPoint(1, 0, false) , new FPoint(2, 0, false) , 1.0, edge1);
        seg1_3 = new EdgeSegment(new FPoint(2, 0, false) , new FPoint(3, 0, false) , 1.0, edge1);
        edge1.addConsecutiveSegment(seg1_1);
        edge1.addConsecutiveSegment(seg1_2);
        edge1.addConsecutiveSegment(seg1_3);

        seg2_1 = new EdgeSegment(new FPoint(0.5, 1, false) , new FPoint(1, 1, false) , 1.0, edge2);
        seg2_2 = new EdgeSegment(new FPoint(1, 1, false) , new FPoint(1.5, 1, false) , 2.0, edge2);
        seg2_3 = new EdgeSegment(new FPoint(1.5, 1, false) , new FPoint(3, 2, false) , 1.0, edge2);
        edge2.addConsecutiveSegment(seg2_1);
        edge2.addConsecutiveSegment(seg2_2);
        edge2.addConsecutiveSegment(seg2_3);


        // aggregate middle segments
        agg = seg1_2.aggregateWith(seg2_2);
        assertEquals(3.0, agg.getWeight(), EPS);
        assertEquals(new FPoint(1, 0.5, false), agg.getA());
        assertEquals(new FPoint(1.75, 0.5, false), agg.getB());


        // check parents of the aggregate
        assertEquals(2, agg.getParents().size());
        assertTrue(agg.getParents().contains(edge1));
        assertTrue(agg.getParents().contains(edge2));


        // replace middle segments with the aggregate
        seg1_2.replaceWith(agg);
        seg2_2.replaceWith(agg);


        // test that adjacent segments were properly changed
        assertEquals(new FPoint(1, 0.5, false), seg1_1.getB());
        assertEquals(new FPoint(1, 0.5, false), seg2_1.getB());

        assertEquals(new FPoint(1.75, 0.5, false), seg1_3.getA());
        assertEquals(new FPoint(1.75, 0.5, false), seg2_3.getA());
    }

}
