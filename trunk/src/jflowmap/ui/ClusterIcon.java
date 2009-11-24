package jflowmap.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import javax.swing.Icon;

class ClusterIcon implements Icon, Comparable<ClusterIcon> {

        final int r = 8;
        private Paint clusterPaint;
        private int clusterId;

        public ClusterIcon(int clusterId, Paint clusterPaint) {
            this.clusterId = clusterId;
            this.clusterPaint = clusterPaint;
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
            g2.setPaint(clusterPaint);
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