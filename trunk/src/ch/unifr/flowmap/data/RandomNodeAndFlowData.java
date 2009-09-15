package ch.unifr.flowmap.data;

import java.util.Random;

/**
 * @author Ilya Boyandin
 * @deprecated
 */
@Deprecated
public class RandomNodeAndFlowData implements INodeAndFlowData {

    private final int numFlows;
    private final int numNodes;
    private final double[] size;
    private final double[] nodeX;
    private final double[] nodeY;
    private final int[] flowFrom;
    private final int[] flowTo;
    
    public RandomNodeAndFlowData(
            int numNodes, int numFlows, 
            double minx, double maxx,
            double miny, double maxy,
            double minSize, double maxSize) {
        this.numNodes = numNodes;
        this.numFlows = numFlows;
        
        size = new double[numFlows];
        
        
        Random rnd = new Random();

        nodeX = new double[numNodes]; 
        nodeY = new double[numNodes]; 
            
        for (int i = 0; i < numNodes; i++) {
            nodeX[i] = rnd.nextDouble()*(maxx - minx) + minx;
            nodeY[i] = rnd.nextDouble()*(maxy - miny) + miny;
        }

        flowFrom = new int[numFlows];
        flowTo = new int[numFlows];

        for (int i = 0; i < numFlows; i++) {
            flowFrom[i] = rnd.nextInt(numNodes);
            flowTo[i] = rnd.nextInt(numNodes);
            size[i] = rnd.nextDouble()*(maxSize - minSize) + minSize;
        }
    }
    
//    @Override
//    public double x1(int index) {
//        return nodeX[flowFrom[index]];
//    }
//    @Override
//    public double x2(int index) {
//        return nodeX[flowTo[index]];
//    }
//    @Override
//    public double y1(int index) {
//        return nodeY[flowFrom[index]];
//    }
//    @Override
//    public double y2(int index) {
//        return nodeY[flowTo[index]];
//    }

//    @Override
//    public List<Integer> getNodeIncomingFlowIndices(int nodeIndex) {
//        List<Integer> list = new ArrayList<Integer>();
//        for (int flowIdx = 0; flowIdx < flowNode2.length; flowIdx++) {
//            if (flowNode2[flowIdx] == nodeIndex) list.add(flowIdx);
//        }
//        return list;
//    }
//
//    @Override
//    public List<Integer> getNodeOutgoingFlowIndices(int nodeIndex) {
//        List<Integer> list = new ArrayList<Integer>();
//        for (int flowIdx = 0; flowIdx < flowNode1.length; flowIdx++) {
//            if (flowNode1[flowIdx] == nodeIndex) list.add(flowIdx);
//        }
//        return list;
//    }
    
    @Override
    public int flowNode1(int flowIndex) {
        return flowFrom[flowIndex];
    }

    @Override
    public int flowNode2(int flowIndex) {
        return flowTo[flowIndex];
    }

	@Override
	public IFlowData getFlowData() {
		return null;
	}

	@Override
	public INodeData getNodeData() {
		return null;
	}

	@Override
	public String getNodeId(int nodeIdx) {
		return null;
	}

	@Override
	public int getNodeIndex(String nodeId) {
		return 0;
	}

    @Override
    public double getIncomingTotal(String nodeId, String attrName) {
        return 0;
    }

    @Override
    public double getOutgoingTotal(String nodeId, String attrName) {
        return 0;
    }

	
}
