package ch.unifr.flowmap.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Line2D;

import prefuse.data.Edge;
import ch.unifr.flowmap.data.Stats;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventListener;
import edu.umd.cs.piccolo.nodes.PPath;

/**
 * @author Ilya Boyandin
 */
public class VisualEdge extends PNode {

    private static final long serialVersionUID = 1L;

    private static final Color STROKE_PAINT = new Color(255, 255, 255);
    private static final Color START_MARKER_STROKE_PAINT = new Color(255, 0, 0);
    private static final Color END_MARKER_STROKE_PAINT = new Color(0, 255, 0);

    private static final Color STROKE_HIGHLIGHTED_PAINT = new Color(255, 0, 0, 200);
    private static final Color STROKE_INVERSE_HIGHLIGHTED_PAINT = new Color(0, 255, 0, 200); // new Color(255, 0, 0, 250);
    private static final double START_MARKER_MAX_SIZE = 6;
    private static final double END_MARKER_SIZE = 6;

    private final PPath line;
    private final PPath startMarker;
    private final PPath endMarker;
    private final FlowMapCanvas canvas;
    private final double value;

    private VisualNode sourceNode;
    private VisualNode targetNode;
    private Edge edge;

    public VisualEdge(FlowMapCanvas canvas, Edge edge, VisualNode sourceNode, VisualNode targetNode) {
        this.edge = edge;
        this.sourceNode = sourceNode;
        this.targetNode = targetNode;
        this.canvas = canvas;

        targetNode.addIncomingEdge(this);
        sourceNode.addOutgoingEdge(this);

        final double x1 = sourceNode.getValueX();
        final double y1 = sourceNode.getValueY();
        final double x2 = targetNode.getValueX();
        final double y2 = targetNode.getValueY();

        value = edge.getDouble(canvas.getEdgeValueAttr());


//    	BasicStroke stroke = new BasicStroke((int)Math.round(value));
//    	float width = (float)(1 + normalizedValue * 100);
        float width = 1;
        BasicStroke stroke = new BasicStroke(width);

//        normalizedValue = Math.log( Math.E+ normalizedValue);
//        if (normalizedValue < 0) normalizedValue  = 0;

        // Calc start/end marker positions
        final double d = dist(x1, y1, x2, y2);
        final double sin_a = (x1 - x2) / d;
        final double cos_a = (y1 - y2) / d;
        double sm_x = x1 - START_MARKER_MAX_SIZE * sin_a; // * normalizedValue;
        double sm_y = y1 - START_MARKER_MAX_SIZE * cos_a; // * normalizedValue;
//    	if (dist(x1, y1, sm_x, sm_y) > d) {
//    		sm_x = x2;
//    		sm_y = y2;
//    	}

        double em_x = x2 + END_MARKER_SIZE * sin_a; // * normalizedValue;
        double em_y = y2 + END_MARKER_SIZE * cos_a; // * normalizedValue;
//    	if (dist(em_x, em_y, x2, y2) > d) {
//    		em_x = x1;
//    		em_y = y1;
//    	}
        startMarker = new PPath(new Line2D.Double(x1, y1, sm_x, sm_y));
        startMarker.setStroke(stroke);
        addChild(startMarker);

        endMarker = new PPath(new Line2D.Double(em_x, em_y, x2, y2));
        endMarker.setStroke(stroke);
        addChild(endMarker);



        line = new PPath(new Line2D.Double(
                sm_x - sin_a * width, sm_y - cos_a * width,
                em_x + sin_a * width, em_y + cos_a * width));
        line.setStroke(stroke);
//		line.setStrokePaint(STROKE_PAINT);
//    	int alpha = (int)Math.round(255 * ((value/10000 - 1)));
//    	if (alpha > 255) alpha = 255;
//    	if (alpha < 0) alpha = 0;
//		line.setStrokePaint(new Color(255, 255, 255, (int)Math.round(200 * normalizedValue)));
        addChild(line);

        updateEdgeColors();
        updateEdgeMarkerColors();

        addInputEventListener(flowListener);
    }

    public double getNormalizedValue() {
        double nv;
        final double minLog;
        final double maxLog;
        if (canvas.getAutoAdjustEdgeColorScale()) {
            maxLog = Math.log(canvas.getValueFilterMax());
            minLog = Math.log(canvas.getValueFilterMin());
        } else {
            Stats stats = canvas.getEdgeValueAttrStats();
            minLog = stats.minLog;
            maxLog = stats.maxLog;
        }
//    	normalizedValue = (Math.log(value) - Math.log(minMax.min)) / (Math.log(minMax.max) - Math.log(minMax.min));
//    	normalizedValue = (value - minMax.min) / (minMax.max - minMax.min);
//    	normalizedValue = Math.sqrt((value - minMax.min) / (minMax.max - minMax.min));
//    	normalizedValue = Math.log( 1 + (value - minMax.min) / (minMax.max - minMax.min));

//    	normalizedValue = 4 * (Math.log(value) - minMax.minLog) / (minMax.maxLog - minMax.minLog);

//        nv = 2 * (Math.log(value) - minMax.minLog) / (minMax.maxLog - minMax.minLog);
        nv = 2 * (Math.log(value) - minLog) / (maxLog - minLog);

        if (nv < 0) {
            nv = 0.0;
        }
        if (nv > 1.0) {
            nv = 1.0;
        }

        return nv;
    }


    public void updateEdgeColors() {
        line.setStrokePaint(getValueColor(STROKE_PAINT, false));
    }

    public void updateEdgeMarkerColors() {
        startMarker.setStrokePaint(getValueColor(START_MARKER_STROKE_PAINT, true));
        endMarker.setStrokePaint(getValueColor(END_MARKER_STROKE_PAINT, true));
    }

    public Edge getEdge() {
        return edge;
    }

    public String getLabel() {
        return sourceNode.getNode().getString(canvas.getLabelAttr()) + " -> " +
                targetNode.getNode().getString(canvas.getLabelAttr());
    }

    public double getValue() {
        return value;
    }

    public VisualNode getSourceNode() {
        return sourceNode;
    }

    public VisualNode getTargetNode() {
        return targetNode;
    }

    private static final double dist(double x1, double y1, double x2, double y2) {
        final double dx = (x1 - x2);
        final double dy = (y1 - y2);
        return Math.sqrt(dx * dx + dy * dy);
    }

    private Color getValueColor(Color baseColor, boolean forMarker) {
        final double normalizedValue = getNormalizedValue();
        int r = (int) Math.round(normalizedValue * baseColor.getRed());
        int g = (int) Math.round(normalizedValue * baseColor.getGreen());
        int b = (int) Math.round(normalizedValue * baseColor.getBlue());
        int alpha;
        if (baseColor.getAlpha() == 255) {
            if (forMarker) {
                alpha = canvas.getEdgeMarkerAlpha();
            } else {
                alpha = canvas.getEdgeAlpha();
            }
        } else {
            alpha = baseColor.getAlpha();
        }
        return new Color(r, g, b,alpha);
    }

//    private static Color getSelValueColor(Color baseColor, double normalizedValue) {
//        return new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(),
//                60 + (int) Math.round(150 * normalizedValue));
//    }

    private static final PInputEventListener flowListener = new PBasicInputEventHandler() {

        @Override
        public void mouseEntered(PInputEvent event) {
            VisualEdge edge = getParentEdge(event.getPickedNode());
            if (edge != null) {
                edge.setHighlighted(true, false);
            }
            edge.getVisualGraph().showTooltip(edge, event.getPosition());
//            node.moveToFront();
        }

        @Override
        public void mouseExited(PInputEvent event) {
            VisualEdge edge = getParentEdge(event.getPickedNode());
            if (!edge.getVisible()) {
                return;
            }
            if (edge != null) {
                edge.setHighlighted(false, false);
            }
            edge.getVisualGraph().hideTooltip();
        }
    };

    private static final VisualEdge getParentEdge(PNode node) {
        PNode parent = node;
        while (parent != null && !(parent instanceof VisualEdge)) {
            parent = parent.getParent();
        }
        return (VisualEdge) parent;
    }

    public FlowMapCanvas getVisualGraph() {
        return canvas;
    }

    public void setHighlighted(boolean value, boolean inverse) {
        if (value) {
            line.setStrokePaint(getValueColor(
                    inverse ? STROKE_INVERSE_HIGHLIGHTED_PAINT : STROKE_HIGHLIGHTED_PAINT, false));
//        	startMarker.setStrokePaint(START_HIGHLIGHTED_MARKER_STROKE_PAINT);
//        	moveToFront();
        } else {
            line.setStrokePaint(getValueColor(STROKE_PAINT, false));
//        	startMarker.setStrokePaint(START_MARKER_STROKE_PAINT);
        }
        getSourceNode().setVisible(value);
        getTargetNode().setVisible(value);
    }

}
