package ch.unifr.flowmap.visuals;

import java.awt.Color;
import java.awt.geom.Line2D;

import prefuse.data.Edge;
import ch.unifr.flowmap.util.Stats;
import ch.unifr.flowmap.models.FlowMapModel;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventListener;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolox.util.PFixedWidthStroke;

/**
 * @author Ilya Boyandin
 */
public class VisualEdge extends PNode {

    private static final double EPS = 0.0000001;

    private static final long serialVersionUID = 1L;

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
    private final VisualFlowMap visualFlowMap;
    private final double value;

    private final VisualNode sourceNode;
    private final VisualNode targetNode;
    private final Edge edge;

    private final double edgeLength;


    public VisualEdge(VisualFlowMap visualFlowMap, Edge edge, VisualNode sourceNode, VisualNode targetNode) {
        this.edge = edge;
        this.sourceNode = sourceNode;
        this.targetNode = targetNode;
        this.visualFlowMap = visualFlowMap;

        targetNode.addIncomingEdge(this);
        sourceNode.addOutgoingEdge(this);

        final double x1 = sourceNode.getValueX();
        final double y1 = sourceNode.getValueY();
        final double x2 = targetNode.getValueX();
        final double y2 = targetNode.getValueY();

        value = edge.getDouble(visualFlowMap.getModel().getValueEdgeAttr());

        // Calc start/end marker positions
        this.edgeLength = dist(x1, y1, x2, y2);

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


        final double width = visualFlowMap.getMaxEdgeWidth();   // TODO: update dynamically
        line = new PPath(new Line2D.Double(
                sm_x /*- sin_a * width*/, sm_y /*- cos_a * width*/,
                em_x /*+ sin_a * width*/, em_y /*+ cos_a * width*/));
        
        addChild(line);

        updateEdgeColors();
        updateEdgeMarkerColors();
        updateEdgeWidth();
        updateVisibiliy();

        addInputEventListener(flowListener);
    }

    public double getNormalizedLogValue() {
        FlowMapModel model = visualFlowMap.getModel();
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

        Stats stats = visualFlowMap.getModel().getGraphStats().getValueEdgeAttrStats();
        nv = stats.normalize(value);

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
        float width = (float)(1 + nv * visualFlowMap.getMaxEdgeWidth());
        PFixedWidthStroke stroke = new PFixedWidthStroke(width);
        startMarker.setStroke(stroke);
        endMarker.setStroke(stroke);
        line.setStroke(stroke);
    }

    public void updateVisibiliy() {
        final FlowMapModel model = visualFlowMap.getModel();
        double valueFilterMin = model.getValueFilterMin();
        double valueFilterMax = model.getValueFilterMax();

        double edgeLengthFilterMin = model.getEdgeLengthFilterMin();
        double edgeLengthFilterMax = model.getEdgeLengthFilterMax();
        final double value = getValue();
        double length = getEdgeLength();
        final boolean visible =
                valueFilterMin <= value && value <= valueFilterMax    &&
                edgeLengthFilterMin <= length && length <= edgeLengthFilterMax
        ;
        setVisible(visible);
        setPickable(visible);
        setChildrenPickable(visible);
    }

    public Edge getEdge() {
        return edge;
    }

    public String getLabel() {
        return sourceNode.getNode().getString(visualFlowMap.getLabelAttr()) + " -> " +
                targetNode.getNode().getString(visualFlowMap.getLabelAttr());
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
        FlowMapModel model = visualFlowMap.getModel();
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

    private static final PInputEventListener flowListener = new PBasicInputEventHandler() {

        @Override
        public void mouseEntered(PInputEvent event) {
            VisualEdge edge = getParentEdge(event.getPickedNode());
            if (edge != null) {
                edge.setHighlighted(true, false, false);
            }
            edge.visualFlowMap.showTooltip(edge, event.getPosition());
//            node.moveToFront();
        }

        @Override
        public void mouseExited(PInputEvent event) {
            VisualEdge edge = getParentEdge(event.getPickedNode());
            if (!edge.getVisible()) {
                return;
            }
            if (edge != null) {
                edge.setHighlighted(false, false, false);
            }
            edge.visualFlowMap.hideTooltip();
        }
    };

    private static final VisualEdge getParentEdge(PNode node) {
        PNode parent = node;
        while (parent != null && !(parent instanceof VisualEdge)) {
            parent = parent.getParent();
        }
        return (VisualEdge) parent;
    }

    public VisualFlowMap getFlowMapCanvas() {
        return visualFlowMap;
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

    @Override
    public String toString() {
        return "VisualEdge{" +
                "label='" + getLabel() + "', " +
                "value=" + value +
                '}';
    }

    public double getEdgeLength() {
        return edgeLength;
    }
}
