package jflowmap.data._old;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ilya Boyandin
 */
public abstract class AbstractNodeData implements INodeData {

	private List<String> ids;
	private List<String> labels;
	private List<Point2D.Double> positions;

	public AbstractNodeData() {
//		if (ids.size() != labels.size()  ||  ids.size() != positions.size()) {
//			throw new IllegalArgumentException("The input arrays must have the same number of elements");
//		}
		this.ids = new ArrayList<String>();
		this.labels = new ArrayList<String>();
		this.positions = new ArrayList<Point2D.Double>();
	}

	protected void addNode(String id, String label, double x, double y) {
		ids.add(id);
		labels.add(label);
		positions.add(new Point2D.Double(x, y));
	}
	
	@Override
	public String nodeId(int nodeIdx) {
		return ids.get(nodeIdx);
	}

	@Override
	public String nodeLabel(int nodeIdx) {
		return labels.get(nodeIdx);
	}

	@Override
	public double nodeX(int nodeIdx) {
		return positions.get(nodeIdx).x;
	}

	@Override
	public double nodeY(int nodeIdx) {
		return positions.get(nodeIdx).y;
	}

	@Override
	public Point2D.Double nodePosition(int nodeIdx) {
		return positions.get(nodeIdx);
	}

	@Override
	public int numNodes() {
		return ids.size();
	}
}
