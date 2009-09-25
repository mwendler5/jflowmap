package ch.unifr.flowmap.models;

import prefuse.data.Graph;
import prefuse.data.io.GraphMLReader;
import prefuse.data.io.DataIOException;
import ch.unifr.flowmap.data.Stats;
import ch.unifr.flowmap.visuals.GraphStats;
import ch.unifr.flowmap.FlowMap;

import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;


public class FlowMapModel {

    private boolean autoAdjustColorScale;
    private boolean useLogColorScale;
    private boolean useLogWidthScale;

    private Graph graph;

    private String valueEdgeAttr = "value";
    private String xNodeAttr = "x";
    private String yNodeAttr = "y";
    private String labelAttr = "tooltip";

    private int edgeAlpha = 40;
    private int edgeMarkerAlpha = 200;

    private double valueFilterMin = Double.MIN_VALUE;
    private double valueFilterMax = Double.MAX_VALUE;

    private double edgeLengthFilterMin = Double.MIN_VALUE;
    private double edgeLengthFilterMax = Double.MAX_VALUE;

    private boolean autoAdjustEdgeColorScale;
    private double maxEdgeWidth = 10.0;

    private final GraphStats graphStats;

    private PropertyChangeSupport changes = new PropertyChangeSupport(this);

    public FlowMapModel(Graph graph, String valueEdgeAttrName, String labelAttrName) {
        this.graph = graph;

        this.valueEdgeAttr = valueEdgeAttrName;
        this.labelAttr = labelAttrName;

        this.graphStats = new GraphStats(graph, valueEdgeAttrName, xNodeAttr, yNodeAttr);

        Stats minMax = graphStats.getValueEdgeAttrStats();
        this.valueFilterMin = minMax.min;
        this.valueFilterMax = minMax.max;

        Stats lengthStats = graphStats.getEdgeLengthStats();
        this.edgeLengthFilterMin = lengthStats.min;
        this.edgeLengthFilterMax = lengthStats.max;

//        Stats xStats = graphStats.getNodeAttrStats(xNodeAttr);
//        Stats yStats = graphStats.getNodeAttrStats(yNodeAttr);
    }

    public Graph getGraph() {
        return graph;
    }

    public boolean getAutoAdjustColorScale() {
        return autoAdjustColorScale;
    }

    public void setAutoAdjustColorScale(final boolean autoAdjustColorScale) {
        boolean old = this.autoAdjustColorScale;
        this.autoAdjustColorScale = autoAdjustColorScale;
        changes.firePropertyChange(PROPERTY_AUTO_ADJUST_COLOR_SCALE, old, autoAdjustColorScale);
    }

    public boolean isUseLogColorScale() {
        return useLogColorScale;
    }

    public void setUseLogColorScale(final boolean useLogColorScale) {
        boolean old = this.useLogColorScale;
        this.useLogColorScale = useLogColorScale;
        changes.firePropertyChange(PROPERTY_USE_LOG_COLOR_SCALE, old, useLogColorScale);
    }

    public boolean isUseLogWidthScale() {
        return useLogWidthScale;
    }

    public void setUseLogWidthScale(final boolean useLogWidthScale) {
        boolean old = useLogWidthScale;
        this.useLogWidthScale = useLogWidthScale;
        changes.firePropertyChange(PROPERTY_USE_LOG_WIDTH_SCALE, old, useLogWidthScale);
    }

    public String getXNodeAttr() {
        return xNodeAttr;
    }

    public String getYNodeAttr() {
        return yNodeAttr;
    }

    public Stats getNodeAttrStats(String attrName) {
        return graphStats.getNodeAttrStats(attrName);
    }

    public String getValueEdgeAttr() {
        return valueEdgeAttr;
    }

    public String getLabelAttr() {
        return labelAttr;
    }

    public int getEdgeAlpha() {
        return edgeAlpha;
    }

    public int getEdgeMarkerAlpha() {
        return edgeMarkerAlpha;
    }

    public double getValueFilterMin() {
        return valueFilterMin;
    }

    public double getValueFilterMax() {
        return valueFilterMax;
    }

    public double getEdgeLengthFilterMin() {
        return edgeLengthFilterMin;
    }

    public double getEdgeLengthFilterMax() {
        return edgeLengthFilterMax;
    }

    public boolean getAutoAdjustEdgeColorScale() {
        return autoAdjustEdgeColorScale;
    }

    public double getMaxEdgeWidth() {
        return maxEdgeWidth;
    }

    public GraphStats getGraphStats() {
        return graphStats;
    }

    public void setEdgeMarkerAlpha(int edgeMarkerAlpha) {
        int old = this.edgeMarkerAlpha;
        this.edgeMarkerAlpha = edgeMarkerAlpha;
        changes.firePropertyChange(PROPERTY_EDGE_MARKER_ALPHA, old, edgeMarkerAlpha);
    }

    public void setValueFilterMin(double valueFilterMin) {
        double old = this.valueFilterMin;
        this.valueFilterMin = valueFilterMin;
        changes.firePropertyChange(PROPERTY_VALUE_FILTER_MIN, old, valueFilterMin);
    }

    public void setValueFilterMax(double valueFilterMax) {
        double old = this.valueFilterMax;
        this.valueFilterMax = valueFilterMax;
        changes.firePropertyChange(PROPERTY_VALUE_FILTER_MAX, old, valueFilterMax);
    }

    public void setEdgeLengthFilterMin(double edgeLengthFilterMin) {
        double old = this.edgeLengthFilterMin;
        this.edgeLengthFilterMin = edgeLengthFilterMin;
        changes.firePropertyChange(PROPERTY_EDGE_LENGTH_FILTER_MIN, old, edgeLengthFilterMin);
    }

    public void setEdgeLengthFilterMax(double edgeLengthFilterMax) {
        double old = this.edgeLengthFilterMax;
        this.edgeLengthFilterMax = edgeLengthFilterMax;
        changes.firePropertyChange(PROPERTY_EDGE_LENGTH_FILTER_MAX, old, edgeLengthFilterMax);
    }

    public void setAutoAdjustEdgeColorScale(boolean autoAdjustEdgeColorScale) {
        boolean old = this.autoAdjustEdgeColorScale;
        this.autoAdjustEdgeColorScale = autoAdjustEdgeColorScale;
        changes.firePropertyChange(PROPERTY_AUTO_ADJUST_EDGE_COLOR_SCALE, old, autoAdjustEdgeColorScale);
    }

    public void setMaxEdgeWidth(double maxEdgeWidth) {
        double old = this.maxEdgeWidth;
        this.maxEdgeWidth = maxEdgeWidth;
        changes.firePropertyChange(PROPERTY_MAX_EDGE_WIDTH, old, maxEdgeWidth);
    }

    public void setEdgeAlpha(int edgeAlpha) {
        int old = this.edgeAlpha;
        this.edgeAlpha = edgeAlpha;
        changes.firePropertyChange(PROPERTY_EDGE_ALPHA, old, edgeAlpha);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changes.addPropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        changes.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changes.removePropertyChangeListener(listener);
    }

    public static final FlowMapModel load(FlowMap.DatasetSpec spec) throws DataIOException {
        GraphMLReader reader = new GraphMLReader();
        FlowMapModel model = new FlowMapModel(reader.readGraph(spec.filename), spec.valueAttrName, spec.labelAttrName);
        model.setValueFilterMin(1000);
        return model;
    }

    public static final String PROPERTY_AUTO_ADJUST_COLOR_SCALE = "autoAdjustColorScale";
    public static final String PROPERTY_USE_LOG_COLOR_SCALE = "useLogColorScale";
    public static final String PROPERTY_USE_LOG_WIDTH_SCALE = "useLogWidthScale";
    public static final String PROPERTY_MAX_LENGTH_FILTER = "lengthFilterMax";
    public static final String PROPERTY_MIN_LENGTH_FILTER = "lengthFilterMin";
    public static final String PROPERTY_VALUE_FILTER_MIN = "valueFilterMin";
    public static final String PROPERTY_VALUE_FILTER_MAX = "valueFilterMax";
    public static final String PROPERTY_EDGE_MARKER_ALPHA = "edgeMarkerAlpha";
    public static final String PROPERTY_EDGE_LENGTH_FILTER_MIN = "edgeLengthFilterMin";
    public static final String PROPERTY_EDGE_LENGTH_FILTER_MAX = "edgeLengthFilterMax";
    public static final String PROPERTY_AUTO_ADJUST_EDGE_COLOR_SCALE = "autoAdjustEdgeColorScale";
    public static final String PROPERTY_MAX_EDGE_WIDTH = "maxEdgeWidth";
    public static final String PROPERTY_EDGE_ALPHA = "edgeAlpha";

}
