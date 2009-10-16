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

    private final PPath startMarker;
    private final PPath endMarker;

    public LineVisualEdge(VisualFlowMap visualFlowMap, Edge edge,
            VisualNode sourceNode, VisualNode targetNode) {
        super(visualFlowMap, edge, sourceNode, targetNode);
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
        
        final double sin_a = (x1 - x2) / edgeLength;
        final double cos_a = (y1 - y2) / edgeLength;
        double sm_x = x1 - markerSize * sin_a;
        double sm_y = y1 - markerSize * cos_a;

        double em_x = x2 + markerSize * sin_a;
        double em_y = y2 + markerSize * cos_a;

        
        startMarker = new PPath(new Line2D.Double(x1, y1, sm_x, sm_y));
        addChild(startMarker);
        
        endMarker = new PPath(new Line2D.Double(em_x, em_y, x2, y2));
        addChild(endMarker);


//        final double width = getVisualFlowMap().getMaxEdgeWidth();   // TODO: update dynamically
        PPath line = new PPath(new Line2D.Double(
                sm_x /*- sin_a * width*/, sm_y /*- cos_a * width*/,
                em_x /*+ sin_a * width*/, em_y /*+ cos_a * width*/));
        setEdgePPath(line);
        addChild(line);
    }

    public void updateEdgeMarkerColors() {
        startMarker.setStrokePaint(getValueColor(START_MARKER_STROKE_PAINT, true));
        endMarker.setStrokePaint(getValueColor(END_MARKER_STROKE_PAINT, true));
    }

    public void updateEdgeWidth() {
        Stroke stroke = createStroke();
        startMarker.setStroke(stroke);
        endMarker.setStroke(stroke);
        getEdgePPath().setStroke(stroke);
    }
}
