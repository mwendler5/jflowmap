package ch.unifr.flowmap.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Insets;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Tuple;
import prefuse.data.tuple.TupleSet;
import ch.unifr.flowmap.data.MinMax;
import ch.unifr.flowmap.util.PiccoloUtils;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.util.PBounds;

/**
 * @author Ilya Boyandin
 */
public class VisualGraph extends PCanvas {

    private static final int DEFAULT_NODE_SIZE = 6;
	private static final int SHORT_ANIMATION_DURATION = 500;
	private static final long serialVersionUID = 1L;
	private final PValueTooltip tooltipBox;
	private final Graph graph;
	private final String edgeValueAttr;
	private String xCoordAttr = "x";
	private String yCoordAttr = "y";
	private String labelAttr = "tooltip";
	private PBounds nodeBounds;

    public VisualGraph(Graph graph, String edgeValueAttrName, String labelAttrName) {
    	this.graph = graph;
    	this.edgeValueAttr = edgeValueAttrName;
    	this.labelAttr = labelAttrName;
    	
//        setBackground(new Color(47, 89, 134));
        setBackground(Color.BLACK);
        
        addInputEventListener(new ZoomHandler(.5, 50));
        setPanEventHandler(new PanHandler());
        
//        Stroke stroke = new BasicStroke(2);
//        Color STROKE_PAINT = new Color(238, 238, 0, 100);
//        Color STROKE_PAINT = new Color(255, 255, 255, 13);

        PNode nodeLayer = new PNode();
        
        final int numNodes = graph.getNodeCount();

        MinMax xStats = getNodeAttrStats(xCoordAttr);
        MinMax yStats = getNodeAttrStats(yCoordAttr);
        
        System.out.println("xStats: " + xStats);
        System.out.println("yStats: " + yStats);

		Map<Node, VisualNode> nodesToVisuals = new HashMap<Node, VisualNode>();
        for (int i = 0; i < numNodes; i++) {
            Node node = graph.getNode(i);
            
			VisualNode vnode = new VisualNode(this, node,
					node.getDouble(xCoordAttr) - xStats.min,
					node.getDouble(yCoordAttr) - yStats.min,
					DEFAULT_NODE_SIZE
			);
            nodeLayer.addChild(vnode);
            nodesToVisuals.put(node, vnode);
        }
        
        
        PNode flowLayer = new PNode();
        
        final int numEdges = graph.getEdgeCount();
        for (int i = 0; i < numEdges; i++) {
        	Edge edge = graph.getEdge(i);
        	
//            double value = edge.getDouble(valueAttrName);
//            if (value < 100) continue;
            
            VisualNode fromNode = nodesToVisuals.get(edge.getSourceNode());
            VisualNode toNode = nodesToVisuals.get(edge.getTargetNode());
            
			VisualEdge ve = new VisualEdge(this, edge, fromNode, toNode);
            flowLayer.addChild(ve);
        }

        getLayer().addChild(flowLayer);
        getLayer().addChild(nodeLayer);
        
        
//        for (PFlowNode node : nodes) {
//            node.moveToFront();
//            node.setVisible(false);
//        }

        tooltipBox = new PValueTooltip();
        tooltipBox.setVisible(false);
        tooltipBox.setPickable(false);
        PCamera camera = getCamera();
		camera.addChild(tooltipBox);
        
        
        nodeBounds = new PBounds(
//        		-500, -250, 500, 250 
        		0, 0, (xStats.max - xStats.min)/2, (yStats.max - yStats.min)/2
        );
//        nodeLayer.localToGlobal(nodeBounds);
        
        
//        PPath pp = new PPath(nodeBounds, new BasicStroke(2));
//        pp.setStrokePaint(Color.red);
//		nodeLayer.addChild(pp);
       
//		camera.globalToLocal(nodeBounds);
//		camera.localToView(nodeBounds);
//		camera.viewToLocal(nodeBounds);
		
//		getCamera().setViewBounds(
//				new Rectangle2D.Double(-500, -250, 500, 250));
		
//		camera.setViewBounds(nodeBounds);
//		camera.setOffset(xStats.min, yStats.min);
//		camera.setOffset(0, 0);
		
//        fitInCameraView(false);
//        camera.viewToLocal(rect);
//        camera.globalToLocal(rect);
//        getCamera().setViewBounds(new Rectangle2D.Double(
//        		-2800, -2400, 2600, 2800));
        
		
//		for (VisualNode vnode : nodesToVisuals.values()) {
//			vnode.createNodeShape();
//		}
		
	}
    
    private static final Insets contentInsets = new Insets(10, 10, 10, 10);
    
    private Insets getContentInsets() {
        return contentInsets;
    }
    
    public void fitInCameraView(boolean animate) {
        if (nodeBounds != null) {
            Insets insets = getContentInsets();
            insets.left += 5;
            insets.top += 5;
            insets.bottom += 5;
            insets.right += 5;
            if (animate) {
                PiccoloUtils.animateViewToPaddedBounds(getCamera(), nodeBounds, insets, SHORT_ANIMATION_DURATION);
            } else {
                PiccoloUtils.setViewPaddedBounds(getCamera(), nodeBounds, insets);
            }
        }
    }

    private final Map<String, MinMax> tupleStats = new HashMap<String, MinMax>();
    
    public String getEdgeValueAttr() {
		return edgeValueAttr;
	}
    
    public String getLabelAttr() {
		return labelAttr;
	}
    
    public MinMax getEdgeValueAttrStats(String attrName) {
    	String key = "edge-" + attrName;
		MinMax stats = tupleStats.get(key);
    	if (stats == null) {
    		stats = getTupleStats(graph.getEdges(), attrName);
        	System.out.println("Attr stats for '" + attrName + "': " + stats);
    		tupleStats.put(key, stats);
    	}
    	return stats;
    }
    
    public MinMax getNodeAttrStats(String attrName) {
    	String key = "node-" + attrName;
    	MinMax stats = tupleStats.get(key);
    	if (stats == null) {
    		stats = getTupleStats(graph.getNodes(), attrName);
    		tupleStats.put(key, stats);
    	}
		return stats;
    }

    public static MinMax getTupleStats(TupleSet tupleSet, String attrName) {
        Iterator<?> it = tupleSet.tuples();

        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;

        while (it.hasNext()) {
        	Tuple tuple = (Tuple)it.next();
			final double v = tuple.getDouble(attrName);
			if (v >= 0) {
	            if (v > max)
	                max = v;
	            if (v < min)
	                min = v;
			}
        }
        return new MinMax(min, max);
    }

    public void showTooltip(PNode component, Point2D pos) {
    	if (component instanceof VisualNode) {
    	    VisualNode vnode = (VisualNode)component;
//    		tooltipBox.setText(fnode.getId(), nodeData.nodeLabel(nodeIdx), "");
			tooltipBox.setText(
					vnode.getLabel(),
			        ""
//			        "Outgoing " + selectedFlowAttrName + ": " + graph.getOutgoingTotal(fnode.getId(), selectedFlowAttrName) + "\n" +
//			        "Incoming " + selectedFlowAttrName + ": " + graph.getIncomingTotal(fnode.getId(), selectedFlowAttrName)
			        ,
			        "");
    	} else if (component instanceof VisualEdge) {
            VisualEdge edge = (VisualEdge)component;
            tooltipBox.setText(
//                    flow.getStartNodeId() + " - " + flow.getEndNodeId(), 
            		edge.getLabel(),
                    edgeValueAttr + ": ", Double.toString(edge.getValue()));
    	} else {
    	    return;
    	}
//			Point2D pos = event.getPosition();
        final PBounds vb = getCamera().getBoundsReference();
        final PBounds tb = tooltipBox.getBoundsReference();
        double x = pos.getX() + 8;
        double y = pos.getY() + 8;
        if (x + tb.getWidth() > vb.getWidth()) {
            final double _x = pos.getX() - tb.getWidth() - 8;
            if (vb.getX() - _x < x + tb.getWidth() - vb.getMaxX()) {
                x = _x;
            }
        }
        if (y + tb.getHeight() > vb.getHeight()) {
            final double _y = pos.getY() - tb.getHeight() - 8;
            if (vb.getY() - _y < y + tb.getHeight() - vb.getMaxY()) {
                y = _y;
            }
        }
    	pos = new Point2D.Double(x, y);
    	getCamera().viewToLocal(pos);
        tooltipBox.setPosition(pos.getX(), pos.getY());
        tooltipBox.setVisible(true);
    }

    public void hideTooltip() {
        tooltipBox.setVisible(false);
    }
}
