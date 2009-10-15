package jflowmap.bundling;

/**
 * @author Ilya Boyandin
 */
public class ForceDirectedBundlerParameters {
    private int P = 1;        // initial number of subdivision points
    private double S = 0.4;   // step size - shouldn't be higher than 1.0
    private int I = 50;       // number of iteration steps performed during a cycle
    private double K = 0.1; // global spring constant (used to control the amount of edge bundling by
                            // determining the stiffness of the edges)
    private double stepDampingFactor = 0.5;
    private double edgeCompatibilityThreshold = 0.60;
    private boolean directionAffectsCompatibility = true;
    private boolean binaryCompatibility = false;
    private boolean useInverseQuadraticModel = false;
    private boolean useRepulsionForOppositeEdges = false; // for compatible edges going into opposite directions
    private boolean useSimpleCompatibilityMeasure = false;
    
    public ForceDirectedBundlerParameters() {
    }
    
    private void ensureDirectionAffectsCompatibilityValueIsValid() {
        if (useSimpleCompatibilityMeasure  ||  useRepulsionForOppositeEdges) {
            directionAffectsCompatibility = false;
        }
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
}