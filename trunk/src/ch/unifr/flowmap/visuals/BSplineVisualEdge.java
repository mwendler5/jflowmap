package ch.unifr.flowmap.visuals;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.Arrays;

import prefuse.data.Edge;
import ch.unifr.flowmap.util.BSplinePath;
import edu.umd.cs.piccolo.nodes.PPath;

/**
 * @author Ilya Boyandin
 */
public class BSplineVisualEdge extends VisualEdge {

    private static final Color DOT_COLOR = new Color(255,0,0,50);
    private static final Font AFONT = new Font("arial", Font.PLAIN, 1);

    private static final long serialVersionUID = 1L;

    public BSplineVisualEdge(VisualFlowMap visualFlowMap, Edge edge,
            VisualNode sourceNode, VisualNode targetNode, Point2D[] splinePoints) {
        super(visualFlowMap, edge, sourceNode, targetNode);

        Point2D start = splinePoints[0];
        Point2D end = splinePoints[splinePoints.length - 1];
        assert(start.getX() == sourceNode.getValueX());
        assert(start.getY() == sourceNode.getValueY());
        assert(end.getX() == targetNode.getValueX());
        assert(end.getY() == targetNode.getValueY());
        
        setSplinePoints(splinePoints);
    }
    
    public void setSplinePoints(Point2D[] points) {
        removeAllChildren();
        if (isSelfLoop()) {
            // TODO: add some visual representation
            return;
        }
        Path2D.Double path;
        if (points.length < 4) {
            path = new Path2D.Double();
            path.moveTo(points[0].getX(), points[0].getY());
            for (int i = 1; i < points.length; i++) {
                path.lineTo(points[i].getX(), points[i].getY());
            }
        } else {
            path = new BSplinePath(Arrays.asList(points));
        }
        PPath ppath = new PPath(path);
        setEdgePPath(ppath);
        addChild(ppath);
        
        // add spline points
//        int cnt = 0;
//        for (Point2D p : points) {
//            PPath ell = new PPath(new Ellipse2D.Double(p.getX(), p.getY(), .5, .5));
//            ell.setStrokePaint(DOT_COLOR);
//            ell.setPaint(DOT_COLOR);
//            ell.moveToFront();
//            addChild(ell);
//            cnt++;
//        }

        update();
    }

    @Override
    public void updateEdgeMarkerColors() {
    }

}
