package ch.unifr.flowmap.visuals;

import java.awt.Color;
import java.awt.geom.Line2D;

import prefuse.data.Edge;
import ch.unifr.flowmap.models.FlowMapModel;
import ch.unifr.flowmap.util.Stats;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolox.util.PFixedWidthStroke;

/**
 * @author Ilya Boyandin
 */
public class LineVisualEdge extends VisualEdge {

    private static final long serialVersionUID = 1L;

    private static final double EPS = 0.0000001;
    private static final Color STROKE_PAINT = new Color(255, 255, 255);
    private static final Color START_MARKER_STROKE_PAINT = new Color(255, 0, 0);
    private static final Color END_MARKER_STROKE_PAINT = new Color(0, 255, 0);

    private static final Color STROKE_HIGHLIGHTED_PAINT = new Color(0, 0, 255, 200);
    private static final Color STROKE_HIGHLIGHTED_INCOMING_PAINT = new Color(255, 0, 0, 200);
    private static final Color STROKE_HIGHLIGHTED_OUTGOING_PAINT = new Color(0, 255, 0, 200); // new Color(255, 0, 0, 250);

    private static final double MARKER_SIZE = 6;

    private final PPath line;
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


        final double width = getVisualFlowMap().getMaxEdgeWidth();   // TODO: update dynamically
        line = new PPath(new Line2D.Double(
                sm_x /*- sin_a * width*/, sm_y /*- cos_a * width*/,
                em_x /*+ sin_a * width*/, em_y /*+ cos_a * width*/));
        
        addChild(line);

        updateEdgeColors();
        updateEdgeMarkerColors();
        updateEdgeWidth();
        updateVisibiliy();
    }

    public double getNormalizedLogValue() {
        FlowMapModel model = getVisualFlowMap().getModel();
        double value = getValue();
        double nv;
        if (model.getAutoAdjustEdgeColorScale()) {
            double minLog = 1.0;
            double maxLog = Math.log(model.getValueFilterMax() - model.getValueFilterMin());
            nv = (Math.log(value - model.getValueFilterMin()) - minLog) / (maxLog - minLog);
        } else {
            Stats stats = model.getGraphStats().getValueEdgeAttrStats();
            nv = stats.normalizeLog(value);
        }
        return nv;
    }

    public double getNormalizedValue() {
        double nv;

        Stats stats = getVisualFlowMap().getModel().getGraphStats().getValueEdgeAttrStats();
        nv = stats.normalize(getValue());

        return nv;
    }

    public void updateEdgeColors() {
        line.setStrokePaint(getValueColor(STROKE_PAINT, false));
    }

    public void updateEdgeMarkerColors() {
        startMarker.setStrokePaint(getValueColor(START_MARKER_STROKE_PAINT, true));
        endMarker.setStrokePaint(getValueColor(END_MARKER_STROKE_PAINT, true));
    }

    public void updateEdgeWidth() {
        double nv = getNormalizedValue();
        float width = (float)(1 + nv * getVisualFlowMap().getMaxEdgeWidth());
        PFixedWidthStroke stroke = new PFixedWidthStroke(width);
        startMarker.setStroke(stroke);
        endMarker.setStroke(stroke);
        line.setStroke(stroke);
    }

    private Color getValueColor(Color baseColor, boolean forMarker) {
        FlowMapModel model = getVisualFlowMap().getModel();
        final double normalizedValue = getNormalizedLogValue();
        int r = (int) Math.round(normalizedValue * baseColor.getRed());
        int g = (int) Math.round(normalizedValue * baseColor.getGreen());
        int b = (int) Math.round(normalizedValue * baseColor.getBlue());
        int alpha;
        if (baseColor.getAlpha() == 255) {
            if (forMarker) {
                alpha = model.getEdgeMarkerAlpha();
            } else {
                alpha = model.getEdgeAlpha();
            }
        } else {
            alpha = baseColor.getAlpha();
        }
        return new Color(r, g, b,alpha);
    }

    public void setHighlighted(boolean value, boolean showDirection, boolean outgoing) {
//        System.out.println(this + ".setHighlighted("  + value + ")");
        if (value) {
            Color paint;
            if (showDirection) {
                paint = (outgoing ? STROKE_HIGHLIGHTED_OUTGOING_PAINT : STROKE_HIGHLIGHTED_INCOMING_PAINT);
            } else {
                paint = STROKE_HIGHLIGHTED_PAINT;
            }
            line.setStrokePaint(getValueColor(paint, false));
        } else {
            line.setStrokePaint(getValueColor(STROKE_PAINT, false));
        }
        getSourceNode().setVisible(value);
        getTargetNode().setVisible(value);
    }
}
