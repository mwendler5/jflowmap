package jflowmap.visuals;

import java.awt.Color;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jflowmap.geom.GeomUtils;
import jflowmap.geom.Point;
import jflowmap.util.PiccoloUtils;

import org.apache.log4j.Logger;

import prefuse.data.Node;

import com.google.common.base.Function;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventListener;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolox.util.PFixedWidthStroke;

/**
 * @author Ilya Boyandin
 */
public class VisualNode extends PNode {

    private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(VisualNode.class);
    public enum Attributes {
        SELECTED, HIGHLIGHTED, CLUSTER_TAG
    }

    private static final Stroke STROKE = null;
    private static final Stroke HIGHLIGHTED_STROKE = new PFixedWidthStroke(1);
    private static final Stroke SELECTED_STROKE = new PFixedWidthStroke(2);
    private static final Color PAINT = new Color(255, 255, 255, 70);
    private static final Color HIGHLIGHTED_PAINT = new Color(200, 200, 0, 200);
    private static final Color SELECTED_PAINT = HIGHLIGHTED_PAINT;
    private static final Color STROKE_PAINT = new Color(255, 255, 255, 200);
    private static final Color SELECTED_STROKE_PAINT = new Color(255, 255, 0, 255);

    private final List<VisualEdge> outgoingEdges = new ArrayList<VisualEdge>();
    private final List<VisualEdge> incomingEdges = new ArrayList<VisualEdge>();

    private final VisualFlowMap visualFlowMap;

	private final Node node;

	private final double valueX;
	private final double valueY;

    private PPath clusterMarker;

    private final double markerSize;
    private final PPath marker;
    private PNode clusterMembers;

    public VisualNode(VisualFlowMap visualFlowMap, Node node, double x, double y, double size) {
//        super();
        this.markerSize = size;
        if (Double.isNaN(x)  ||  Double.isNaN(y)) {
            logger.error("NaN coordinates passed in for node: " + node);
            throw new IllegalArgumentException("NaN coordinates passed in for node " + node);
        }
        setX(x);
        setY(y);
        this.valueX = x;
        this.valueY = y;
        this.marker = new PPath(createNodeShape(x, y, size));
//        marker.setStrokePaint(STROKE_PAINT);
//        marker.setStroke(STROKE);
        marker.setPaint(PAINT);
        marker.setStroke(null);
//    	this.x = x;
//    	this.y = y;
        this.node = node;
        this.visualFlowMap = visualFlowMap;
        addInputEventListener(INPUT_EVENT_HANDLER);
//        setVisible(false);
//        setVisible(true);
        addChild(marker);

        VisualNodeCluster cluster = VisualNodeCluster.getJoinedFlowMapNodeCluster(node);
        if (cluster != null) {
            clusterMembers = new PNode();
            Color origNodePaint = new Color(100, 100, 100, 100);
            for (VisualNode origNode : cluster) {
                PPath pnode = new PPath(createNodeShape(origNode.getValueX(), origNode.getValueY(), size));
                pnode.setPaint(origNodePaint);
                clusterMembers.addChild(pnode);


                PPath pline = new PPath(new Line2D.Double(origNode.getPosition(), this.getPosition()));
                pline.setStrokePaint(origNodePaint);
                clusterMembers.addChild(pline);
            }
            visualFlowMap.addChild(clusterMembers);
            clusterMembers.moveToBack();
        }

        updateVisibility();
	}

    public Point2D getPosition() {
        return new Point2D.Double(valueX, valueY);
    }

    private Shape createNodeShape(double x, double y, double size) {
        return new Ellipse2D.Double(x - size/2, y - size/2, size, size);
    }

    public void updateVisibility() {
        boolean visibility =
            visualFlowMap.getModel().getShowNodes()  ||  isHighlighted()  ||  isSelected();
        marker.setVisible(visibility);
        if (clusterMembers != null) {
            clusterMembers.setVisible(visibility);
        }
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

    public void setClusterTag(ClusterTag tag) {
        addAttribute(Attributes.CLUSTER_TAG, tag);
        updateClusterMarker();
    }

    public ClusterTag getClusterTag() {
        return (ClusterTag)getAttribute(Attributes.CLUSTER_TAG, null);
    }

	public String getFullLabel() {
	    String fullLabel;
	    ClusterTag clusterTag = getClusterTag();
		if (clusterTag != null) {
	        StringBuilder sb = new StringBuilder(getLabel());
		    sb.append(" [Cluster ").append(clusterTag.getClusterId()).append("]");
		    fullLabel = sb.toString();
		} else {
		    fullLabel = getLabel();
		}
		return fullLabel;
	}

    private static final PInputEventListener INPUT_EVENT_HANDLER = new PBasicInputEventHandler() {
        @Override
        public void mouseClicked(PInputEvent event) {
            VisualNode vnode = PiccoloUtils.getParentNodeOfType(event.getPickedNode(), VisualNode.class);
            vnode.visualFlowMap.setSelectedNode(vnode.isSelected() ? null : vnode);
        }

        @Override
        public void mouseEntered(PInputEvent event) {
            VisualNode vnode = PiccoloUtils.getParentNodeOfType(event.getPickedNode(), VisualNode.class);
            vnode.setHighlighted(true);
            vnode.getVisualGraph().showTooltip(vnode, event.getPosition());
        }

        @Override
        public void mouseExited(PInputEvent event) {
            VisualNode vnode = PiccoloUtils.getParentNodeOfType(event.getPickedNode(), VisualNode.class);
            vnode.setHighlighted(false);
            vnode.getVisualGraph().hideTooltip();
        }
    };

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
     * Returns a newly created and modifiable list of
     * incoming and outgoing edges of the node.
     */
    public List<VisualEdge> getEdges() {
        List<VisualEdge> edges = new ArrayList<VisualEdge>(incomingEdges.size() + outgoingEdges.size());
        edges.addAll(outgoingEdges);
        edges.addAll(incomingEdges);
        return edges;
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
        return getBooleanAttribute(Attributes.SELECTED.name(), false);
    }

    public void setSelected(boolean selected) {
        addAttribute(Attributes.SELECTED.name(), selected);
        updateVisibility();
        updateColorsAndStroke();
        updateEdgeColors();
    }

    public boolean isHighlighted() {
        return getBooleanAttribute(Attributes.HIGHLIGHTED.name(), false);
    }

    public void setHighlighted(boolean highlighted) {
        addAttribute(Attributes.HIGHLIGHTED.name(), highlighted);
        updateVisibility();
        updateColorsAndStroke();
        updateEdgeColors();
    }

    private void updateColorsAndStroke() {
        boolean selected = isSelected();
        boolean highlighted = isHighlighted();
        if (selected) {
            marker.setStroke(SELECTED_STROKE);
            marker.setStrokePaint(SELECTED_STROKE_PAINT);
            marker.setPaint(SELECTED_PAINT);
        } else if (highlighted) {
            marker.setStroke(HIGHLIGHTED_STROKE);
            marker.setStrokePaint(STROKE_PAINT);
            marker.setPaint(HIGHLIGHTED_PAINT);
        } else {
            marker.setStroke(STROKE);
            marker.setStrokePaint(STROKE_PAINT);
            marker.setPaint(PAINT);
        }
    }

    private void updateEdgeColors() {
        boolean highlightEdges = (isSelected() || isHighlighted());
        for (VisualEdge flow : outgoingEdges) {
            if (flow.getVisible()) {
                flow.setHighlighted(highlightEdges, true, false);
            }
        }
        for (VisualEdge flow : incomingEdges) {
            if (flow.getVisible()) {
                flow.setHighlighted(highlightEdges, true, true);
            }
        }
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


    private void updateClusterMarker() {
        ClusterTag clusterTag = getClusterTag();
        if (clusterTag == null  ||  !clusterTag.isVisible()) {
            if (clusterMarker != null) {    // hide marker
                removeChild(clusterMarker);
                clusterMarker = null;
            }
        } else {
            if (clusterMarker == null) {    // show marker
                double size = markerSize * 2;
                clusterMarker = new PPath(new Ellipse2D.Double(getValueX() - size/2, getValueY() - size/2, size, size));
                clusterMarker.setStroke(new PFixedWidthStroke(1));
                addChild(clusterMarker);
                clusterMarker.moveToBack();
            }
            clusterMarker.setPaint(clusterTag.getClusterPaint());
        }
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

    public static final Function<VisualNode, Point> TRANSFORM_NODE_TO_POSITION = new Function<VisualNode, Point>() {
        public Point apply(VisualNode node) {
            return new Point(node.getX(), node.getY());
        }
    };
}
