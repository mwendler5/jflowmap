package jflowmap.visuals;

import java.awt.Color;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jflowmap.util.GeomUtils;

import org.apache.log4j.Logger;

import prefuse.data.Node;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventListener;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolox.util.PFixedWidthStroke;

/**
 * @author Ilya Boyandin
 */
public class VisualNode extends PPath {

    private static Logger logger = Logger.getLogger(VisualNode.class);

    private static final long serialVersionUID = 1L;
    
    public static final int NO_CLUSTER = 0;

    private static final Stroke STROKE = new PFixedWidthStroke(1);
    private static final Color PAINT = new Color(255, 255, 255, 70);
    private static final Color STROKE_PAINT = new Color(255, 255, 255, 50);
    private static final Color HIGHLIGHTED_PAINT = new Color(255, 0, 0, 120);
    private static final Color SELECTED_STROKE_PAINT = new Color(255, 255, 0, 255);
    
    private final List<VisualEdge> outgoingEdges = new ArrayList<VisualEdge>();
    private final List<VisualEdge> incomingEdges = new ArrayList<VisualEdge>();

    private final VisualFlowMap visualFlowMap;

	private final Node node;

	private final double valueX;
	private final double valueY;
    
	private boolean selected;
	private boolean highlighted;
    private boolean alwaysVisible;

    private PPath clusterMarker;
    private int clusterId = NO_CLUSTER;
    private Color clusterColor = null;

    private double markerSize;

    public VisualNode(VisualFlowMap visualFlowMap, Node node, double x, double y, double size) {
        super(new Ellipse2D.Double(x - size/2, y - size/2, size, size));
        this.markerSize = size;
        if (Double.isNaN(x)  ||  Double.isNaN(y)) {
            logger.error("NaN coordinates passed in for node: " + node);
            throw new IllegalArgumentException("NaN coordinates passed in for node " + node);
        }
        this.valueX = x;
        this.valueY = y;
//        setStrokePaint(STROKE_PAINT);
        setPaint(PAINT);
//        setStroke(STROKE);
        setStroke(null);
//    	this.x = x;
//    	this.y = y;
        this.node = node;
        this.visualFlowMap = visualFlowMap;
        addInputEventListener(INPUT_EVENT_HANDLER);
//        setVisible(false);
        setVisible(true);
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
   
    public VisualFlowMap getVisualGraph() {
		return visualFlowMap;
	}
    
    public String getLabel() {
        StringBuilder sb = new StringBuilder();
        String labelAttr = visualFlowMap.getLabelAttr();
        if (labelAttr != null) {
            sb.append(node.getString(labelAttr));
        } else {
            sb.append("(").append(getValueX()).append(",").append(getValueY()).append(")");
        }
        return sb.toString();
    }

	public String getFullLabel() {
	    String fullLabel;
		if (clusterId != NO_CLUSTER) {
	        StringBuilder sb = new StringBuilder(getLabel());
		    sb.append(" [Cluster ").append(clusterId).append("]");
		    fullLabel = sb.toString();
		} else {
		    fullLabel = getLabel();
		}
		return fullLabel;
	}

    private static final PInputEventListener INPUT_EVENT_HANDLER = new PBasicInputEventHandler() {
        @Override
        public void mouseClicked(PInputEvent event) {
            VisualNode vnode = getParentVisualNode(event.getPickedNode());
            vnode.visualFlowMap.setSelectedNode(vnode.isSelected() ? null : vnode);
        }
        
        @Override
        public void mouseEntered(PInputEvent event) {
            VisualNode vnode = getParentVisualNode(event.getPickedNode());
            vnode.setHighlighted(true);
            vnode.getVisualGraph().showTooltip(vnode, event.getPosition());
        }

        @Override
        public void mouseExited(PInputEvent event) {
            VisualNode vnode = getParentVisualNode(event.getPickedNode());
            vnode.setHighlighted(true);
            if (!vnode.isSelected()) {
                vnode.setHighlighted(false);
            }
            vnode.getVisualGraph().hideTooltip();
        }
    };

    private static final VisualNode getParentVisualNode(PNode node) {
        PNode parent = node;
        while (parent != null && !(parent instanceof VisualNode)) {
            parent = parent.getParent();
        }
        return (VisualNode) parent;
    }

    public void addOutgoingEdge(VisualEdge flow) {
        outgoingEdges.add(flow);
    }
    
    public List<VisualEdge> getOutgoingEdges() {
        return Collections.unmodifiableList(outgoingEdges);
    }

    public void addIncomingEdge(VisualEdge flow) {
        incomingEdges.add(flow);
    }
    
    public List<VisualEdge> getIncomingEdges() {
        return Collections.unmodifiableList(incomingEdges);
    }
    
    /**
     * Returns an unmodifiable list of incoming edges if incoming is true
     * and outgoing if incoming is false.
     */
    public List<VisualEdge> getEdges(boolean incoming) {
        if (incoming) {
            return getIncomingEdges();
        } else {
            return getOutgoingEdges();
        }
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
//        System.out.println(this + ".setSelected("  + selected + ")");
        this.selected = selected;
        setVisible(true);
        setAlwaysVisible(selected);
        if (selected) {
            setStrokePaint(SELECTED_STROKE_PAINT);
        } else {
            setStrokePaint(STROKE_PAINT);
        }
        if (highlighted) {
//            System.out.println(" > " + this + " outgoing edges # = "  + outgoingEdges.size());
            for (VisualEdge flow : outgoingEdges) {
//                System.out.println(" > " + this + " outgoing edge "  + flow + " visible=" + flow.getVisible());
                if (flow.getVisible()) {
                    flow.getTargetNode().setAlwaysVisible(selected);
//                    flow.getTargetNode().setVisible(selected);
                    flow.setHighlighted(selected, true, false);
                }
            }
//            System.out.println(" > " + this + " incoming edges # = "  + incomingEdges.size());
            for (VisualEdge flow : incomingEdges) {
//                System.out.println(" > " + this + " incoming edge "  + flow + " visible=" + flow.getVisible());
                if (flow.getVisible()) {
                    flow.getSourceNode().setAlwaysVisible(selected);
//                    flow.getSourceNode().setVisible(selected);
                    flow.setHighlighted(selected, true, true);
                }
            }
            if (!selected) {
                setEdgesHighlighted(false);
            }
        }
    }

    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
//        setVisible(highlighted);
        if (highlighted) {
            setPaint(HIGHLIGHTED_PAINT);
        } else {
            setPaint(PAINT);
        }

        setEdgesHighlighted(highlighted);
    }

    private void setEdgesHighlighted(boolean highlighted) {
        for (VisualEdge flow : outgoingEdges) {
            if (flow.getVisible()) {
                flow.setHighlighted(highlighted, true, false);
            }
        }
        for (VisualEdge flow : incomingEdges) {
            if (flow.getVisible()) {
                flow.setHighlighted(highlighted, true, true);
            }
        }
    }

    @Override
    public void setVisible(boolean visible) {
        if (!visible  &&  alwaysVisible) {
            return;
        }
        super.setVisible(visible);
    }
    
    public boolean isAlwaysVisible() {
        return alwaysVisible;
    }

    public void setAlwaysVisible(boolean alwaysVisible) {
        this.alwaysVisible = alwaysVisible;
    }

    @Override
    public String toString() {
        return "VisualNode{" +
                "label=" + getLabel() +
                '}';
    }

    public void updatePickability() {
//        boolean pickable = false;
//        for (VisualEdge ve : outgoingEdges) {
//            if (ve.getVisible()) {
//                pickable = true;
//                break;
//            }
//        }
//        if (!pickable)
//        for (VisualEdge ve : incomingEdges) {
//            if (ve.getVisible()) {
//                pickable = true;
//                break;
//            }
//        }
//        setPickable(pickable);
//        setChildrenPickable(pickable);
    }

    
    public void hideClusterMarker() {
        if (clusterMarker != null) {
            removeChild(clusterMarker);
            clusterMarker = null;
        }
    }
    
    public void setClusterId(int clusterId) {
        this.clusterId = clusterId;
    }
    
    public int getClusterId() {
        return clusterId;
    }
    
    public void setClusterColor(Color clusterColor) {
        this.clusterColor = clusterColor;
        showClusterMarker();
    }
    
    public Color getClusterColor() {
        return clusterColor;
    }
    
    private void showClusterMarker() {
        if (clusterMarker == null) {
            double size = markerSize * 2;
            clusterMarker = new PPath(new Ellipse2D.Double(getValueX() - size/2, getValueY() - size/2, size, size));
            clusterMarker.setStroke(new PFixedWidthStroke(1));
            addChild(clusterMarker);
            clusterMarker.moveToBack();
        }
        clusterMarker.setPaint(clusterColor);
    }
    
    /**
     * Returns a list of the opposite nodes of the node's incoming/outgoing edges. 
     * @param ofIncomingEdges False for outgoing edges
     * @return Opposite nodes of incoming edges if ofIncomingEdges is true or of 
     *         outgoing edges if ofIncomingEdges is false.
     */
    public List<VisualNode> getOppositeNodes(boolean ofIncomingEdges) {
        List<VisualEdge> edges;
        if (ofIncomingEdges) {
            edges = incomingEdges;
        } else {
            edges = outgoingEdges;
        }
        List<VisualNode> nodes = new ArrayList<VisualNode>(edges.size());
        for (VisualEdge edge : edges) {
            VisualNode opposite;
            if (ofIncomingEdges) {
                opposite = edge.getSourceNode();
            } else {
                opposite = edge.getTargetNode();
            }
            nodes.add(opposite);
        }
        return nodes;
   }

   public double distanceTo(VisualNode node) {
       return GeomUtils.distance(getValueX(), getValueY(), node.getValueX(), node.getValueY());
   }

   public boolean hasIncomingEdges() {
       return incomingEdges.size() > 0;
   }

    public boolean hasOutgoingEdges() {
        return outgoingEdges.size() > 0;
    }

    public boolean hasEdges() {
        return hasIncomingEdges()  ||  hasOutgoingEdges();
    }

    public static final Comparator<VisualNode> LABEL_COMPARATOR = new Comparator<VisualNode>() {
        public int compare(VisualNode o1, VisualNode o2) {
            return o1.getLabel().compareTo(o2.getLabel());
        }
    };

}
