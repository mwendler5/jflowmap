package jflowmap.visuals;

import java.awt.Paint;

/**
 * @author Ilya Boyandin
 */
public class ClusterTag {

    private final int clusterId;
    private final Paint clusterPaint;
    
    public ClusterTag(int clusterId, Paint clusterPaint) {
        this.clusterId = clusterId;
        this.clusterPaint = clusterPaint;
    }

    public int getClusterId() {
        return clusterId;
    }

    public Paint getClusterPaint() {
        return clusterPaint;
    }
    
    public static ClusterTag createFor(int clusterId, Paint clusterColor) {
        return new ClusterTag(clusterId, clusterColor);
        
    }

}
