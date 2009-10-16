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
    private String valueEdgeAttr;
    private final Graph graph;

    private Point2D.Double[] edgeStarts;
    private Point2D.Double[] edgeEnds;
    private double[] edgeValues;
    private byte[][] edgeCompatibilityMeasures; // use byte to save memory
    private int numEdges;
    private int cycle;

    private int P;      // number of subdivision points (will double with every cycle)
    private double S;   // step size
    private int I;      // number of iteration steps performed during a cycle
    
    private ForceDirectedBundlerParameters params;
    
    private ProgressTracker progressTracker;

    
    public ForceDirectedEdgeBundler(
            Graph graph, 
            String xNodeAttr, String yNodeAttr, String valueEdgeAttr,
            ForceDirectedBundlerParameters params) {
        this.graph = graph;
        this.xNodeAttr = xNodeAttr;
        this.yNodeAttr = yNodeAttr;
        this.valueEdgeAttr = valueEdgeAttr;
        this.params = params;
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
        if (params.getEdgeValueAffectsAttraction()) {
            edgeValues = new double[numEdges];
        }
        for (int i = 0; i < numEdges; i++) {
            Edge edge = graph.getEdge(i);
            edgeStarts[i] = new Point2D.Double(getStartX(edge), getStartY(edge));
            edgeEnds[i] = new Point2D.Double(getEndX(edge), getEndY(edge));
            edgeLengths[i] = edgeStarts[i].distance(edgeEnds[i]);
            if (params.getEdgeValueAffectsAttraction()) {
                edgeValues[i] = getValue(edge);
            }
        }

        this.I = params.getI();
        this.P = params.getP();
        this.S = params.getS();
        
        if (params.getPrecalculateCompatibilityMeasures()) {
            calcEdgeCompatibilityMeasures();
        }
        
        cycle = 0;
    }
    
    private void calcEdgeCompatibilityMeasures() {
        progressTracker.startSubtask("Allocating memory", .05);
        edgeCompatibilityMeasures = new byte[numEdges][];
        progressTracker.subtaskCompleted();
        
        logger.info("Calculating compatibility measures");
        logger.info("Using " + (params.getUseSimpleCompatibilityMeasure() ? "simple" : "standard") + " compatibility measure");
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
            edgeCompatibilityMeasures[i] = new byte[i];
            for (int j = 0; j < i; j++) {
                if (progressTracker.isCancelled()) {
                    edgeCompatibilityMeasures = null;
                    return;
                }
                
                double C = calcEdgeCompatibility(i, j);
                if (Math.abs(C) >= params.getEdgeCompatibilityThreshold()) {
                    numCompatible++;
                }
                Csum += Math.abs(C);
                Ccnt++;
                
                // C is between -1.0 and 1.0, so we can multiply it by 100 and store in byte
                // to save memory. This way we lose precision, but is not that important.
                edgeCompatibilityMeasures[i][j] = (byte)Math.round(C * 100);
            }
            progressTracker.incSubtaskProgress();
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Average edge compatibility = " + (Csum / Ccnt));
            logger.debug("Compatibility ratio = " + Math.round((numCompatible * 100.0 / Ccnt) * 100)/100.0 + "%");
        }
    }

    private double calcEdgeCompatibility(int i, int j) {
        double C;
        if (params.getUseSimpleCompatibilityMeasure()) {
            C = calcSimpleEdgeCompatibility(i, j);
        } else {
            C = calcStandardEdgeCompatibility(i, j);
        }
        assert(C >= 0  &&  C <= 1.0);
        if (params.getBinaryCompatibility()) {
            if (C >= params.getEdgeCompatibilityThreshold()) {
                C = 1.0;
            } else {
                C = 0.0;
            }
        }
        if (params.getUseRepulsionForOppositeEdges()) {
            Vector2D p = Vector2D.valueOf(edgeStarts[i], edgeEnds[i]);
            Vector2D q = Vector2D.valueOf(edgeStarts[j], edgeEnds[j]);
            double cos = p.dot(q) / (p.length() * q.length());
            if (cos < 0) {
                C = -C;
            }
        }
        return C;
    }
    
    private double calcSimpleEdgeCompatibility(int i, int j) {
        if (isSelfLoop(i)  ||  isSelfLoop(j)) {
            return 0.0;
        }

        double l_avg = (edgeLengths[i] + edgeLengths[j])/2;
        return l_avg / (l_avg + 
                edgeStarts[i].distance(edgeStarts[j]) + 
                edgeEnds[i].distance(edgeEnds[j]));
    }

    private double calcStandardEdgeCompatibility(int i, int j) {
        if (isSelfLoop(i)  ||  isSelfLoop(j)) {
            return 0.0;
        }
        
        Vector2D p = Vector2D.valueOf(edgeStarts[i], edgeEnds[i]);
        Vector2D q = Vector2D.valueOf(edgeStarts[j], edgeEnds[j]);
        Point2D pm = GeomUtils.midpoint(edgeStarts[i], edgeEnds[i]);
        Point2D qm = GeomUtils.midpoint(edgeStarts[j], edgeEnds[j]);
        double l_avg = (edgeLengths[i] + edgeLengths[j])/2;
        
        // angle compatibility
        double Ca;
        if (params.getDirectionAffectsCompatibility()) {
            Ca = (p.dot(q) / (p.length() * q.length()) + 1.0) / 2.0;
        } else {
            Ca = Math.abs(p.dot(q) / (p.length() * q.length()));
        }
        if (Math.abs(Ca) < EPS) { Ca = 0.0; }       // this led to errors (when Ca == -1e-12)
        if (Math.abs(Math.abs(Ca) - 1.0) < EPS) { Ca = 1.0; }
        
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
    
    private double getEdgeCompatibility(int i, int j) {
        if (i == j) return 0;
        if (params.getPrecalculateCompatibilityMeasures()) {
            byte C;
            if (i > j)
                C = edgeCompatibilityMeasures[i][j];
            else
                C = edgeCompatibilityMeasures[j][i];
            return (double)C / 100.0;
        } else {
            return calcEdgeCompatibility(i, j);
        }
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
            S *= (1.0 - params.getStepDampingFactor());
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
                double k_p = params.getK() / (edgeLengths[pe] * numOfSegments);
                
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
                            double C = getEdgeCompatibility(pe, qe);
                            if (Math.abs(C) > params.getEdgeCompatibilityThreshold()) {
                                Vector2D q_i = Vector2D.valueOf(edgePoints[qe][i]);
                                Vector2D v = q_i.minus(p_i);
                                if (!v.isZero()) {  // zero vector has no direction
                                    double d = v.length();  // shouldn't be zero
                                    double m;
                                    if (params.getUseInverseQuadraticModel()) {
                                        m = (C / d) / (d * d);
                                    } else {
                                        m = (C / d) / d;
                                    }
                                    if (params.getEdgeValueAffectsAttraction()) {
                                        m *= 1.0 + (edgeValues[qe] - edgeValues[pe])/(edgeValues[qe] + edgeValues[pe]);
                                    }
                                    if (Math.abs(m) < 1.0) {
                                        v = v.times(m);
                                    } else {
                                        v = v.times(Math.signum(m));
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
                        throw new AssertionError("F_s_i is NaN");
                    }
                    if (F_e_i.isNaN()) {
                        throw new AssertionError("F_e_i is NaN");
                    }
                    if (Double.isInfinite(newP[i].x)  &&  Double.isInfinite(newP[i].y)) {
                        throw new AssertionError("Point moved to infinity");
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
    
    private double getValue(Edge edge) {
        return edge.getDouble(valueEdgeAttr);
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
