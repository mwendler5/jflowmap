package jflowmap.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.table.AbstractTableModel;

import jflowmap.visuals.VisualNode;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

/**
 * @author Ilya Boyandin
 */
class ClustersTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;
    private List<VisualNode> visualNodes;
    private Map<Integer, ClusterIcon> clusterIcons;
    
    public void setVisualNodes(List<VisualNode> nodes) {
        if (nodes == null) {
            this.visualNodes = null;
            this.clusterIcons = null;
        } else {
            this.visualNodes = Lists.newArrayList(
                Iterators.filter(
                        nodes.iterator(),
                        new Predicate<VisualNode>() {
                            public boolean apply(VisualNode node) {
                                return node.getClusterId() != VisualNode.NO_CLUSTER;
                            }
                        }
                )
            );
            Collections.sort(visualNodes, VisualNode.LABEL_COMPARATOR);
            fireTableDataChanged();
            //fireTableChanged();
            fireTableStructureChanged();
            initClusterIcons();
        }
    }
    
    private void initClusterIcons() {
        clusterIcons = new HashMap<Integer, ClusterIcon>();
        for (VisualNode node : visualNodes) {
            int clusterId = node.getClusterId();
            if (!clusterIcons.containsKey(clusterId)) {
                clusterIcons.put(clusterId, new ClusterIcon(clusterId, node.getClusterColor()));
            }
        }
    }

    public int getColumnCount() {
        return 2;
    }

    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return "Node";
            case 1:
                return "Cluster";
            default:
                return "";
        }
    }
    
    public VisualNode getVisualNode(int row) {
        return visualNodes.get(row);
    }
    
    public Class<?> getColumnClass(int column) {
        switch (column) {
            case 0:
                return String.class;
            case 1:
                return ClusterIcon.class;
            default:
                return Object.class;
        }
    }


    public int getRowCount() {
        if (visualNodes == null) return 0;
        return visualNodes.size();
    }

    public Object getValueAt(final int row, int column) {
        final VisualNode node = visualNodes.get(row);
        switch (column) {
            case 0: return node.getLabel();
//            case 1: return node.getClusterId();
            case 1: return clusterIcons.get(node.getClusterId());
        }
        return null;
    }
    
    static class ClusterIcon implements Icon, Comparable<ClusterIcon> {

        final int r = 8;
        private Color clusterColor;
        private int clusterId;

        public ClusterIcon(int clusterId, Color clusterColor) {
            this.clusterId = clusterId;
            this.clusterColor = clusterColor;
        }

        public int getIconHeight() {
            return r * 2 + 2;
        }

        public int getIconWidth() {
            return r * 2 + 2 + 32;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Rectangle b = c.getBounds();
            int ox = x + 2;
            int oy = y + 3;
            g2.setColor(Color.darkGray);
            g2.drawOval(ox, oy, r, r);
            g2.setColor(clusterColor);
            g2.fillOval(ox, oy, r, r);
            g2.setColor(Color.black);
            Font f = g2.getFont();
            g2.drawString(Integer.toString(clusterId), x + r * 2 + 2 + 2, y + (b.height + f.getSize())/2 - 1);
        }

        public int compareTo(ClusterIcon o) {
//            if (clusterId <= 0  &&  o.clusterId > 0) return +1; 
//            if (clusterId > 0  &&  o.clusterId <= 0) return -1; 
            return clusterId - o.clusterId;
        }
        
    }
    
}
