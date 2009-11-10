package jflowmap.visuals;

import java.awt.Color;

/**
 * @author Ilya Boyandin
 */
public class ClusterTag {

    private final int clusterId;
    private final Color clusterColor;
    
    public ClusterTag(int clusterId, Color clusterColor) {
        this.clusterId = clusterId;
        this.clusterColor = clusterColor;
    }

    public int getClusterId() {
        return clusterId;
    }

    public Color getClusterColor() {
        return clusterColor;
    }
    
    public static ClusterTag createFor(int clusterId, Color clusterColor) {
        return new ClusterTag(clusterId, clusterColor);
        
    }

}
