package jflowmap.visuals;

import java.awt.Shape;
import java.awt.geom.Line2D;

import prefuse.data.Edge;
import edu.umd.cs.piccolo.nodes.PPath;

/**
 * @author Ilya Boyandin
 */
public class LineVisualEdge extends VisualEdge {

    private static final long serialVersionUID = 1L;

    public LineVisualEdge(VisualFlowMap visualFlowMap, Edge edge,
            VisualNode sourceNode, VisualNode targetNode) {
        super(visualFlowMap, edge, sourceNode, targetNode);
        targetNode.addIncomingEdge(this);
        sourceNode.addOutgoingEdge(this);

        final double x1 = sourceNode.getValueX();
        final double y1 = sourceNode.getValueY();
        final double x2 = targetNode.getValueX();
        final double y2 = targetNode.getValueY();

        Shape shape;
        if (isSelfLoop()) {
            shape = createSelfLoopShape();

        } else {
            shape = new Line2D.Double(x1, y1, x2, y2);
        }

        PPath ppath = new PPath(shape);
        setEdgePPath(ppath);
        addChild(ppath);
    }
}
