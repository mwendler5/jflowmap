package jflowmap.visuals;

import java.awt.Color;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.Stroke;

import jflowmap.models.FlowMapParamsModel;
import jflowmap.util.GeomUtils;
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

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(VisualEdge.class);

	private static final float[] DEFAULT_GRADIENT_FRACTIONS = new float[] { 0.0f, 1.0f };
    private static final float MIN_FRACTION_DIFF = 1e-5f;

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
            this.edgeLength = GeomUtils.distance(x1, y1, x2, y2);
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

//    public abstract void updateEdgeMarkerColors();

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
            Stats stats = visualFlowMap.getGraphStats().getValueEdgeAttrStats();
            nv = stats.normalizeLog(value);
        }
        if (Double.isNaN(nv)) {
            logger.error("NaN normalized log value for edge: " + this);
        }
        return nv;
    }

    public double getNormalizedValue() {
        double nv;
    
        Stats stats = visualFlowMap.getGraphStats().getValueEdgeAttrStats();
        nv = stats.normalize(getValue());

        if (Double.isNaN(nv)) {
            logger.error("NaN normalized value for edge: " + this);
        }
    
        return nv;
    }

//    protected Color getValueColor(Color baseColor, boolean forMarker) {
//        FlowMapParamsModel model = getVisualFlowMap().getModel();
//        final double normalizedValue = getNormalizedLogValue();
//        int r = (int) Math.round(normalizedValue * baseColor.getRed());
//        int g = (int) Math.round(normalizedValue * baseColor.getGreen());
//        int b = (int) Math.round(normalizedValue * baseColor.getBlue());
//        int alpha;
//        if (baseColor.getAlpha() == 255) {
//            if (forMarker) {
//                alpha = model.getDirectionMarkerAlpha();
//            } else {
//                alpha = model.getEdgeAlpha();
//            }
//        } else {
//            alpha = baseColor.getAlpha();
//        }
//        return new Color(r, g, b, alpha);
//    }
    
    protected Paint createPaint() {
		// TODO: use colors from color scheme
        FlowMapParamsModel model = getVisualFlowMap().getModel();
        final double normalizedValue = getNormalizedLogValue();
        int intensity = (int)Math.round(255 * normalizedValue);
        int alpha = model.getEdgeAlpha();
        if (isSelfLoop()) {
            return new Color(intensity, intensity, 0, alpha);	// mix of red and green
        } else {
        	if (!model.getShowDirectionMarkers()  &&  !model.getFillEdgesWithGradient()) {
        		return new Color(intensity, intensity, intensity, alpha);	// white
        	} else {
        		Color startEdgeColor, endEdgeColor;
        		if (model.getFillEdgesWithGradient()) {
    				startEdgeColor = new Color(intensity, 0, 0, alpha);
    				endEdgeColor = new Color(0, intensity, 0, alpha);
        		} else {
        			// TODO: use a special paint (not gradient) for this case
					startEdgeColor = new Color(intensity, intensity, intensity, alpha);
					endEdgeColor = startEdgeColor;
        		}

				float[] fractions = null;
        		Color[] colors = null;
	        	if (model.getShowDirectionMarkers()) {
	        		float markerSize;
		        	if (model.getUseProportionalDirectionMarkers()) {
		        		markerSize = (float)model.getDirectionMarkerSize();
		        	} else {
		        		Stats lstats = visualFlowMap.getGraphStats().getEdgeLengthStats();
						markerSize = (float)Math.min(
								.5 - MIN_FRACTION_DIFF,	 // the markers must not be longer than half of an edge
								((lstats.min + model.getDirectionMarkerSize() * (lstats.max - lstats.min)) 
								/ 2)			
								/ edgeLength	// the markers must be of equal length for every edge
												// (excepting the short ones)
						);
		        	}
		        	if (markerSize - MIN_FRACTION_DIFF < 0) {
		        		markerSize = MIN_FRACTION_DIFF;
		        	}
		        	if (markerSize > 0.5f - MIN_FRACTION_DIFF) {
		        		markerSize = 0.5f - MIN_FRACTION_DIFF;
		        	}
		            int markerAlpha = model.getDirectionMarkerAlpha();
		        	Color startMarkerColor = new Color(intensity, 0, 0, markerAlpha);
		        	Color endMarkerColor = new Color(0, intensity, 0, markerAlpha);
					fractions = new float[] {
							markerSize - MIN_FRACTION_DIFF,			// start marker 
							markerSize, 1.0f - markerSize,			// line 
							1.0f - markerSize + MIN_FRACTION_DIFF	// end marker
					};
					colors = new Color[] {
							startMarkerColor,
							startEdgeColor,
							endEdgeColor,
							endMarkerColor,
					};
	        	} else {
	        		fractions = DEFAULT_GRADIENT_FRACTIONS;
	        		colors = new Color[] { startEdgeColor, endEdgeColor };
	        	}
//	        	System.out.println(Arrays.toString(fractions));
				return new LinearGradientPaint(
	                    (float)getSourceX(), (float)getSourceY(),
	                    (float)getTargetX(), (float)getTargetY(),
	                    fractions,
	                    colors
	            );
        	}
        	
        }
    }

    public void updateEdgeColors() {
        PPath ppath = getEdgePPath();
        if (ppath != null) {
            ppath.setStrokePaint(createPaint());
        }
    }

    public void setHighlighted(boolean value, boolean showDirection, boolean outgoing) {
        PPath ppath = getEdgePPath();
        if (ppath != null) {
            Paint paint;
            if (value) {
                Color color;
                if (showDirection) {
                    color = (outgoing ? STROKE_HIGHLIGHTED_OUTGOING_PAINT : STROKE_HIGHLIGHTED_INCOMING_PAINT);
                } else {
                    color = STROKE_HIGHLIGHTED_PAINT;
                }
//                paint = getValueColor(color, false);
                paint = color;
            } else {
                paint = createPaint();
            }
            ppath.setStrokePaint(paint);
//            getSourceNode().setVisible(value);
//            getTargetNode().setVisible(value);
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
//        updateEdgeMarkerColors();
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

}
