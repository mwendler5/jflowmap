package ch.unifr.flowmap.models;

import java.awt.geom.Point2D;

import prefuse.data.Edge;
import prefuse.data.Graph;
import ch.unifr.flowmap.util.Vector2D;

/**
 * @author Ilya Boyandin
 */
public class ForceDirectedEdgeBundler {
    
    private Point2D.Double[][] edgePoints;
    private double[] edgeLengths;
    private String xNodeAttr;
    private String yNodeAttr;
    private Graph graph;
    private double K;
    
    public ForceDirectedEdgeBundler(Graph graph, String xNodeAttr, String yNodeAttr) {
        this.graph = graph;
        this.xNodeAttr = xNodeAttr;
        this.yNodeAttr = yNodeAttr;
    }
    
    /**
     * Global spring constant (to control the amount of edge bundling)
     */
    public void setK(double k) {
        K = k;
    }
    
    public Point2D[][] getEdgePoints() {
        return edgePoints;
    }
    
    public void bundle(int numCycles) {
        final int numEdges = graph.getEdgeCount();
        int maxP = (0x01 << (numCycles - 1));   // maximum number of subdivision points 
                                                // (maximum = for the last cycle)
        edgePoints = new Point2D.Double[numEdges][maxP + 2];
        edgeLengths = new double[numEdges];
        for (int i = 0; i < numEdges; i++) {
            Edge edge = graph.getEdge(i);
            
            Point2D.Double start = new Point2D.Double(getStartX(edge), getStartY(edge));
            Point2D.Double end = new Point2D.Double(getEndX(edge), getEndY(edge));
            edgePoints[i][0] = start;
            edgePoints[i][maxP + 1] = end;
            
            edgeLengths[i] = start.distance(end);
        } 

        int P = 1;          // number of subdivision points 
        double S = .04;     // step size
        int I = 50;         // number of iteration steps performed during a cycle
        
        // iterative refinement scheme
        for (int cycle = 0; cycle < numCycles; cycle++) {
            
            // add subdivision points
            for (Point2D[] points : edgePoints) {
                for (int i = P; i > 0; i--) {
                    Point2D left = points[i - 1];
                    Point2D right;
                    if (i == P) {
                        right = points[points.length - 1];
                    } else {
                        right = points[i];
                    }
                    points[i] = middle(left, right);
                }
            }
            
            // perform simulation cycle
//            for (int step = 0; step < I; step++) {
//                
//                for (int pe = 0; pe < numEdges; pe++) {
//                    Point2D.Double[] p = edgePoints[pe];
//
//                    final int numOfSegments = P + 1;
//                    double k_p = K / (edgeLengths[pe] * numOfSegments);
//                    
//                    for (int i = 1; i <= maxP; i++) {
//                        // spring forces
//                        double F_s_i_left = k_p * (p[i].distance(p[i - 1]));
//                        double F_s_i_right = k_p * (p[i].distance(p[i + 1]));
//                        
//                        // attracting electrostatic forces (for each other edge)
//                        double F_e_i = 0; 
//                        for (int qe = 0; qe < numEdges; qe++) {
//                            if (qe != pe) {
//                                Point2D.Double[] q = edgePoints[qe];
//                                F_e_i += 1 / (p[i].distance(q[i]));      // using inverse-linear model (not square)
//                            }
//                        }
//  
//                        double F_p_i = F_s_i_left + F_s_i_right + F_e_i;
////                        p[i] += S;
//                        
//                    }
//                }
//            }
            for (int step = 0; step < I; step++) {
                
                for (int pe = 0; pe < numEdges; pe++) {
                    Point2D.Double[] p = edgePoints[pe];

                    final int numOfSegments = P + 1;
                    double k_p = K / (edgeLengths[pe] * numOfSegments);
                    
                    for (int i = 1; i <= P; i++) {
                        // spring forces
                        Vector2D p_i = Vector2D.valueOf(p[i]);
                        Vector2D p_prev = Vector2D.valueOf(p[i - 1]);
                        Vector2D p_next = Vector2D.valueOf(i == P ? p[p.length - 1] : p[i + 1]);
                        Vector2D F_s_i = (
                            p_prev.minus(p_i).times(k_p /*  * p_i.distanceTo(p_prev) */)
                        ).plus(
                            p_next.minus(p_i).times(k_p /*  * p_i.distanceTo(p_next)*/)
                        );

                        // attracting electrostatic forces (for each other edge)
                        Vector2D F_e_i = null;
                        for (int qe = 0; qe < numEdges; qe++) {
                            if (qe != pe) {
                                Point2D.Double[] q = edgePoints[qe];
                                Vector2D d = Vector2D.valueOf(q[i]).minus(p_i).direction();
                                if (F_e_i == null) {
                                    F_e_i = d;
                                } else {
                                    F_e_i = F_e_i.plus(d);
                                }
//                                F_e_i_v += 1 / (p[i].distance(q[i]));      // using inverse-linear model (not square)
                            }
                        }
  
                        Vector2D F_p_i = F_s_i.plus(F_e_i);
                        F_p_i.times(S).movePoint(p[i]);
                    }
                }
            }
            
            // set parameters for the next cycle
            P *= 2;
            S /= 2;
            I = (I * 2) / 3;
        }
    }
    
    private Point2D middle(Point2D a, Point2D b) {
        return new Point2D.Double((a.getX() + b.getX())/2, (a.getY() + b.getY())/2);
    }
    
    private double getStartX(Edge edge) {
        return edge.getSourceNode().getDouble(xNodeAttr);
    }
    
    private double getStartY(Edge edge) {
        return edge.getSourceNode().getDouble(yNodeAttr);
    }
    
    private double getEndX(Edge edge) {
        return edge.getTargetNode().getDouble(xNodeAttr);
    }

    private double getEndY(Edge edge) {
        return edge.getTargetNode().getDouble(yNodeAttr);
    }
    
}
