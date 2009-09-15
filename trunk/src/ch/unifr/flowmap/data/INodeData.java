package ch.unifr.flowmap.data;

import java.awt.geom.Point2D;

/**
 * @author Ilya Boyandin
 */
public interface INodeData {

	int numNodes();

	String nodeId(int nodeIdx);
	
	double nodeX(int nodeIdx);

	double nodeY(int nodeIdx);

	Point2D.Double nodePosition(int nodeIdx);

	String nodeLabel(int nodeIdx);

//	int numAttrs();
//
//	String getAttrLabel(int attrIdx);
//	
//	INodeAttrValue<?> getAttrValue(int nodeIdx, int attrIdx);

}
