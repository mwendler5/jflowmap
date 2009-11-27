package jflowmap.visuals;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.util.Arrays;

import jflowmap.geom.BSplinePath;
import jflowmap.geom.Point;
import prefuse.data.Edge;
import edu.umd.cs.piccolo.nodes.PPath;

/**
 * @author Ilya Boyandin
 */
public class BSplineVisualEdge extends VisualEdge {

    private static final Color DOT_COLOR = new Color(255,0,0,100);

    private static final long serialVersionUID = 1L;
    
    public BSplineVisualEdge(VisualFlowMap visualFlowMap, Edge edge,
            VisualNode sourceNode, VisualNode targetNode, Point[] points,
            boolean showSplinePoints) {
        super(visualFlowMap, edge, sourceNode, targetNode);

        Point start = points[0];
        Point end = points[points.length - 1];
        assert(start.x() == sourceNode.getValueX());
        assert(start.y() == sourceNode.getValueY());
        assert(end.x() == targetNode.getValueX());
        assert(end.y() == targetNode.getValueY());
        
        Shape shape;
        if (isSelfLoop()) {
            shape = createSelfLoopShape();
        } else {
            Path2D path;
            if (points.length < 4) {
                path = new Path2D.Double();
                path.moveTo(points[0].x(), points[0].y());
                for (int i = 1; i < points.length; i++) {
                    path.lineTo(points[i].x(), points[i].y());
                }
            } else {
                path = new BSplinePath(Arrays.asList(points));
            }
            
            // add spline points
            if (showSplinePoints) {
                int cnt = 0;
                for (Point p : points) {
                    PPath ell = new PPath(new Ellipse2D.Double(p.x()-.2, p.y()-.2, .4, .4));
                    ell.setStrokePaint(DOT_COLOR);
                    ell.setPaint(DOT_COLOR);
                    ell.moveToFront();
                    addChild(ell);
                    cnt++;
                }
            }
            
            shape = path;
        }

        PPath ppath = new PPath(shape);
        setEdgePPath(ppath);
        addChild(ppath);

//        update();
    }
    
    @Override
    public void updateEdgeColors() {
        PPath ppath = getEdgePPath();
        if (ppath != null) {
            ppath.setStrokePaint(createPaint());
        }
    }

}
