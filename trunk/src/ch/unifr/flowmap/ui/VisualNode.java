package ch.unifr.flowmap.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;

import prefuse.data.Node;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventListener;
import edu.umd.cs.piccolo.nodes.PPath;

/**
 * @author Ilya Boyandin
 */
public class VisualNode extends PPath {

    private static final long serialVersionUID = 1L;
    
    private static final Stroke STROKE = new BasicStroke(1);
    private static final Color PAINT = new Color(100, 100, 100, 120);
    private static final Color STROKE_PAINT = new Color(255, 255, 255, 100);
    private static final Color SELECTED_PAINT = new Color(255, 0, 0, 120);
    private static final Color SELECTED_STROKE_PAINT = Color.white /*new Color(255, 255, 0, 255)*/;
    
    private final List<VisualEdge> outgoingEdges = new ArrayList<VisualEdge>();
    private final List<VisualEdge> incomingEdges = new ArrayList<VisualEdge>();

    private final VisualGraph visualGraph;

	private Node node;

	private double valueX;
	private double valueY;
    
    public VisualNode(VisualGraph visualGraph, Node node, double x, double y, double size) {
        super(new Ellipse2D.Double(x - size/2, y - size/2, size, size));
        this.valueX = x;
        this.valueY = y;
        setStrokePaint(STROKE_PAINT);
        setPaint(PAINT);
        setStroke(STROKE);
//    	this.x = x;
//    	this.y = y;
        this.node = node;
        this.visualGraph = visualGraph;
        addInputEventListener(INPUT_EVENT_HANDLER);
        setVisible(false);
	}

    public double getValueX() {
    	return valueX;
    }

    public double getValueY() {
		return valueY;
	}
    
//    public void createNodeShape() {
//        double sum = 0;
//        for (VisualEdge vedge : outgoingEdges) {
//        	sum += vedge.getEdge().getDouble("value");
//        }
//        for (VisualEdge vedge : incomingEdges) {
//			sum += vedge.getEdge().getDouble("value");
//		}
//        
//        double r = Math.sqrt(sum)/50;
//        PPath ppath = new PPath(new Ellipse2D.Double(x - r/2, y - r/2, r, r));
//        ppath.setStrokePaint(STROKE_PAINT);
//        ppath.setPaint(PAINT);
//        ppath.setStroke(STROKE);
//        
//        addChild(ppath);
//    }
    
    
	public Node getNode() {
		return node;
	}
   
    public VisualGraph getVisualGraph() {
		return visualGraph;
	}

	public String getLabel() {
		return node.getString(visualGraph.getLabelAttr());
	}

    private static final PInputEventListener INPUT_EVENT_HANDLER = new PBasicInputEventHandler() {
        @Override
        public void mouseEntered(PInputEvent event) {
            PNode node = event.getPickedNode();
            if (node instanceof VisualNode) {
                VisualNode fnode = (VisualNode)node;
//                fnode.setStrokePaint(SELECTED_STROKE_PAINT);
                fnode.setPaint(SELECTED_PAINT);
                fnode.setVisible(true);
                for (VisualEdge flow : fnode.outgoingEdges) {
                    flow.setHighlighted(true);
                }
                for (VisualEdge flow : fnode.incomingEdges) {
                    flow.setInverseHighlighted(true);
                }
                fnode.getVisualGraph().showTooltip(fnode, event.getPosition());
            }
        }

        @Override
        public void mouseExited(PInputEvent event) {
            PNode node = event.getPickedNode();
            if (node instanceof VisualNode) {
                VisualNode fnode = (VisualNode)node;
//                fnode.setStrokePaint(STROKE_PAINT);
                fnode.setPaint(PAINT);
                fnode.setVisible(false);
                for (VisualEdge flow : fnode.outgoingEdges) {
                    flow.setHighlighted(false);
                }
                for (VisualEdge flow : fnode.incomingEdges) {
                    flow.setInverseHighlighted(false);
                }
                fnode.getVisualGraph().hideTooltip();
            }
        }
    };
    
    public void addOutgoingEdge(VisualEdge flow) {
        outgoingEdges.add(flow);
    }

    public void addIncomingEdge(VisualEdge flow) {
        incomingEdges.add(flow);
    }

}
