package ch.unifr.flowmap.data;


/**
 * @author Ilya Boyandin
 */
public interface INodeAndFlowData {

	INodeData getNodeData();

	IFlowData getFlowData();

	String getNodeId(int nodeIdx);

	int getNodeIndex(String nodeId);

    int flowNode2(int flowIdx);

    int flowNode1(int flowIdx);

    double getOutgoingTotal(String nodeId, String attrName);

    double getIncomingTotal(String nodeId, String attrName);
   
}
