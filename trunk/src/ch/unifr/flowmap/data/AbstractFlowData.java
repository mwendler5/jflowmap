package ch.unifr.flowmap.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ilya Boyandin
 */
public abstract class AbstractFlowData implements IFlowData {
	
	private int numFlows;
	private List<String> startNodeIds;
	private List<String> endNodeIds;
	private List<String> labels;
	private List<String> attrNamesList;
	private Map<AttrValueKey, IAttrValue<?>> attrValuesMap;

	public AbstractFlowData() {
		this.numFlows = 0;
		this.startNodeIds = new ArrayList<String>();
		this.endNodeIds = new ArrayList<String>();
		this.labels = new ArrayList<String>();
		this.attrNamesList = new ArrayList<String>();
		this.attrValuesMap = new HashMap<AttrValueKey, IAttrValue<?>>();
	}

	protected void addFlow(
			String startNodeId, String endNodeId, String label,
			String[] attrNames, String[] attrValues) {
		if (attrNames.length != attrValues.length) {
			throw new IllegalArgumentException();
		}
		if (startNodeIds.size() != numFlows  ||  
			endNodeIds.size() != numFlows  ||  
			labels.size() != numFlows) {
			throw new IllegalStateException();
		}
		startNodeIds.add(startNodeId);
		endNodeIds.add(endNodeId);
		labels.add(label);
		
		final int flowIdx = numFlows++;

		for (int i = 0, size = attrNames.length; i < size; i++) {
			if (!attrNamesList.contains(attrNames[i])) {
				attrNamesList.add(attrNames[i]);
			}
			this.attrValuesMap.put(
					createAttrValueKey(flowIdx, attrNames[i]),
					NodeAttrValues.parseValue(attrValues[i]));
		}
	}
	
	@Override
	public int numFlows() {
		return numFlows;
	}

	@Override
	public String getFlowLabel(int flowIdx) {
		return labels.get(flowIdx);
	}

	@Override
	public String getFlowStartNodeId(int flowIdx) {
		return startNodeIds.get(flowIdx);
	}

	@Override
	public String getFlowEndNodeId(int flowIdx) {
		return endNodeIds.get(flowIdx);
	}

	@Override
	public int numAttrs() {
		return attrNamesList.size();
	}

	@Override
	public String getAttrName(int attrIdx) {
		return attrNamesList.get(attrIdx);
	}
	
	@Override
	public IAttrValue<?> getAttrValue(int flowIdx, String attrName) {
		return attrValuesMap.get(createAttrValueKey(flowIdx, attrName));
	}

	private static final AttrValueKey createAttrValueKey(int flowIdx, String attrName) {
		return new AttrValueKey(flowIdx, attrName);
	}
	
	private static class AttrValueKey {
		private final int flowIdx; 
		private final String attrName;
		public AttrValueKey(int flowIdx, String attrName) {
			this.flowIdx = flowIdx;
			this.attrName = attrName;
		}
		public int getFlowIdx() {
			return flowIdx;
		}
		public String getAttrName() {
			return attrName;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((attrName == null) ? 0 : attrName.hashCode());
			result = prime * result + flowIdx;
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			AttrValueKey other = (AttrValueKey) obj;
			if (attrName == null) {
				if (other.attrName != null)
					return false;
			} else if (!attrName.equals(other.attrName))
				return false;
			if (flowIdx != other.flowIdx)
				return false;
			return true;
		}
	}

	@Override
	public double getAttrValueAsDouble(int flowIdx, String attrName) {
		return DoubleAttrValue.asDouble(getAttrValue(flowIdx, attrName));
	}
}