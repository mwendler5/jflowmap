package ch.unifr.flowmap.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JComponent;

import ch.unifr.flowmap.data.IFlowData;
import ch.unifr.flowmap.data.INodeData;
import ch.unifr.flowmap.data.RandomNodeAndFlowData;

/**
 * @author Ilya Boyandin
 * @deprecated
 */
public class JFlowMap extends JComponent {

    private static final long serialVersionUID = 1L;
    private final RandomNodeAndFlowData data;
	private String attrName;
    
    public JFlowMap(String attrName) {
    	this.attrName = attrName;
        data = new RandomNodeAndFlowData(200, 2000, 15, 750, 15, 550, 1, 10);
    }
    
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2 = (Graphics2D)g;
        
        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, 800, 600);
//        g2.setColor(new Color(255, 255, 255, 13));
//        g2.setColor(new Color(238, 238, 0, 13));
        g2.setColor(new Color(0, 138, 238, 13));
//        g2.setStroke(new BasicStroke(2));
        
        INodeData nodeData = data.getNodeData();
        IFlowData flowData = data.getFlowData();
        
        for (int i = 0; i < flowData.numFlows(); i++) {
        	final int node1 = data.flowNode1(i);
        	final int node2 = data.flowNode2(i);

        	g2.setStroke(new BasicStroke((int)Math.round(flowData.getAttrValueAsDouble(i, attrName))));
            g2.drawLine(
                    (int)Math.round(nodeData.nodeX(node1)), (int)Math.round(nodeData.nodeY(node1)),
                    (int)Math.round(nodeData.nodeX(node2)), (int)Math.round(nodeData.nodeY(node2)));
        }
    }

}
