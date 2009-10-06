package ch.unifr.flowmap.bundling;

import java.awt.geom.Point2D;
import java.util.Arrays;

import org.apache.log4j.Logger;

import prefuse.data.Edge;
import prefuse.data.Graph;
import ch.unifr.flowmap.JFlowMap;
import ch.unifr.flowmap.util.Vector2D;

/**
 * @author Ilya Boyandin
 */
public class ForceDirectedEdgeBundler {

    private static Logger logger = Logger.getLogger(ForceDirectedEdgeBundler.class);
    
    private Point2D.Double[][] edgePoints;
    private double[] edgeLengths;
    private String xNodeAttr;
    private String yNodeAttr;
    private Graph graph;
    private double K = 1.0;

    private int numEdges;
    private int P;      // number of subdivision points
    private double S;   // step size
    private int I;      // number of iteration steps performed during a cycle
    
    private int cycle;

    private int numCycles;
    
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
        Point2D[][] points = new Point2D[numEdges][P + 2];
        for (int i = 0; i < numEdges; i++) {
            System.arraycopy(edgePoints[i], 0, points[i], 0, P + 1);
            points[i][P + 1] = edgePoints[i][edgePoints[i].length - 1];
        }
        return points;
    }
    
    /**
     * Number of subdivision points
     */
    public int getP() {
        return P;
    }
    
    public void bundle(int numCycles, double initialStepSize) {
        if (logger.isDebugEnabled()) {
            logger.debug("ForceDirectedEdgeBundler.bundle()");
        }
        init(numCycles, initialStepSize);
        // iterative refinement scheme
        for (int i = 0; i < numCycles; i++) {
            nextCycle();
        }
    }
    
    public void init(int numCycles, double initialStepSize) {
        if (logger.isDebugEnabled()) {
            logger.debug("ForceDirectedEdgeBundler.init()");
        }
        if (numCycles < 2) {
            throw new IllegalArgumentException("numCycles must be at least 2");
        }
        this.numCycles = numCycles; 
        numEdges = graph.getEdgeCount();
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

        P = 1;          // number of subdivision points 
        S = initialStepSize;     // step size
//        S = .04;
        I = 50;         // number of iteration steps performed during a cycle
//        I = 1;         // number of iteration steps performed during a cycle
        
        cycle = 0;
    }
    
    public boolean canPerformMoreCycles() {
        return (cycle < numCycles);
    }

    private boolean isSelfLoop(int edgeIdx) {
        return Math.abs(edgeLengths[edgeIdx]) == 0.0;
    }
    
    public void nextCycle() {
        if (logger.isDebugEnabled()) {
            logger.debug("ForceDirectedEdgeBundler.nextCycle()");
        }
        if (!canPerformMoreCycles()) {
            throw new IllegalStateException("Bundler was initialized only for " + numCycles + " cycles");
        }
        // set parameters for the next cycle
        if (cycle > 0) {
            P *= 2;
            S /= 2;
            I = (I * 2) / 3;
        }

        // add subdivision points
        for (int i = 0, numEdges = edgePoints.length; i < numEdges; i++) {
            if (isSelfLoop(i)) {
                continue;       // ignore self-loops
            }
            Point2D[] points = edgePoints[i]; 
            for (int j = P; j > 0; j--) {
                Point2D left = points[j/2];
                Point2D right;
                if (j == P) {
                    right = points[points.length - 1];
                } else {
                    right = points[j/2 + 1];
                }
                points[j] = middle(left, right); //between(left, right, 2.0/3.0);
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
            if (logger.isDebugEnabled()) {
                logger.debug("Cycle " + (cycle + 1) + " of " + numCycles + "; Step " + (step + 1) + " of " + I);
            }
            
            for (int pe = 0; pe < numEdges; pe++) {
                Point2D.Double[] p = edgePoints[pe];
                if (isSelfLoop(pe)) {
                    continue;       // ignore self-loops
                }

                final int numOfSegments = P + 1;
                double k_p = K / (edgeLengths[pe] * numOfSegments);
                
                if (Double.isInfinite(k_p)) {
                    logger.warn("Infinite value of k_p for edge " + graph.getEdge(pe));
                }
                for (int i = 1; i <= P; i++) {
                    // spring forces
                    Vector2D p_i = Vector2D.valueOf(p[i]);
                    Vector2D p_prev = Vector2D.valueOf(p[i - 1]);
                    Vector2D p_next = Vector2D.valueOf(i == P ? p[p.length - 1] : p[i + 1]);
                    Vector2D F_s_i = (
                        p_prev.minus(p_i).times(k_p) //  * p_i.distanceTo(p_prev))
                    ).plus(
                        p_next.minus(p_i).times(k_p) //  * p_i.distanceTo(p_next))
                    );

                    // attracting electrostatic forces (for each other edge)
                    Vector2D F_e_i = null;
                    for (int qe = 0; qe < numEdges; qe++) {
                        if (isSelfLoop(qe)) {
                            continue;       // ignore self-loops
                        }
                        if (qe != pe) {
                            Vector2D q_i = Vector2D.valueOf(edgePoints[qe][i]);
                            Vector2D v = q_i.minus(p_i);
                            if (!v.isZero()) {  // zero vector has no direction
                                v = v.direction().times(1 / p_i.distanceTo(q_i));
                                if (F_e_i == null) {
                                    F_e_i = v;
                                } else {
                                    F_e_i = F_e_i.plus(v);
                                }
                            }
//                          F_e_i_v += 1 / (p[i].distance(q[i]));      // using inverse-linear model (not square)
                        }
                    }

//                    if (logger.isDebugEnabled()) {
//                        logger.debug(
//                                "Edge " + graph.getEdge(pe) + ", subdiv point " + i + ": " +
//                                "F_s_i = " + F_s_i + ", " +
//                                "F_e_i = " + F_e_i
//                        );
//                    }

                    if (F_s_i.isNaN()) {
                        logger.warn("F_s_i is NaN (Edge " + graph.getEdge(pe) + ", subdiv point " + i + ")");
                    }
                    Vector2D F_p_i;
                    if (F_e_i == null) {
                        F_p_i = F_s_i;
                    } else {
                        if (F_e_i.isNaN()) {
                            logger.warn("F_e_i is NaN (Edge " + graph.getEdge(pe) + ", subdiv point " + i + ")");
                        }
                        F_p_i = F_s_i.plus(F_e_i);
                    }
                    F_p_i.times(S).movePoint(p[i]);
                }
            }
        }
        
        cycle++;
    }
    
    private Point2D middle(Point2D a, Point2D b) {
        return between(a, b, 0.5);
    }
    
    private Point2D between(Point2D a, Point2D b, double alpha) {
        return new Point2D.Double(
                Math.min(a.getX(), b.getX()) + Math.abs(b.getX() - a.getX()) * alpha,
                Math.min(a.getY(), b.getY()) + Math.abs(b.getY() - a.getY()) * alpha
        );
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
