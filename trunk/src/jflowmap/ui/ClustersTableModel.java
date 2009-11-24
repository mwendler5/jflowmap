package jflowmap.ui;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import jflowmap.visuals.ClusterTag;
import jflowmap.visuals.VisualNodeCluster;

/**
 * @author ilya
 */
public class ClustersTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 8418700902193831848L;
    private List<VisualNodeCluster> clusters;

    public void setClusters(List<VisualNodeCluster> clusters) {
        this.clusters = clusters;
        fireTableDataChanged();
        fireTableStructureChanged();
    }

    public void clearData() {
        this.clusters = null;
        fireTableDataChanged();
        fireTableStructureChanged();
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
        case 0:
            return "Cluster";
        case 1:
            return "Nodes";
        default:
            return "";
        }
   }
    
    @Override
    public Class<?> getColumnClass(int column) {
        switch (column) {
        case 0:
            return ClusterIcon.class;
        case 1:
            return String.class;
        default:
            return Object.class;
        }
    }
    
    @Override
    public int getColumnCount() {
        return 2;
    }
    
    @Override
    public int getRowCount() {
        if (clusters == null) return 0;
        return clusters.size();
    }

    public Object getValueAt(final int row, int column) {
        VisualNodeCluster cluster = clusters.get(row);
        switch (column) {
        case 0:
            ClusterTag tag = cluster.getTag();
            return new ClusterIcon(tag.getClusterId(), tag.getClusterPaint());
        case 1:
            return cluster.getNodeListAsString();
        default:
            return null;
        }
    }

}
