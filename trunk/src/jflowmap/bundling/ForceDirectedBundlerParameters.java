package jflowmap.bundling;

/**
 * @author Ilya Boyandin
 */
public class ForceDirectedBundlerParameters {
    private int numCycles;
    private int P;        // initial number of subdivision points
    private double S;   // step size - shouldn't be higher than 1.0
    private int I;       // number of iteration steps performed during a cycle
    private double K; // global spring constant (used to control the amount of edge bundling by
                            // determining the stiffness of the edges)
    private double stepDampingFactor;
    private double edgeCompatibilityThreshold;
    private boolean directionAffectsCompatibility;
    private boolean binaryCompatibility;
    private boolean useInverseQuadraticModel;
    private boolean useRepulsionForOppositeEdges; // for compatible edges going into opposite directions
    private boolean useSimpleCompatibilityMeasure;
    private boolean edgeValueAffectsAttraction;
    
    public ForceDirectedBundlerParameters() {
        resetToDefaults();
    }
    
    public void resetToDefaults() {
        numCycles = 6;
        P = 1;
        S = 0.4;
        I = 50;
        K = 0.1;
        stepDampingFactor = 0.5;
        edgeCompatibilityThreshold = 0.60;
        directionAffectsCompatibility = true;
        binaryCompatibility = false;
        useInverseQuadraticModel = false;
        useRepulsionForOppositeEdges = false;
        useSimpleCompatibilityMeasure = false;
        edgeValueAffectsAttraction = true;
    }
    
    private void ensureDirectionAffectsCompatibilityValueIsValid() {
        if (useSimpleCompatibilityMeasure  ||  useRepulsionForOppositeEdges) {
            directionAffectsCompatibility = false;
        }
    }
    
    public int getNumCycles() {
        return numCycles;
    }
    
    public void setNumCycles(int numCycles) {
        this.numCycles = numCycles;
    }
    
    public int getP() {
        return P;
    }

    public void setP(int p) {
        P = p;
    }

    public double getS() {
        return S;
    }

    public void setS(double s) {
        S = s;
    }

    public int getI() {
        return I;
    }

    public void setI(int i) {
        I = i;
    }

    public double getK() {
        return K;
    }

    public void setK(double k) {
        K = k;
    }

    public double getEdgeCompatibilityThreshold() {
        return edgeCompatibilityThreshold;
    }

    public void setEdgeCompatibilityThreshold(double edgeCompatibilityThreshold) {
        this.edgeCompatibilityThreshold = edgeCompatibilityThreshold;
    }

    public boolean getDirectionAffectsCompatibility() {
        return directionAffectsCompatibility;
    }

    public void setDirectionAffectsCompatibility(boolean directionAffectsCompatibility) {
        this.directionAffectsCompatibility = directionAffectsCompatibility;
    }

    public boolean getBinaryCompatibility() {
        return binaryCompatibility;
    }

    public void setBinaryCompatibility(boolean binaryCompatibility) {
        this.binaryCompatibility = binaryCompatibility;
    }

    public boolean getUseInverseQuadraticModel() {
        return useInverseQuadraticModel;
    }

    public void setUseInverseQuadraticModel(boolean useInverseQuadraticModel) {
        this.useInverseQuadraticModel = useInverseQuadraticModel;
    }

    public boolean getUseRepulsionForOppositeEdges() {
        return useRepulsionForOppositeEdges;
    }

    public void setUseRepulsionForOppositeEdges(boolean useRepulsionForOppositeEdges) {
        this.useRepulsionForOppositeEdges = useRepulsionForOppositeEdges;
        ensureDirectionAffectsCompatibilityValueIsValid();
    }

    public boolean getUseSimpleCompatibilityMeasure() {
        return useSimpleCompatibilityMeasure;
    }

    public void setUseSimpleCompatibilityMeasure(
            boolean useSimpleCompatibilityMeasure) {
        this.useSimpleCompatibilityMeasure = useSimpleCompatibilityMeasure;
        ensureDirectionAffectsCompatibilityValueIsValid();
    }

    public double getStepDampingFactor() {
        return stepDampingFactor;
    }

    public void setStepDampingFactor(double stepDampingFactor) {
        this.stepDampingFactor = stepDampingFactor;
    }

    public boolean getEdgeValueAffectsAttraction() {
        return edgeValueAffectsAttraction;
    }

    public void setEdgeValueAffectsAttraction(boolean edgeValueAffectsAttraction) {
        this.edgeValueAffectsAttraction = edgeValueAffectsAttraction;
    }

    
}