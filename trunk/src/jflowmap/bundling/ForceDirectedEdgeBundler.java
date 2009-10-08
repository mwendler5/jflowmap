package jflowmap.bundling;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jflowmap.util.Vector2D;

import org.apache.log4j.Logger;

import prefuse.data.Edge;
import prefuse.data.Graph;

/**
 * @author Ilya Boyandin
 */
public class ForceDirectedEdgeBundler {

    private static Logger logger = Logger.getLogger(ForceDirectedEdgeBundler.class);
    
    private Point2D.Double[][] edgePoints;
    private double[] edgeLengths;
    private final String xNodeAttr;
    private final String yNodeAttr;
    private final Graph graph;
    private double K = 1.0;

    private int numEdges;
    private int P;      // number of subdivision points
    private double S;   // step size
    private int I;      // number of iteration steps performed during a cycle
    
    private int cycle;

    private Point2D.Double[] edgeStarts;
    private Point2D.Double[] edgeEnds;
    private double[][] edgeCompatibilityMeasures;
    
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
            points[i][0] = edgeStarts[i];
            System.arraycopy(edgePoints[i], 0, points[i], 1, P);
            points[i][P + 1] = edgeEnds[i];
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
        init(initialStepSize);
        // iterative refinement scheme
        for (int i = 0; i < numCycles; i++) {
            logger.debug("Cycle " + i + " of " + numCycles);
            nextCycle();
        }
    }
    
    public void init(double initialStepSize) {
        if (logger.isDebugEnabled()) {
            logger.debug("ForceDirectedEdgeBundler.init()");
        }
        numEdges = graph.getEdgeCount();
        edgeLengths = new double[numEdges];
        edgeStarts = new Point2D.Double[numEdges];
        edgeEnds = new Point2D.Double[numEdges];
        for (int i = 0; i < numEdges; i++) {
            Edge edge = graph.getEdge(i);
            edgeStarts[i] = new Point2D.Double(getStartX(edge), getStartY(edge));
            edgeEnds[i] = new Point2D.Double(getEndX(edge), getEndY(edge));
            edgeLengths[i] = edgeStarts[i].distance(edgeEnds[i]);
        }
        
        calcEdgeCompatibilityMeasures();

        P = 1;          // number of subdivision points
        S = initialStepSize;     // step size
//        S = .04;
//        I = 50;         // number of iteration steps performed during a cycle
        I = 50;         // number of iteration steps performed during a cycle
//        I = 1;         // number of iteration steps performed during a cycle
        
        cycle = 0;
    }
    
    private boolean isSelfLoop(int edgeIdx) {
        return Math.abs(edgeLengths[edgeIdx]) == 0.0;
    }
    
    private void calcEdgeCompatibilityMeasures() {
        edgeCompatibilityMeasures = new double[numEdges][numEdges];
        
        for (int i = 0; i < numEdges; i++) {
            Vector2D p = Vector2D.valueOf(edgeStarts[i], edgeEnds[i]);
            Point2D pm = middle(edgeStarts[i], edgeEnds[i]);
            for (int j = 0; j < i; j++) {
                Vector2D q = Vector2D.valueOf(edgeStarts[i], edgeEnds[i]);
                Point2D qm = middle(edgeStarts[j], edgeEnds[j]);
                double l_avg = (edgeLengths[i] + edgeLengths[j])/2;
                
                // angle compatibility
//                double Ca = Math.abs(Math.cos(Math.acos(p.dot(q) / (p.length() * q.length()))));
//                double Ca = Math.cos(Math.acos(p.dot(q) / (p.length() * q.length())));
                double Ca = p.dot(q) / (p.length() * q.length());
                if (Ca < 0.0) Ca = 0; 
//                double Ca = 1.0;
                
                
                // scale compatibility
//                double Cs = 2 / (l_avg * Math.min(edgeLengths[i], edgeLengths[j])  + (Math.max(edgeLengths[i], edgeLengths[j]) / l_avg));
                double Cs = Math.min(edgeLengths[i], edgeLengths[j])  / Math.max(edgeLengths[i], edgeLengths[j]);
//                double Cs = 1.0;
                
                // position compatibility
                double Cp = l_avg / (l_avg + pm.distance(qm));
//                double Cp = 1.0;
                
                // visibility compatibility
                double Cv = Math.min(visibility(p, q), visibility(q, p));

                
//                double Cdir = (p.dot(q) / (p.length() * q.length()) > 0 ? 1.0 : 0.0)
                
                edgeCompatibilityMeasures[i][j] = edgeCompatibilityMeasures[j][i] = Ca * Cs * Cp * Cv;
            }
        }
    }
    
    private double visibility(Vector2D p, Vector2D q) {
        return Math.max(
                0,
                1 //- 2 * middle(edgeStarts[i], edgeEnds[i]) / middle()
        );
    }

    public void nextCycle() {
        if (logger.isDebugEnabled()) {
            logger.debug("ForceDirectedEdgeBundler.nextCycle()");
        }
        
        final double minEdgeCompatibility = 0.6;
        int P = this.P;
        double S = this.S;
        int I = this.I;
        
        // Set parameters for the next cycle
        if (cycle > 0) {
            P *= 2;
            S /= 2;
            I = (I * 2) / 3;
        }
        
        addSubdivisionPoints(P);
        
        // Perform simulation steps
        Point2D.Double[][] tmpEdgePoints = new Point2D.Double[numEdges][P];
        
        for (int step = 0; step < I; step++) {
            if (logger.isDebugEnabled()) {
                logger.debug("Cycle " + (cycle + 1) + "; Step " + (step + 1) + " of " + I);
            }
            
            for (int pe = 0; pe < numEdges; pe++) {
                Point2D.Double[] p = edgePoints[pe];
                Point2D.Double[] newP = tmpEdgePoints[pe];
                if (isSelfLoop(pe)) {
                    continue;       // ignore self-loops
                }

                final int numOfSegments = P + 1;
                double k_p = K / (edgeLengths[pe] * numOfSegments);
                
                if (Double.isInfinite(k_p)) {
                    logger.warn("Infinite value of k_p for edge " + graph.getEdge(pe));
                }
                for (int i = 0; i < P; i++) {
                    // spring forces
                    Vector2D p_i = Vector2D.valueOf(p[i]);
                    Vector2D p_prev = Vector2D.valueOf(i == 0 ? edgeStarts[pe] : p[i - 1]);
                    Vector2D p_next = Vector2D.valueOf(i == P - 1 ? edgeEnds[pe] : p[i + 1]);
                    Vector2D F_s_i = (
                        p_prev.minus(p_i).times(k_p) //  * p_i.distanceTo(p_prev))
                    ).plus(
                        p_next.minus(p_i).times(k_p) //  * p_i.distanceTo(p_next))
                    );

                    // attracting electrostatic forces (for each other edge)
                    Vector2D F_e_i = null;
                    for (int qe = 0; qe < numEdges; qe++) {
                        if (qe != pe  &&  !isSelfLoop(qe)  &&  edgeCompatibilityMeasures[pe][qe] > minEdgeCompatibility) {
                            Vector2D q_i = Vector2D.valueOf(edgePoints[qe][i]);
                            Vector2D v = q_i.minus(p_i);
                            if (!v.isZero()) {  // zero vector has no direction
                                v = v.times(edgeCompatibilityMeasures[pe][qe] / p_i.distanceTo(q_i));
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
                    newP[i] = F_p_i.times(S).movePoint(p[i]);
                }
            }
            copy(tmpEdgePoints, edgePoints);
        }
        
        // update params only in case of success (i.e. no exception)
        this.P = P;
        this.S = S;
        this.I = I;
        
        cycle++;
    }

    private void addSubdivisionPoints(int P) {
        // bigger array for subdivision points of the next cycle
        Point2D.Double[][] newEdgePoints = new Point2D.Double[numEdges][P];

        // Add subdivision points
        for (int i = 0, numEdges = newEdgePoints.length; i < numEdges; i++) {
            if (isSelfLoop(i)) {
                continue;   // ignore self-loops
            }
            Point2D[] newPoints = newEdgePoints[i];
            if (cycle == 0) {
                assert(P == 1);
                newPoints[0] = middle(edgeStarts[i], edgeEnds[i]);
            } else {
                List<Point2D> points = new ArrayList<Point2D>(Arrays.asList(edgePoints[i]));
                points.add(0, edgeStarts[i]);
                points.add(edgeEnds[i]);
                
                final int prevP = edgePoints[i].length;
                
                double polylineLen = 0;
                double[] segmentLen = new double[prevP + 1];
                for (int j = 0; j < prevP + 1; j++) {
                    double segLen = points.get(j).distance(points.get(j + 1));
                    segmentLen[j] = segLen;
                    polylineLen += segLen;
                }
                
                double L = polylineLen / (P + 1);
                int curSegment = 0;
                double prevSegmentsLen = 0;
                Point2D p = points.get(0);
                Point2D nextP = points.get(1);
                for (int j = 0; j < P; j++) {
                    while (segmentLen[curSegment] < L * (j + 1) - prevSegmentsLen) {
                        prevSegmentsLen += segmentLen[curSegment];
                        curSegment++;
                        p = points.get(curSegment);
                        nextP = points.get(curSegment + 1);
                    }
                    double d = L * (j + 1) - prevSegmentsLen;
                    newPoints[j] = between(p, nextP, d / segmentLen[curSegment]);
                }
                
            }
        }
        edgePoints = newEdgePoints;
    }
    
    private void copy(Point2D.Double[][] src, Point2D.Double[][] dest) {
        if (src.length != dest.length) {
            throw new RuntimeException("Src and dest array sizes mismatch");
        }
        for (int i = 0; i < src.length; i++) {
            System.arraycopy(src[i], 0, dest[i], 0, src[i].length);
        }
    }

    private Point2D middle(Point2D a, Point2D b) {
        return between(a, b, 0.5);
    }
    
    /**
     * Returns a point on a segment between the two points
     * @param alpha Between 0 and 1
     */
    private Point2D between(Point2D a, Point2D b, double alpha) {
        return new Point2D.Double(
                a.getX() + (b.getX() - a.getX()) * alpha,
                a.getY() + (b.getY() - a.getY()) * alpha
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
