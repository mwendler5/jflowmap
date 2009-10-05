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
import edu.umd.cs.piccolo.nodes.PText;

/**
 * @author Ilya Boyandin
 */
public class BSplineVisualEdge extends VisualEdge {

    private static final Color DOT_COLOR = new Color(255,0,0,50);

    private static final Font AFONT = new Font("arial", Font.PLAIN, 1);

    private static final long serialVersionUID = 1L;

    private PPath ppath;
    
    public BSplineVisualEdge(VisualFlowMap visualFlowMap, Edge edge,
            VisualNode sourceNode, VisualNode targetNode, Point2D[] splinePoints) {
        super(visualFlowMap, edge, sourceNode, targetNode);
        setSplinePoints(splinePoints);
    }
    
    public void setSplinePoints(Point2D[] points) {
        if (ppath != null) {
            removeAllChildren();
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
        this.ppath = new PPath(path);
        addChild(ppath);
        
        // add spline points
        int cnt = 0;
        for (Point2D p : points) {
            PPath ell = new PPath(new Ellipse2D.Double(p.getX(), p.getY(), .5, .5));
            ell.setStrokePaint(DOT_COLOR);
            ell.setPaint(DOT_COLOR);
            ell.moveToFront();
            addChild(ell);
//            PText lbl = new PText(getLabel() + ": #" + Integer.toString(cnt));
//            lbl.setX(p.getX() - 2);
//            lbl.setY(p.getY() + 1);
//            lbl.setPaint(Color.green);
//            lbl.setFont(AFONT);
//            addChild(lbl);
//            lbl.moveToFront();
            cnt++;
        }

        update();
    }
    
    @Override
    public PPath getEdgePPath() {
        return ppath;
    }

    @Override
    public void setHighlighted(boolean value, boolean showDirection, boolean outgoing) {
    }

    @Override
    public void updateEdgeMarkerColors() {
    }

}
