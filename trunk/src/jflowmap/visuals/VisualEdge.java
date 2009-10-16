package jflowmap.visuals;

import java.awt.Color;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.Stroke;

import jflowmap.models.FlowMapParamsModel;
import jflowmap.util.Stats;

import org.apache.log4j.Logger;

import prefuse.data.Edge;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventListener;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolox.util.PFixedWidthStroke;

/**
 * @author Ilya Boyandin
 */
public abstract class VisualEdge extends PNode {

    private static Logger logger = Logger.getLogger(VisualEdge.class);

    private static final long serialVersionUID = 1L;

    private static final Color STROKE_PAINT = new Color(255, 255, 255);
    private static final Color STROKE_HIGHLIGHTED_PAINT = new Color(0, 0, 255, 200);
    private static final Color STROKE_HIGHLIGHTED_INCOMING_PAINT = new Color(255, 0, 0, 200);
    private static final Color STROKE_HIGHLIGHTED_OUTGOING_PAINT = new Color(0, 255, 0, 200);

    private final VisualFlowMap visualFlowMap;

    private final VisualNode sourceNode;
    private final VisualNode targetNode;
    private final Edge edge;

    private final double edgeLength;

    private PPath edgePPath;
    
    public VisualEdge(VisualFlowMap visualFlowMap, Edge edge, VisualNode sourceNode, VisualNode targetNode) {
        this.edge = edge;
        this.sourceNode = sourceNode;
        this.targetNode = targetNode;
        this.visualFlowMap = visualFlowMap;
    
        if (isSelfLoop()) {
            this.edgeLength = 0;
        } else {
            final double x1 = sourceNode.getValueX();
            final double y1 = sourceNode.getValueY();
            final double x2 = targetNode.getValueX();
            final double y2 = targetNode.getValueY();
            this.edgeLength = dist(x1, y1, x2, y2);
        }

        addInputEventListener(visualEdgeListener);
    }

    
    public double getSourceX() {
        return sourceNode.getValueX();
    }
    
    public double getSourceY() {
        return sourceNode.getValueY();
    }
    
    public double getTargetX() {
        return targetNode.getValueX();
    }
    
    public double getTargetY() {
        return targetNode.getValueY();
    }
    
    protected void setEdgePPath(PPath ppath) {
        this.edgePPath = ppath;
    }    
    
    protected PPath getEdgePPath() {
        return edgePPath;
    }
    
    public boolean isSelfLoop() {
        return sourceNode == targetNode;
    }
    
    public void updateEdgeWidth() {
        PPath ppath = getEdgePPath();
        if (ppath != null) {
            ppath.setStroke(createStroke());
        }
    }

    public abstract void updateEdgeMarkerColors();

    public void updateVisibiliy() {
        final FlowMapParamsModel model = visualFlowMap.getModel();
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
//        System.out.println(this + "  " + visible  + "  filter:[" + valueFilterMin + "-" + valueFilterMax + "] value = " + value);
        setVisible(visible);
        setPickable(visible);
        setChildrenPickable(visible);
    }

    public Edge getEdge() {
        return edge;
    }

    public VisualFlowMap getVisualFlowMap() {
        return visualFlowMap;
    }

    public String getLabel() {
        return sourceNode.getNode().getString(visualFlowMap.getLabelAttr()) + " -> " +
               targetNode.getNode().getString(visualFlowMap.getLabelAttr());
    }

    public double getValue() {
        return edge.getDouble(visualFlowMap.getModel().getValueEdgeAttr());
    }

    public double getEdgeLength() {
        return edgeLength;
    }

    public VisualNode getSourceNode() {
        return sourceNode;
    }

    public VisualNode getTargetNode() {
        return targetNode;
    }

    @Override
    public String toString() {
        return "VisualEdge{" +
                "label='" + getLabel() + "', " +
                "value=" + getValue() +
        '}';
    }

    public double getNormalizedLogValue() {
        FlowMapParamsModel model = getVisualFlowMap().getModel();
        double value = getValue();
        double nv;
        if (model.getAutoAdjustEdgeColorScale()) {
            double minLog = 1.0;
            double maxLog = Math.log(model.getValueFilterMax() - model.getValueFilterMin());
            if (maxLog == minLog) {
                nv = 1.0;
            } else {
                nv = (Math.log(value - model.getValueFilterMin()) - minLog) / (maxLog - minLog);
            }
        } else {
            Stats stats = model.getGraphStats().getValueEdgeAttrStats();
            nv = stats.normalizeLog(value);
        }
        if (Double.isNaN(nv)) {
            logger.error("NaN normalized log value for edge: " + this);
        }
        return nv;
    }

    public double getNormalizedValue() {
        double nv;
    
        Stats stats = getVisualFlowMap().getModel().getGraphStats().getValueEdgeAttrStats();
        nv = stats.normalize(getValue());

        if (Double.isNaN(nv)) {
            logger.error("NaN normalized value for edge: " + this);
        }
    
        return nv;
    }

    protected Color getValueColor(Color baseColor, boolean forMarker) {
        FlowMapParamsModel model = getVisualFlowMap().getModel();
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
        return new Color(r, g, b, alpha);
    }
    
    protected Paint getEdgeGradientPaint() {
        FlowMapParamsModel model = getVisualFlowMap().getModel();
        final double normalizedValue = getNormalizedLogValue();
        int intensity = (int)Math.round(255 * normalizedValue);
        int alpha = model.getEdgeAlpha();
        if (isSelfLoop()) {
            return new Color(intensity, intensity, 0, alpha);
        } else {
            return new LinearGradientPaint(
                    (float)getSourceX(), (float)getSourceY(),
                    (float)getTargetX(), (float)getTargetY(),
                    new float[] {0.0f, 1.0f}, new Color[] {
                    new Color(intensity, 0, 0, alpha), // red
                    new Color(0, intensity, 0, alpha)  // green
            });
        }
    }

    public void updateEdgeColors() {
        PPath ppath = getEdgePPath();
        if (ppath != null) {
//            Paint paint = getValueColor(STROKE_PAINT, false);
            Paint paint = getEdgeGradientPaint();
            ppath.setStrokePaint(paint);
        }
    }

    public void setHighlighted(boolean value, boolean showDirection, boolean outgoing) {
        PPath ppath = getEdgePPath();
        if (ppath != null) {
            Color paint;
            if (value) {
                if (showDirection) {
                    paint = (outgoing ? STROKE_HIGHLIGHTED_OUTGOING_PAINT : STROKE_HIGHLIGHTED_INCOMING_PAINT);
                } else {
                    paint = STROKE_HIGHLIGHTED_PAINT;
                }
            } else {
                paint = STROKE_PAINT;
            }
            Color color = getValueColor(paint, false);
            ppath.setStrokePaint(color);
            getSourceNode().setVisible(value);
            getTargetNode().setVisible(value);
        }
    }

    protected Stroke createStroke() {
        double nv = getNormalizedValue();
        float width = (float)(1 + nv * getVisualFlowMap().getMaxEdgeWidth());
        return new PFixedWidthStroke(width);
//        return new BasicStroke(width);
    }

    public void update() {
        updateEdgeColors();
        updateEdgeMarkerColors();
        updateEdgeWidth();
        updateVisibiliy();
    }

    private static final PInputEventListener visualEdgeListener = new PBasicInputEventHandler() {
        @Override
        public void mouseEntered(PInputEvent event) {
            VisualEdge ve = getParentVisualEdge(event.getPickedNode());
            if (ve != null) {
                ve.setHighlighted(true, false, false);
            }
            ve.getVisualFlowMap().showTooltip(ve, event.getPosition());
//            node.moveToFront();
        }

        @Override
        public void mouseExited(PInputEvent event) {
            VisualEdge ve = getParentVisualEdge(event.getPickedNode());
            if (!ve.getVisible()) {
                return;
            }
            if (ve != null) {
                ve.setHighlighted(false, false, false);
            }
            ve.getVisualFlowMap().hideTooltip();
        }
    };

    private static final VisualEdge getParentVisualEdge(PNode node) {
        PNode parent = node;
        while (parent != null && !(parent instanceof VisualEdge)) {
            parent = parent.getParent();
        }
        return (VisualEdge) parent;
    }

    private static final double dist(double x1, double y1, double x2, double y2) {
        final double dx = (x1 - x2);
        final double dy = (y1 - y2);
        return Math.sqrt(dx * dx + dy * dy);
    }

}
