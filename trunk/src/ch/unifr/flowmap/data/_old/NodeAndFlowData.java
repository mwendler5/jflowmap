package ch.unifr.flowmap.data._old;

import ch.unifr.flowmap.util.Stats;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ilya Boyandin
 */
public class NodeAndFlowData implements INodeAndFlowData {

	private final INodeData nodeData;
	private final IFlowData flowData;
	private final Map<String, Integer> nodeIdToIndex;
    private final int[] flowStartNodeIdx;
    private final int[] flowEndNodeIdx;

	public NodeAndFlowData(INodeData nodeData, IFlowData flowData) {
		this.nodeData = nodeData;
		this.nodeIdToIndex = new HashMap<String, Integer>(nodeData.numNodes());
		for (int i = 0, numNodes = nodeData.numNodes(); i < numNodes; i++) {
			final String nodeId = nodeData.nodeId(i);
			if (nodeIdToIndex.containsKey(nodeId)) {
				throw new IllegalArgumentException("Duplicate node id " + nodeId);
			}
			nodeIdToIndex.put(nodeId, i);
		}
		this.flowData = flowData;
		this.flowStartNodeIdx = new int[flowData.numFlows()]; 
		this.flowEndNodeIdx = new int[flowData.numFlows()]; 
		for (int i = 0, numFlows = flowData.numFlows(); i < numFlows; i++) {
			String startId = flowData.getFlowStartNodeId(i);
			checkNodeExists(startId);
			String endId = flowData.getFlowEndNodeId(i);
			checkNodeExists(endId);
			
			flowStartNodeIdx[i] = nodeIdToIndex.get(startId);
			flowEndNodeIdx[i] = nodeIdToIndex.get(endId);
		}
	}

	private void checkNodeExists(String nodeId) {
		if (!nodeIdToIndex.containsKey(nodeId)) {
			throw new IllegalArgumentException("Node " + nodeId + " doesn't exist");
		}
	}
	
	@Override
	public int flowNode1(int flowIdx) {
		return flowStartNodeIdx[flowIdx];
	}

	@Override
	public int flowNode2(int flowIdx) {
		return flowEndNodeIdx[flowIdx];
	}

	@Override
	public IFlowData getFlowData() {
		return flowData;
	}

	@Override
	public INodeData getNodeData() {
		return nodeData;
	}

    public static Stats getFlowStats(INodeAndFlowData data, String attrName) {
    	IFlowData flowData = data.getFlowData();
        final int size = flowData.numFlows();

        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;

        for (int i = 0; i < size; i++) {
			final double v = DoubleAttrValue.asDouble(flowData.getAttrValue(i, attrName));
            if (v > max)
                max = v;
            if (v < min)
                min = v;
        }
        return new Stats(min, max);
    }

	@Override
	public String getNodeId(int nodeIdx) {
		return nodeData.nodeId(nodeIdx);
	}

	@Override
	public int getNodeIndex(String nodeId) {
		return nodeIdToIndex.get(nodeId);
	}
	
	@Override
	public double getOutgoingTotal(String nodeId, String attrName) {
	    final int nodeIdx = nodeIdToIndex.get(nodeId);
	    double total = 0;
	    for (int i = 0, size = flowStartNodeIdx.length; i < size; i++) {
	        if (flowStartNodeIdx[i] == nodeIdx) {
                double v = flowData.getAttrValueAsDouble(i, attrName);
                if (!Double.isNaN(v)) total += v;
	        }
	    }
	    return total;
	}
	
    @Override
	public double getIncomingTotal(String nodeId, String attrName) {
        final int nodeIdx = nodeIdToIndex.get(nodeId);
        double total = 0;
        for (int i = 0, size = flowEndNodeIdx.length; i < size; i++) {
            if (flowEndNodeIdx[i] == nodeIdx) {
                double v = flowData.getAttrValueAsDouble(i, attrName);
                if (!Double.isNaN(v)) total += v;
            }
        }
        return total;
    }

}
