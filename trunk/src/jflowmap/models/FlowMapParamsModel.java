package jflowmap.models;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import jflowmap.util.GraphStats;
import jflowmap.util.MinMax;

import org.apache.log4j.Logger;

import prefuse.data.Graph;


public class FlowMapParamsModel {

    private static Logger logger = Logger.getLogger(FlowMapParamsModel.class);

    private boolean autoAdjustColorScale;
    private boolean useLogColorScale;
    private boolean useLogWidthScale;
    private boolean showNodes = true;
    private boolean showDirectionMarkers = true;
    private boolean fillEdgesWithGradient = false;
    private boolean useProportionalDirectionMarkers = true;

    private String valueEdgeAttr;
    private String xNodeAttr;
    private String yNodeAttr;
    private String labelAttr = "tooltip";

    private int edgeAlpha = 40;
    private int directionMarkerAlpha = 200;

    private double valueFilterMin = Double.MIN_VALUE;
    private double valueFilterMax = Double.MAX_VALUE;

    private double edgeLengthFilterMin = Double.MIN_VALUE;
    private double edgeLengthFilterMax = Double.MAX_VALUE;

    private boolean autoAdjustEdgeColorScale;
    private double maxEdgeWidth = 1.0;
    private double directionMarkerSize = 0.1; 

    private PropertyChangeSupport changes = new PropertyChangeSupport(this);

    public FlowMapParamsModel(GraphStats graphStats, String valueEdgeAttrName, 
    		String xNodeAttr, String yNodeAttr, String labelAttrName) {
        this.valueEdgeAttr = valueEdgeAttrName;
        this.xNodeAttr = xNodeAttr;
        this.yNodeAttr = yNodeAttr;
        this.labelAttr = labelAttrName;

        MinMax minMax = graphStats.getValueEdgeAttrStats();
        this.valueFilterMin = minMax.getMin();
        this.valueFilterMax = minMax.getMax();

        if (minMax.getMax() - minMax.getMin() > 10.0) {
            maxEdgeWidth = 10.0;
        } else {
            maxEdgeWidth = Math.floor(minMax.getMax() - minMax.getMin());
        }
        MinMax lengthStats = graphStats.getEdgeLengthStats();
        this.edgeLengthFilterMin = lengthStats.getMin();
        this.edgeLengthFilterMax = lengthStats.getMax();

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

    public String getValueEdgeAttr() {
        return valueEdgeAttr;
    }

    public String getLabelAttr() {
        return labelAttr;
    }

    public int getEdgeAlpha() {
        return edgeAlpha;
    }

    public int getDirectionMarkerAlpha() {
        return directionMarkerAlpha;
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

    public void setDirectionMarkerAlpha(int edgeMarkerAlpha) {
        int old = this.directionMarkerAlpha;
        this.directionMarkerAlpha = edgeMarkerAlpha;
        changes.firePropertyChange(PROPERTY_DIRECTION_MARKER_ALPHA, old, edgeMarkerAlpha);
    }

    public void setValueFilterMin(double valueFilterMin) {
        if (this.valueFilterMin != valueFilterMin) {
            double old = this.valueFilterMin;
            this.valueFilterMin = valueFilterMin;
            changes.firePropertyChange(PROPERTY_VALUE_FILTER_MIN, old, valueFilterMin);
        }
    }

    public void setValueFilterMax(double valueFilterMax) {
        if (this.valueFilterMax != valueFilterMax) {
            double old = this.valueFilterMax;
            this.valueFilterMax = valueFilterMax;
            changes.firePropertyChange(PROPERTY_VALUE_FILTER_MAX, old, valueFilterMax);
        }
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

    public boolean getShowNodes() {
		return showNodes;
	}

	public void setShowNodes(boolean value) {
		if (showNodes != value) {
			showNodes = value;
			changes.firePropertyChange(PROPERTY_SHOW_NODES, !value, value);
		}
	}

	public boolean getShowDirectionMarkers() {
		return showDirectionMarkers;
	}

	public void setShowDirectionMarkers(boolean value) {
		if (showDirectionMarkers != value) {
			showDirectionMarkers = value;
			changes.firePropertyChange(PROPERTY_SHOW_DIRECTION_MARKERS, !value, value);
		}
	}

	public boolean getFillEdgesWithGradient() {
		return fillEdgesWithGradient;
	}

	public void setFillEdgesWithGradient(boolean value) {
		if (fillEdgesWithGradient != value) {
			fillEdgesWithGradient = value;
			changes.firePropertyChange(PROPERTY_FILL_EDGES_WITH_GRADIENT, !value, value);
		}
	}

	public boolean getUseProportionalDirectionMarkers() {
		return useProportionalDirectionMarkers;
	}

	public void setUseProportionalDirectionMarkers(boolean value) {
		if (useProportionalDirectionMarkers != value) {
			useProportionalDirectionMarkers = value;
			changes.firePropertyChange(PROPERTY_USE_PROPORTIONAL_DIRECTION_MARKERS, !value, value);
		}
	}

	public double getDirectionMarkerSize() {
		return directionMarkerSize;
	}

	public void setDirectionMarkerSize(double markerSize) {
		if (markerSize < 0  ||  markerSize > .5) {
			throw new IllegalArgumentException(
					"Direction marker size must be between 0.0 and 0.5: attempted to set " + markerSize);
		}
		if (this.directionMarkerSize != markerSize) { 
			double old = directionMarkerSize;
			this.directionMarkerSize = markerSize;
			changes.firePropertyChange(PROPERTY_DIRECTION_MARKER_SIZE, old, markerSize);
		}
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

	public static final String PROPERTY_AUTO_ADJUST_COLOR_SCALE = "autoAdjustColorScale";
    public static final String PROPERTY_USE_LOG_COLOR_SCALE = "useLogColorScale";
    public static final String PROPERTY_USE_LOG_WIDTH_SCALE = "useLogWidthScale";
    public static final String PROPERTY_MAX_LENGTH_FILTER = "lengthFilterMax";
    public static final String PROPERTY_MIN_LENGTH_FILTER = "lengthFilterMin";
    public static final String PROPERTY_VALUE_FILTER_MIN = "valueFilterMin";
    public static final String PROPERTY_VALUE_FILTER_MAX = "valueFilterMax";
    public static final String PROPERTY_DIRECTION_MARKER_ALPHA = "directionMarkerAlpha";
    public static final String PROPERTY_DIRECTION_MARKER_SIZE = "directionMarkerSize";
    public static final String PROPERTY_EDGE_LENGTH_FILTER_MIN = "edgeLengthFilterMin";
    public static final String PROPERTY_EDGE_LENGTH_FILTER_MAX = "edgeLengthFilterMax";
    public static final String PROPERTY_AUTO_ADJUST_EDGE_COLOR_SCALE = "autoAdjustEdgeColorScale";
    public static final String PROPERTY_MAX_EDGE_WIDTH = "maxEdgeWidth";
    public static final String PROPERTY_EDGE_ALPHA = "edgeAlpha";
    public static final String PROPERTY_USE_PROPORTIONAL_DIRECTION_MARKERS = "proportionalDirMarkers";
    public static final String PROPERTY_FILL_EDGES_WITH_GRADIENT = "fillEdgesWithGradient";
    public static final String PROPERTY_SHOW_DIRECTION_MARKERS = "showDirectionMarkers";
    public static final String PROPERTY_SHOW_NODES = "showNodes";

}
