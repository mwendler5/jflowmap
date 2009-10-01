package ch.unifr.flowmap.visuals;

import java.awt.geom.Point2D;
import java.util.Arrays;

import prefuse.data.Edge;
import ch.unifr.flowmap.util.BSplinePath;
import edu.umd.cs.piccolo.nodes.PPath;

/**
 * @author Ilya Boyandin
 */
public class BSplineVisualEdge extends VisualEdge {

    private static final long serialVersionUID = 1L;

    private PPath splinePPath;
    private BSplinePath spline;
    
    public BSplineVisualEdge(VisualFlowMap visualFlowMap, Edge edge,
            VisualNode sourceNode, VisualNode targetNode) {
        super(visualFlowMap, edge, sourceNode, targetNode);
        
        final double x1 = sourceNode.getValueX();
        final double y1 = sourceNode.getValueY();
        final double x2 = targetNode.getValueX();
        final double y2 = targetNode.getValueY();
        
        setBSplinePoints(new Point2D[] {
                new Point2D.Double(x1, y1),
                new Point2D.Double(x1 + (x2 - x1)/4, y1 + (y2 - y1)/4),
                new Point2D.Double(x1 + 3 * (x2 - x1)/4, y1 + 3 * (y2 - y1)/4),
                new Point2D.Double(x2, y2)
        });
    }
    
    public void setBSplinePoints(Point2D[] points) {
        if (splinePPath != null) {
            removeAllChildren();
        }
        spline = new BSplinePath(Arrays.asList(points));
        splinePPath = new PPath(spline);
        addChild(splinePPath);
        update();
    }
    
    @Override
    public PPath getEdgePPath() {
        return splinePPath;
    }

    @Override
    public void setHighlighted(boolean value, boolean showDirection, boolean outgoing) {
    }

    @Override
    public void updateEdgeMarkerColors() {
    }

}
