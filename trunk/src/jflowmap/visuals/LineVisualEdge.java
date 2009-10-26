package jflowmap.visuals;

import java.awt.Color;
import java.awt.Stroke;
import java.awt.geom.Line2D;

import prefuse.data.Edge;
import edu.umd.cs.piccolo.nodes.PPath;

/**
 * @author Ilya Boyandin
 */
public class LineVisualEdge extends VisualEdge {

    private static final long serialVersionUID = 1L;

    private static final double EPS = 0.0000001;
    private static final Color START_MARKER_STROKE_PAINT = new Color(255, 0, 0);
    private static final Color END_MARKER_STROKE_PAINT = new Color(0, 255, 0);

    private static final double MARKER_SIZE = 6;

    private PPath startMarker;
    private PPath endMarker;

    private boolean showMarkers;

    public LineVisualEdge(VisualFlowMap visualFlowMap, Edge edge,
            VisualNode sourceNode, VisualNode targetNode, boolean showMarkers) {
        super(visualFlowMap, edge, sourceNode, targetNode);
        this.showMarkers = showMarkers;
        targetNode.addIncomingEdge(this);
        sourceNode.addOutgoingEdge(this);

        final double x1 = sourceNode.getValueX();
        final double y1 = sourceNode.getValueY();
        final double x2 = targetNode.getValueX();
        final double y2 = targetNode.getValueY();

        // Calc start/end marker positions

        final double edgeLength = getEdgeLength();

        double markerSize;
        if (MARKER_SIZE > edgeLength / 3.0) {
            markerSize = edgeLength / 3.0;
        } else {
            markerSize = MARKER_SIZE;
        }
        
        double ex1, ex2, ey1, ey2;
        if (showMarkers) {
            final double sin_a = (x1 - x2) / edgeLength;
            final double cos_a = (y1 - y2) / edgeLength;
            double ms_x = x1 - markerSize * sin_a;
            double ms_y = y1 - markerSize * cos_a;
            double me_y = y2 + markerSize * cos_a;
            double me_x = x2 + markerSize * sin_a;

            startMarker = new PPath(new Line2D.Double(x1, y1, ms_x, ms_y));
            addChild(startMarker);
            endMarker = new PPath(new Line2D.Double(me_x, me_y, x2, y2));
            addChild(endMarker);

            ex1 = ms_x;
            ex2 = me_x;
            ey1 = ms_y;
            ey2 = me_y;
        } else {
            ex1 = x1;
            ex2 = x2;
            ey1 = y1;
            ey2 = y2;
        }
        
//        final double width = getVisualFlowMap().getMaxEdgeWidth();   // TODO: update dynamically
        PPath line = new PPath(new Line2D.Double(ex1, ey1, ex2, ey2));
        setEdgePPath(line);
        addChild(line);
    }

//    public void updateEdgeMarkerColors() {
//        if (!showMarkers) return;
//        startMarker.setStrokePaint(getValueColor(START_MARKER_STROKE_PAINT, true));
//        endMarker.setStrokePaint(getValueColor(END_MARKER_STROKE_PAINT, true));
//    }

    public void updateEdgeWidth() {
        Stroke stroke = createStroke();
        if (showMarkers) {
            startMarker.setStroke(stroke);
            endMarker.setStroke(stroke);
        }
        getEdgePPath().setStroke(stroke);
    }
}
