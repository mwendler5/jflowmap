package jflowmap.visuals;

import java.awt.Paint;

/**
 * @author Ilya Boyandin
 */
public class ClusterTag {

    private final int clusterId;
    private final Paint clusterPaint;
    private final boolean visible;
    
    public ClusterTag(int clusterId, Paint clusterPaint) {
        this(clusterId, clusterPaint, true);
    }

    public ClusterTag(int clusterId, Paint clusterPaint, boolean visible) {
        this.clusterId = clusterId;
        this.clusterPaint = clusterPaint;
        this.visible = visible;
    }

    public int getClusterId() {
        return clusterId;
    }

    public Paint getClusterPaint() {
        return clusterPaint;
    }
    
    public boolean isVisible() {
        return visible;
    }
    
    public ClusterTag withVisible(boolean visible) {
        if (visible == this.visible) {
            return this;
        } else {
            return new ClusterTag(clusterId, clusterPaint, visible);
        }
    }
    
    public static ClusterTag createFor(int clusterId, Paint clusterColor) {
        return new ClusterTag(clusterId, clusterColor);
        
    }

}
