package jflowmap.bundling;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jflowmap.util.GeomUtils;
import jflowmap.util.Vector2D;

import org.apache.log4j.Logger;

import prefuse.data.Edge;
import prefuse.data.Graph;
import at.fhj.utils.misc.ProgressTracker;

/**
 * This is an implementation of the algorithm described in the paper
 * "Force-Directed Edge Bundling for Graph Visualization" by
 * Danny Holten and Jarke J. van Wijk.
 * 
 * @author Ilya Boyandin
 */
public class ForceDirectedEdgeBundler {

    private static final double EPS = 1e-7;

    private static Logger logger = Logger.getLogger(ForceDirectedEdgeBundler.class);
    
    private Point2D.Double[][] edgePoints;
    private double[] edgeLengths;
    private final String xNodeAttr;
    private final String yNodeAttr;
    private final Graph graph;

    private Point2D.Double[] edgeStarts;
    private Point2D.Double[] edgeEnds;
    private double[][] edgeCompatibilityMeasures;
    private int numEdges;
    private int cycle;

    private double K = 0.1; // global spring constant (used to control the amount of edge bundling by
                            // determining the stiffness of the edges)
    private int P = 1;      // initial number of subdivision points (will double with every cycle)
    private double S = 0.4;   // step size - shouldn't be higher than 1.0
    private int I = 50;      // number of iteration steps performed during a cycle
    private double edgeCompatibilityThreshold = 0.60;
    private boolean directionAffectsCompatibility;
    private boolean binaryCompatibility;
    private boolean useInverseQuadraticModel;
    private boolean useRepulsionForOppositeEdges; // useRepulsionForCompatibleEdgesOfOppositeDirections
    private boolean useSimpleCompatibilityMeasure;
    private double stepDampingFactor = 0.5;
    
    private ProgressTracker progressTracker;


//    public static class Parameters {
//        
//    }
    
    public ForceDirectedEdgeBundler(Graph graph, String xNodeAttr, String yNodeAttr,
                                    int I, double K, double edgeCompatibilityThreshold,
                                    double S, double stepDampingFactor,
                                    boolean directionAffectsCompatibility, boolean binaryCompatibility,
                                    boolean useInverseQuadraticModel, boolean useRepulsionForOppositeEdges,
                                    boolean useSimpleCompatibilityMeasure) {
        this.graph = graph;
        this.xNodeAttr = xNodeAttr;
        this.yNodeAttr = yNodeAttr;
        this.I = I;
        this.K = K;
        this.edgeCompatibilityThreshold = edgeCompatibilityThreshold;
        this.S = S;
        this.stepDampingFactor = stepDampingFactor;
        this.binaryCompatibility = binaryCompatibility;
        this.useInverseQuadraticModel = useInverseQuadraticModel;
        this.useRepulsionForOppositeEdges = useRepulsionForOppositeEdges;
        if (useRepulsionForOppositeEdges) {
            directionAffectsCompatibility = false;
        }
        this.directionAffectsCompatibility = directionAffectsCompatibility;
        this.useSimpleCompatibilityMeasure = useSimpleCompatibilityMeasure;
    }

    public ProgressTracker getProgressTracker() {
        return progressTracker;
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
    
    public void bundle(ProgressTracker progressTracker, int numCycles) {
        if (logger.isDebugEnabled()) {
            logger.debug("ForceDirectedEdgeBundler.bundle()");
        }
        init(progressTracker);
        // iterative refinement scheme
        for (int i = 0; i < numCycles; i++) {
            logger.debug("Cycle " + i + " of " + numCycles);
            nextCycle();
        }
    }

    public void init(ProgressTracker progressTracker) {
        if (logger.isDebugEnabled()) {
            logger.debug("ForceDirectedEdgeBundler.init()");
        }
        this.progressTracker = progressTracker;
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
        
        cycle = 0;
    }
    
    private void calcEdgeCompatibilityMeasures() {
        progressTracker.startSubtask("Allocating memory", .05);
        edgeCompatibilityMeasures = new double[numEdges][];
        progressTracker.subtaskCompleted();
        
        logger.info("Calculating compatibility measures");
        logger.info("Using " + (useSimpleCompatibilityMeasure ? "simple" : "standard") + " compatibility measure");
        progressTracker.startSubtask("Precalculating edge compatibility measures", .95);
        progressTracker.setSubtaskIncUnit(100.0 / numEdges);
        int Ccnt = 0;
        int numCompatible = 0;
        double Csum = 0;
        for (int i = 0; i < numEdges; i++) {
            if (progressTracker.isCancelled()) {
                edgeCompatibilityMeasures = null;
                return;
            }
            edgeCompatibilityMeasures[i] = new double[i];
            for (int j = 0; j < i; j++) {
                if (progressTracker.isCancelled()) {
                    edgeCompatibilityMeasures = null;
                    return;
                }
                double C;
                if (useSimpleCompatibilityMeasure) {
                    C = calcSimpleEdgeCompatibility(i, j);
                } else {
                    C = calcEdgeCompatibility(i, j);
                }
                assert(C >= 0  &&  C <= 1.0);
                if (C >= edgeCompatibilityThreshold) {
                    numCompatible++;
                }
                if (binaryCompatibility) {
                    if (C >= edgeCompatibilityThreshold) {
                        C = 1.0;
                    } else {
                        C = 0.0;
                    }
                }
                Csum += C;
                Ccnt++;
                if (useRepulsionForOppositeEdges) {
                    Vector2D p = Vector2D.valueOf(edgeStarts[i], edgeEnds[i]);
                    Vector2D q = Vector2D.valueOf(edgeStarts[j], edgeEnds[j]);
                    double cos = p.dot(q) / (p.length() * q.length());
                    if (cos < 0) {
                        C = -C;
                    }
                }
                edgeCompatibilityMeasures[i][j] = C;
            }
            progressTracker.incSubtaskProgress();
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Average edge compatibility = " + (Csum / Ccnt));
            logger.debug("Compatibility ratio = " + Math.round((numCompatible * 100.0 / Ccnt) * 100)/100.0 + "%");
        }
    }

    private double calcSimpleEdgeCompatibility(int i, int j) {
        double l_avg = (edgeLengths[i] + edgeLengths[j])/2;
        return l_avg / (l_avg + 
                edgeStarts[i].distance(edgeStarts[j]) + 
                edgeEnds[i].distance(edgeEnds[j]));
    }

    private double calcEdgeCompatibility(int i, int j) {
        Vector2D p = Vector2D.valueOf(edgeStarts[i], edgeEnds[i]);
        Vector2D q = Vector2D.valueOf(edgeStarts[j], edgeEnds[j]);
        Point2D pm = GeomUtils.midpoint(edgeStarts[i], edgeEnds[i]);
        Point2D qm = GeomUtils.midpoint(edgeStarts[j], edgeEnds[j]);
        double l_avg = (edgeLengths[i] + edgeLengths[j])/2;
        
        // angle compatibility
        double Ca;
        if (directionAffectsCompatibility) {
            Ca = (p.dot(q) / (p.length() * q.length()) + 1.0) / 2.0;
        } else {
            Ca = Math.abs(p.dot(q) / (p.length() * q.length()));
        }
        if (Math.abs(Ca) < EPS) {
            Ca = 0.0;
        }
        
        // scale compatibility
        double Cs = 2 / (
                (l_avg / Math.min(edgeLengths[i], edgeLengths[j]))  + 
                (Math.max(edgeLengths[i], edgeLengths[j]) / l_avg)
        );
        
        // position compatibility
        double Cp = l_avg / (l_avg + pm.distance(qm));
        
        // visibility compatibility
        double Cv;
        if (Ca * Cs * Cp > .9) {
            // this compatibility measure is only applied if the edges are 
            // (almost) parallel, equal in length and close together
            Cv = Math.min(
                    visibilityCompatibility(edgeStarts[i], edgeEnds[i], edgeStarts[j], edgeEnds[j]), 
                    visibilityCompatibility(edgeStarts[j], edgeEnds[j], edgeStarts[i], edgeEnds[i]) 
            );
        } else {
            Cv = 1.0;
        }
        
        assert(Ca >= 0  &&  Ca <= 1);
        assert(Cs >= 0  &&  Cs <= 1);
        assert(Cp >= 0  &&  Cp <= 1);
        assert(Cv >= 0  &&  Cv <= 1);

        return Ca * Cs * Cp * Cv;
    }
    
    private static double visibilityCompatibility(Point2D p0, Point2D p1, Point2D q0, Point2D q1) {
        Point2D i0 = GeomUtils.projectPointToLine(p0, p1, q0);
        Point2D i1 = GeomUtils.projectPointToLine(p0, p1, q1);
        Point2D im = GeomUtils.midpoint(i0, i1);
        Point2D pm = GeomUtils.midpoint(p0, p1);
        return Math.max(
                0,
                1 - 2 * pm.distance(im) / i0.distance(i1)
        );
    }
    
    private double getEdgeCompatibility(int edgeI, int edgeJ) {
        if (edgeI == edgeJ) return 0;
        if (edgeI > edgeJ)
            return edgeCompatibilityMeasures[edgeI][edgeJ];
        else
            return edgeCompatibilityMeasures[edgeJ][edgeI];
    }
    
    private boolean isSelfLoop(int edgeIdx) {
        return Math.abs(edgeLengths[edgeIdx]) == 0.0;
    }

    public void nextCycle() {
        if (logger.isDebugEnabled()) {
            logger.debug("ForceDirectedEdgeBundler.nextCycle()");
        }
        
        int P = this.P;
        double S = this.S;
        int I = this.I;
        
        // Set parameters for the next cycle
        if (cycle > 0) {
            P *= 2;
            S *= (1.0 - stepDampingFactor);
//            S /= 1.2;
            I = (I * 2) / 3;
        }
        
        if (progressTracker.isCancelled()) {
            return;
        }
        progressTracker.startSubtask("Adding subdivision points", .1);
        addSubdivisionPoints(P);
        progressTracker.subtaskCompleted();
        
        // Perform simulation steps
        Point2D.Double[][] tmpEdgePoints = new Point2D.Double[numEdges][P];
        
        for (int step = 0; step < I; step++) {
            if (progressTracker.isCancelled()) {
                return;
            }
            progressTracker.startSubtask("Step " + (step + 1) + " of " + I, (1.0 - .1) / I);
            progressTracker.setSubtaskIncUnit(100.0 / numEdges);
            if (logger.isDebugEnabled()) {
                logger.debug("Cycle " + (cycle + 1) + "; Step " + (step + 1) + " of " + I);
            }
            for (int pe = 0; pe < numEdges; pe++) {
                if (progressTracker.isCancelled()) {
                    return;
                }
                Point2D.Double[] p = edgePoints[pe];
                Point2D.Double[] newP = tmpEdgePoints[pe];
                if (isSelfLoop(pe)) {
                    continue;       // ignore self-loops
                }

                final int numOfSegments = P + 1;
                double k_p = K / (edgeLengths[pe] * numOfSegments);
                
                for (int i = 0; i < P; i++) {
                    // spring forces
                    Vector2D p_i = Vector2D.valueOf(p[i]);
                    Vector2D p_prev = Vector2D.valueOf(i == 0 ? edgeStarts[pe] : p[i - 1]);
                    Vector2D p_next = Vector2D.valueOf(i == P - 1 ? edgeEnds[pe] : p[i + 1]);
                    Vector2D F_s_i = (
                        p_prev.minus(p_i) //.times(k_p)  * p_i.distanceTo(p_prev))
                    ).plus(
                        p_next.minus(p_i) //.times(k_p)  * p_i.distanceTo(p_next))
                    );
                    
                    if (Math.abs(k_p) < 1.0) {
                        F_s_i = F_s_i.times(k_p);
                    }

                    // attracting electrostatic forces (for each other edge)
                    Vector2D F_e_i = Vector2D.ZERO;
                    for (int qe = 0; qe < numEdges; qe++) {
                        if (qe != pe  &&  !isSelfLoop(qe)) {
                            double ec = getEdgeCompatibility(pe, qe);
                            if (Math.abs(ec) > edgeCompatibilityThreshold) {
                                Vector2D q_i = Vector2D.valueOf(edgePoints[qe][i]);
                                Vector2D v = q_i.minus(p_i);
                                if (!v.isZero()) {  // zero vector has no direction
                                    double d = v.length();  // shouldn't be zero
                                    double m;
                                    if (useInverseQuadraticModel) {
                                        m = ec / (d * d * d);
                                    } else {
                                        m = ec / (d * d);
                                    }
                                    if (Math.abs(m) < 1.0) {
                                        v = v.times(m);
                                    }
//                                    if (v.isNaN()) {
//                                        // can happen if d is Infinity and ec is zero
//                                        logger.warn("v is NaN");
//                                    }
                                    F_e_i = F_e_i.plus(v);
                                }
//                              F_e_i_v += 1 / (p[i].distance(q[i]));      // using inverse-linear model (not square)
                            }
                        }
                    }

                    if (Math.abs(F_s_i.length()) < EPS  &&  Math.abs(F_e_i.length()) < EPS) {
                        newP[i] = p[i];
                        continue;
                    }
                    Vector2D F_p_i = F_s_i.plus(F_e_i);
                    newP[i] = F_p_i.times(S).movePoint(p[i]);

                    if (F_s_i.isNaN()) {
                        throw new RuntimeException("F_s_i is NaN");
                    }
                    if (F_e_i.isNaN()) {
                        throw new RuntimeException("F_e_i is NaN");
                    }
                    if (Double.isInfinite(newP[i].x)  &&  Double.isInfinite(newP[i].y)) {
                        throw new RuntimeException("Point moved to infinity");
                    }
//                    if (Math.abs(F_s_i.length() / F_e_i.length()) > 1e5  ||  Math.abs(F_e_i.length() / F_s_i.length()) > 1e5) {
//                        logger.warn("F_s_i and F_e_i differ in more than five orders of magnitude: F_s_i = " + F_s_i.length() + ", F_e_i = " + F_e_i.length());
//                    }
//                    if (F_s_i.length() > 1e10) {
//                        logger.warn("Extremely high F_s_i value: F_s_i = " + F_s_i.length());
//                    }
//                    if (F_e_i.length() > 1e10) {
//                        logger.warn("Extremely high F_e_i value: F_e_i = " + F_e_i.length());
//                    }
                }
                progressTracker.incSubtaskProgress();
            }
            copy(tmpEdgePoints, edgePoints);
            progressTracker.subtaskCompleted();
        }
        
        if (!progressTracker.isCancelled()) {
            // update params only in case of success (i.e. no exception)
            this.P = P;
            this.S = S;
            this.I = I;
            
            cycle++;
        }
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
                newPoints[0] = GeomUtils.midpoint(edgeStarts[i], edgeEnds[i]);
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
                    newPoints[j] = GeomUtils.between(p, nextP, d / segmentLen[curSegment]);
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
