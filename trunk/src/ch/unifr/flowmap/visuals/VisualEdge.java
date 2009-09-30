package ch.unifr.flowmap.visuals;

import prefuse.data.Edge;
import ch.unifr.flowmap.models.FlowMapModel;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventListener;

/**
 * @author Ilya Boyandin
 */
public abstract class VisualEdge extends PNode {

    private static final long serialVersionUID = 1L;

    private final VisualFlowMap visualFlowMap;

    private final VisualNode sourceNode;
    private final VisualNode targetNode;
    private final Edge edge;

    private final double edgeLength;
    
    public VisualEdge(VisualFlowMap visualFlowMap, Edge edge, VisualNode sourceNode, VisualNode targetNode) {
        this.edge = edge;
        this.sourceNode = sourceNode;
        this.targetNode = targetNode;
        this.visualFlowMap = visualFlowMap;
    
        final double x1 = sourceNode.getValueX();
        final double y1 = sourceNode.getValueY();
        final double x2 = targetNode.getValueX();
        final double y2 = targetNode.getValueY();
        this.edgeLength = dist(x1, y1, x2, y2);

        addInputEventListener(visualEdgeListener);
    }

    public abstract void setHighlighted(boolean value, boolean showDirection, boolean outgoing);

    public abstract void updateEdgeWidth();

    public abstract void updateEdgeColors();

    public abstract void updateEdgeMarkerColors();

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
