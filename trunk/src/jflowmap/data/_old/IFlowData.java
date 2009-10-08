package jflowmap.data._old;


/**
 * @author Ilya Boyandin
 */
public interface IFlowData {

	int numFlows();

	String getFlowStartNodeId(int flowIdx);

	String getFlowEndNodeId(int flowIdx);
	
	String getFlowLabel(int flowIdx);

	int numAttrs();

	String getAttrName(int attrIdx);
	
	IAttrValue<?> getAttrValue(int flowIdx, String attrName);

	double getAttrValueAsDouble(int flowIdx, String attrName);

}
