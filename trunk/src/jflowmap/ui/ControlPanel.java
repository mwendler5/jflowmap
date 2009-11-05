package jflowmap.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableColumnModel;

import jflowmap.JFlowMap;
import jflowmap.bundling.ForceDirectedBundlerParameters;
import jflowmap.clustering.NodeDistanceMeasure;
import jflowmap.models.FlowMapParamsModel;
import jflowmap.util.GraphStats;
import jflowmap.util.MinMax;
import jflowmap.visuals.VisualFlowMap;
import at.fhj.utils.graphics.AxisMarks;
import at.fhj.utils.swing.FancyTable;
import at.fhj.utils.swing.TableSorter;
import at.fhj.utils.swing.FancyTable.FancyIconRenderer;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class ControlPanel {

    private JTabbedPane tabbedPane1;
    private JPanel panel1;
    private JCheckBox useLogColorScaleCheckbox;
    private JSlider maxEdgeWidthSlider;
    private JSpinner maxEdgeWidthSpinner;
    private JSpinner minValueFilterSpinner;
    private JSlider minLengthFilterSlider;
    private JSpinner minLengthFilterSpinner;
    private JSlider maxValueFilterSlider;
    private JSpinner maxValueFilterSpinner;
    private JCheckBox autoAdjustColorScaleCheckBox;
    private JSlider maxLengthFilterSlider;
    private JSpinner maxLengthFilterSpinner;
    private JSlider minValueFilterSlider;
    private JCheckBox useLogWidthScaleCheckbox;
    private JComboBox datasetCombo;
    private JComboBox comboBox2;
    private JComboBox comboBox3;
    private JComboBox comboBox4;
    private JComboBox comboBox5;
    private JComboBox comboBox6;
    private JSlider edgeOpacitySlider;
    private JSpinner edgeOpacitySpinner;
    private JCheckBox mapEdgeValueToCheckBox;
    private JCheckBox mapEdgeValueToCheckBox1;
    private JSlider edgeMarkerOpacitySlider;
    private JSpinner edgeMarkerOpacitySpinner;
    private JSpinner edgeStiffnessSpinner;
    private JSpinner stepSizeSpinner;
    private JSpinner edgeCompatibilityThresholdSpinner;
    private JSpinner stepDampingFactorSpinner;
    private JSpinner stepsInCycleSpinner;
    private JButton bundleButton;
    private JButton resetButton;
    private JSpinner numberOfCyclesSpinner;
    private JButton defaultValuesButton;
    private JCheckBox directionAffectsCompatibilityCheckBox;
    private JCheckBox binaryCompatibilityCheckBox;
    private JCheckBox inverseQuadraticModelCheckBox;
    private JCheckBox repulsiveEdgesCheckBox;
    private JCheckBox simpleCompatibilityMeasureCheckBox;
    private JCheckBox showNodesCheckBox;
    private JSpinner repulsionSpinner;
    private JCheckBox edgeValueAffectsAttractionCheckBox;
    private FancyTable similarNodesTable;
    private JButton clusterButton;
    private JSlider maxClusterDistanceSlider;
    private JCheckBox fillEdgesWithGradientCheckBox;
    private JCheckBox showDirectionMarkersCheckBox;
    private JSlider edgeMarkerSizeSlider;
    private JSpinner edgeMarkerSizeSpinner;
    private JCheckBox proportionalDirectionMarkersCheckBox;
    private JLabel edgeMarkerSizeLabel;
    private JLabel edgeMarkerOpacityLabel;
    private JSpinner maxClusterDistanceSpinner;
    private JComboBox distanceMeasureCombo;
    private JTabbedPane tabbedPane2;
    private FancyTable clustersTable;
    private final JFlowMap jFlowMap;
    private boolean initializing;
    private ForceDirectedBundlerParameters fdBundlingParams;
    private NodeSimilarityDistancesTableModel similarNodesTableModel;
    private TableSorter similarNodesTableSorter;
    private boolean modelsInitialized;
    private ClustersTableModel clustersTableModel;
    private TableSorter clustersTableSorter;

    public ControlPanel(JFlowMap flowMap) {
        this.jFlowMap = flowMap;
        $$$setupUI$$$();

        loadFlowMapData(flowMap.getVisualFlowMap());
        initChangeListeners();

        updateDirectionAffectsCompatibilityCheckBox();
        updateDirectionAffectsCompatibilityCheckBox();
        updateRepulsionSpinner();
        updateMarkersInputs();
    }

    private void loadFlowMapData(VisualFlowMap visualFlowMap) {
        fdBundlingParams = new ForceDirectedBundlerParameters(visualFlowMap.getGraphStats());
        if (!modelsInitialized) {
            initModelsOnce();
            modelsInitialized = true;
        }
        initModels();
        setData(visualFlowMap.getModel());
    }

    private void updateRepulsionSpinner() {
        repulsionSpinner.setEnabled(repulsiveEdgesCheckBox.isSelected());
    }

    private void updateSimpleCompatibilityMeasureCheckBox() {
        if (repulsiveEdgesCheckBox.isSelected()) {
            simpleCompatibilityMeasureCheckBox.setSelected(false);
            simpleCompatibilityMeasureCheckBox.setEnabled(false);
        } else {
            simpleCompatibilityMeasureCheckBox.setEnabled(true);
        }
    }

    private void updateDirectionAffectsCompatibilityCheckBox() {
        if (simpleCompatibilityMeasureCheckBox.isSelected() || repulsiveEdgesCheckBox.isSelected()) {
            directionAffectsCompatibilityCheckBox.setSelected(false);
            directionAffectsCompatibilityCheckBox.setEnabled(false);
        } else {
            directionAffectsCompatibilityCheckBox.setEnabled(true);
        }
    }

    private void updateMarkersInputs() {
        boolean showMarkers = showDirectionMarkersCheckBox.isSelected();
        proportionalDirectionMarkersCheckBox.setEnabled(showMarkers);
        edgeMarkerSizeSlider.setEnabled(showMarkers);
        edgeMarkerSizeSpinner.setEnabled(showMarkers);
        edgeMarkerSizeLabel.setEnabled(showMarkers);
        edgeMarkerOpacitySlider.setEnabled(showMarkers);
        edgeMarkerOpacitySpinner.setEnabled(showMarkers);
        edgeMarkerOpacityLabel.setEnabled(showMarkers);
    }

    private void initModelsOnce() {
        datasetCombo.setModel(new DefaultComboBoxModel(JFlowMap.datasetSpecs));
    }

    public void initForceDirectedEdgeBundlerParamsModels() {
        numberOfCyclesSpinner.setModel(new SpinnerNumberModel(fdBundlingParams.getNumCycles(), 1, 10, 1));
        stepsInCycleSpinner.setModel(new SpinnerNumberModel(fdBundlingParams.getI(), 1, 1000, 1));
        edgeCompatibilityThresholdSpinner.setModel(new SpinnerNumberModel(fdBundlingParams.getEdgeCompatibilityThreshold(), 0.0, 1.0, 0.1));

        double s = fdBundlingParams.getS();
        double sExp = AxisMarks.ordAlpha(s);
        double sStep = sExp / 100;
        double sMax = sExp * 100;
        stepSizeSpinner.setModel(new SpinnerNumberModel(s, 0.0, sMax, sStep));

        edgeStiffnessSpinner.setModel(new SpinnerNumberModel(fdBundlingParams.getK(), 0.0, 1000.0, 1.0));

        stepDampingFactorSpinner.setModel(new SpinnerNumberModel(fdBundlingParams.getStepDampingFactor(), 0.0, 1.0, 0.1));
        directionAffectsCompatibilityCheckBox.setSelected(fdBundlingParams.getDirectionAffectsCompatibility());
        binaryCompatibilityCheckBox.setSelected(fdBundlingParams.getBinaryCompatibility());
        inverseQuadraticModelCheckBox.setSelected(fdBundlingParams.getUseInverseQuadraticModel());
        repulsiveEdgesCheckBox.setSelected(fdBundlingParams.getUseRepulsionForOppositeEdges());
        simpleCompatibilityMeasureCheckBox.setSelected(fdBundlingParams.getUseSimpleCompatibilityMeasure());
        edgeValueAffectsAttractionCheckBox.setSelected(fdBundlingParams.getEdgeValueAffectsAttraction());
        repulsionSpinner.setModel(new SpinnerNumberModel(fdBundlingParams.getRepulsionAmount(), 0.0, 1.0, 0.1));
    }

    private void initModels() {
        initializing = true;
        GraphStats stats = getGraphStats();

        MinMax valueStats = stats.getValueEdgeAttrStats();

        minValueFilterSpinner.setModel(new SpinnerNumberModel(valueStats.getMin(), valueStats.getMin(), valueStats.getMax(), 1));
        maxValueFilterSpinner.setModel(new SpinnerNumberModel(valueStats.getMax(), valueStats.getMin(), valueStats.getMax(), 1));

        minValueFilterSlider.setMinimum(toValueEdgeFilterSliderValue(valueStats.getMin()));
        minValueFilterSlider.setMaximum(toValueEdgeFilterSliderValue(valueStats.getMax()));
        maxValueFilterSlider.setMinimum(toValueEdgeFilterSliderValue(valueStats.getMin()));
        maxValueFilterSlider.setMaximum(toValueEdgeFilterSliderValue(valueStats.getMax()));

        MinMax lengthStats = stats.getEdgeLengthStats();

        minLengthFilterSpinner.setModel(new SpinnerNumberModel(lengthStats.getMin(), lengthStats.getMin(), lengthStats.getMax(), 1));
        maxLengthFilterSpinner.setModel(new SpinnerNumberModel(lengthStats.getMax(), lengthStats.getMin(), lengthStats.getMax(), 1));

        minLengthFilterSlider.setMinimum(toEdgeLengthFilterSliderValue(lengthStats.getMin()));
        minLengthFilterSlider.setMaximum(toEdgeLengthFilterSliderValue(lengthStats.getMax()));
        maxLengthFilterSlider.setMinimum(toEdgeLengthFilterSliderValue(lengthStats.getMin()));
        maxLengthFilterSlider.setMaximum(toEdgeLengthFilterSliderValue(lengthStats.getMax()));

        edgeOpacitySpinner.setModel(new SpinnerNumberModel(0, 0, 255, 1));
        edgeOpacitySlider.setMinimum(0);
        edgeOpacitySlider.setMaximum(255);

        edgeMarkerSizeSpinner.setModel(new SpinnerNumberModel(0, 0, 0.5, 0.01));
        edgeMarkerSizeSlider.setMinimum(0);
        edgeMarkerSizeSlider.setMaximum(50);

        edgeMarkerOpacitySpinner.setModel(new SpinnerNumberModel(0, 0, 255, 1));
        edgeMarkerOpacitySlider.setMinimum(0);
        edgeMarkerOpacitySlider.setMaximum(255);

        maxEdgeWidthSpinner.setModel(new SpinnerNumberModel(0, 0, 100, 1));
        maxEdgeWidthSlider.setMinimum(0);
        maxEdgeWidthSlider.setMaximum(100);
        initializing = false;

        initForceDirectedEdgeBundlerParamsModels();

        maxClusterDistanceSpinner.setModel(new SpinnerNumberModel(0, 0, 10000, 1));
        maxClusterDistanceSlider.setMinimum(0);
        maxClusterDistanceSlider.setMaximum(10000);
    }

    public void setData(FlowMapParamsModel data) {
        autoAdjustColorScaleCheckBox.setSelected(data.getAutoAdjustColorScale());
        useLogWidthScaleCheckbox.setSelected(data.isUseLogWidthScale());
        useLogColorScaleCheckbox.setSelected(data.isUseLogColorScale());

        minLengthFilterSlider.setValue(toEdgeLengthFilterSliderValue(data.getEdgeLengthFilterMin()));
        maxLengthFilterSlider.setValue(toEdgeLengthFilterSliderValue(data.getEdgeLengthFilterMax()));
        minLengthFilterSpinner.setValue(data.getEdgeLengthFilterMin());
        maxLengthFilterSpinner.setValue(data.getEdgeLengthFilterMax());

        minValueFilterSlider.setValue(toValueEdgeFilterSliderValue(data.getValueFilterMin()));
        maxValueFilterSlider.setValue(toValueEdgeFilterSliderValue(data.getValueFilterMax()));
        minValueFilterSpinner.setValue(data.getValueFilterMin());
        maxValueFilterSpinner.setValue(data.getValueFilterMax());

        edgeOpacitySpinner.setValue(data.getEdgeAlpha());
        edgeOpacitySlider.setValue(data.getEdgeAlpha());
        edgeMarkerOpacitySpinner.setValue(data.getDirectionMarkerAlpha());
        edgeMarkerOpacitySlider.setValue(data.getDirectionMarkerAlpha());
        edgeMarkerSizeSlider.setValue(toEdgeMarkerSizeSliderValue(data.getDirectionMarkerSize()));
        edgeMarkerSizeSpinner.setValue(data.getDirectionMarkerSize());

        showDirectionMarkersCheckBox.setSelected(data.getShowDirectionMarkers());
        showNodesCheckBox.setSelected(data.getShowNodes());
        fillEdgesWithGradientCheckBox.setSelected(data.getFillEdgesWithGradient());
        proportionalDirectionMarkersCheckBox.setSelected(data.getUseProportionalDirectionMarkers());

        maxEdgeWidthSpinner.setValue(data.getMaxEdgeWidth());
        maxEdgeWidthSlider.setValue((int) Math.round(data.getMaxEdgeWidth()));
    }

    private void initChangeListeners() {
        datasetCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (initializing) return;
                loadFlowMap((JFlowMap.DatasetSpec) datasetCombo.getSelectedItem());
            }
        });

        // Edge value filter
        minValueFilterSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (initializing) return;
                minValueFilterSlider.setValue(toValueEdgeFilterSliderValue((Double) minValueFilterSpinner.getValue()));
            }
        });
        minValueFilterSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (initializing) return;
                double value = fromValueEdgeFilterSliderValue(minValueFilterSlider.getValue());
                getFlowMapModel().setValueFilterMin(value);
                minValueFilterSpinner.setValue(value);
            }
        });
        maxValueFilterSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (initializing) return;
                maxValueFilterSlider.setValue(toValueEdgeFilterSliderValue((Double) maxValueFilterSpinner.getValue()));
            }
        });
        maxValueFilterSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (initializing) return;
                double value = fromValueEdgeFilterSliderValue(maxValueFilterSlider.getValue());
                getFlowMapModel().setValueFilterMax(value);
                maxValueFilterSpinner.setValue(value);
            }
        });


        // Edge length filter
        minLengthFilterSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (initializing) return;
                minLengthFilterSlider.setValue(toEdgeLengthFilterSliderValue((Double) minLengthFilterSpinner.getValue()));
            }
        });
        minLengthFilterSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (initializing) return;
                double value = fromLengthEdgeFilterSliderValue(minLengthFilterSlider.getValue());
                getFlowMapModel().setEdgeLengthFilterMin(value);
                minLengthFilterSpinner.setValue(value);
            }
        });
        maxLengthFilterSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (initializing) return;
                maxLengthFilterSlider.setValue(toEdgeLengthFilterSliderValue((Double) maxLengthFilterSpinner.getValue()));
            }
        });
        maxLengthFilterSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (initializing) return;
                double value = fromLengthEdgeFilterSliderValue(maxLengthFilterSlider.getValue());
                getFlowMapModel().setEdgeLengthFilterMax(value);
                maxLengthFilterSpinner.setValue(value);
            }
        });


        // Aesthetics
        fillEdgesWithGradientCheckBox.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (initializing) return;
                getFlowMapModel().setFillEdgesWithGradient(
                        fillEdgesWithGradientCheckBox.isSelected());
            }
        });
        showDirectionMarkersCheckBox.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (initializing) return;
                getFlowMapModel().setShowDirectionMarkers(
                        showDirectionMarkersCheckBox.isSelected());
                updateMarkersInputs();
            }
        });
        proportionalDirectionMarkersCheckBox.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (initializing) return;
                getFlowMapModel().setUseProportionalDirectionMarkers(
                        proportionalDirectionMarkersCheckBox.isSelected());
            }
        });

        // Direction marker size
        edgeMarkerSizeSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (initializing) return;
                edgeMarkerSizeSlider.setValue(toEdgeMarkerSizeSliderValue(
                        (Double) edgeMarkerSizeSpinner.getValue()));
            }
        });
        edgeMarkerSizeSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (initializing) return;
                double value = fromEdgeMarkerSizeSliderValue(edgeMarkerSizeSlider.getValue());
                getFlowMapModel().setDirectionMarkerSize(value);
                edgeMarkerSizeSpinner.setValue(value);
            }
        });

        // Edge opacity
        edgeOpacitySpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (initializing) return;
                edgeOpacitySlider.setValue((Integer) edgeOpacitySpinner.getValue());
            }
        });
        edgeOpacitySlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (initializing) return;
                int value = edgeOpacitySlider.getValue();
                getFlowMapModel().setEdgeAlpha(value);
                edgeOpacitySpinner.setValue(value);
            }
        });

        // Edge marker opacity
        edgeMarkerOpacitySpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (initializing) return;
                edgeMarkerOpacitySlider.setValue((Integer) edgeMarkerOpacitySpinner.getValue());
            }
        });
        edgeMarkerOpacitySlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (initializing) return;
                int value = edgeMarkerOpacitySlider.getValue();
                getFlowMapModel().setDirectionMarkerAlpha(value);
                edgeMarkerOpacitySpinner.setValue(value);
            }
        });

        // Edge width
        maxEdgeWidthSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (initializing) return;
                maxEdgeWidthSlider.setValue((int) Math.round(((Number) maxEdgeWidthSpinner.getValue()).doubleValue()));
            }
        });
        maxEdgeWidthSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (initializing) return;
                int value = maxEdgeWidthSlider.getValue();
                getFlowMapModel().setMaxEdgeWidth(value);
                maxEdgeWidthSpinner.setValue(value);
            }
        });


        // Edge Bundling
        bundleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                fdBundlingParams.setNumCycles((Integer) numberOfCyclesSpinner.getValue());
                fdBundlingParams.setI((Integer) stepsInCycleSpinner.getValue());
                fdBundlingParams.setK((Double) edgeStiffnessSpinner.getValue());
                fdBundlingParams.setEdgeCompatibilityThreshold((Double) edgeCompatibilityThresholdSpinner.getValue());
                fdBundlingParams.setS((Double) stepSizeSpinner.getValue());
                fdBundlingParams.setStepDampingFactor((Double) stepDampingFactorSpinner.getValue());
                fdBundlingParams.setDirectionAffectsCompatibility(directionAffectsCompatibilityCheckBox.isSelected());
                fdBundlingParams.setBinaryCompatibility(binaryCompatibilityCheckBox.isSelected());
                fdBundlingParams.setUseInverseQuadraticModel(inverseQuadraticModelCheckBox.isSelected());
                fdBundlingParams.setUseRepulsionForOppositeEdges(repulsiveEdgesCheckBox.isSelected());
                fdBundlingParams.setUseSimpleCompatibilityMeasure(simpleCompatibilityMeasureCheckBox.isSelected());
                fdBundlingParams.setRepulsionAmount((Double) repulsionSpinner.getValue());
                fdBundlingParams.setEdgeValueAffectsAttraction(edgeValueAffectsAttractionCheckBox.isSelected());
                getVisualFlowMap().bundleEdges(fdBundlingParams);
            }
        });
        resetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jFlowMap.resetBundling();
            }
        });
        defaultValuesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                fdBundlingParams.resetToDefaults();
                initForceDirectedEdgeBundlerParamsModels();
            }
        });
        simpleCompatibilityMeasureCheckBox.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                updateDirectionAffectsCompatibilityCheckBox();
            }
        });
        repulsiveEdgesCheckBox.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                updateSimpleCompatibilityMeasureCheckBox();
                updateDirectionAffectsCompatibilityCheckBox();
                updateRepulsionSpinner();
            }
        });


        // Node clustering
        clusterButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getVisualFlowMap().clusterNodes((NodeDistanceMeasure) distanceMeasureCombo.getSelectedItem());
                double max = getVisualFlowMap().getMaxNodeDistance();
                double val = max / 2;
                getVisualFlowMap().setClusterDistanceThreshold(val);
                maxClusterDistanceSpinner.setModel(new SpinnerNumberModel(val, 0, max, AxisMarks.ordAlpha(max / 1000)));
                maxClusterDistanceSlider.setValue(toMaxClusterDistanceSliderValue(val));
                maxClusterDistanceSlider.setMaximum(toMaxClusterDistanceSliderValue(max));
                similarNodesTableModel.setDistances(getVisualFlowMap().getNodeDistanceList());
                similarNodesTableSorter.setSortingStatus(2, TableSorter.ASCENDING);
                clustersTableModel.setVisualNodes(getVisualFlowMap().getVisualNodes());
                clustersTableSorter.setSortingStatus(1, TableSorter.ASCENDING);   // to re-sort
            }
        });
        maxClusterDistanceSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (initializing) return;
                maxClusterDistanceSlider.setValue(toMaxClusterDistanceSliderValue((Double) maxClusterDistanceSpinner.getValue()));
                getVisualFlowMap().setClusterDistanceThreshold((Double) maxClusterDistanceSpinner.getValue());
                clustersTableModel.setVisualNodes(getVisualFlowMap().getVisualNodes()); // to update list (re-run filtering)
                clustersTableSorter.fireTableDataChanged();
                clustersTableSorter.setSortingStatus(1, TableSorter.ASCENDING); // to re-sort
            }
        });
        maxClusterDistanceSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (initializing) return;
                maxClusterDistanceSpinner.setValue(fromMaxClusterDistanceSliderValue(maxClusterDistanceSlider.getValue()));
            }
        });
    }

    private void loadFlowMap(JFlowMap.DatasetSpec dataset) {
        VisualFlowMap visualFlowMap = jFlowMap.loadFlowMap(dataset);
        jFlowMap.setVisualFlowMap(visualFlowMap);
        loadFlowMapData(visualFlowMap);
        similarNodesTableModel.setDistances(null);
        clustersTableModel.setVisualNodes(null);
    }

    public FlowMapParamsModel getFlowMapModel() {
        return getVisualFlowMap().getModel();
    }

    private VisualFlowMap getVisualFlowMap() {
        return jFlowMap.getVisualFlowMap();
    }

    private GraphStats getGraphStats() {
        return getVisualFlowMap().getGraphStats();
    }

    private double fromValueEdgeFilterSliderValue(final int logValue) {
        MinMax stats = getGraphStats().getValueEdgeAttrStats();
        double value = Math.round(Math.pow(Math.E, logValue));
        if (value < stats.getMin()) {
            value = stats.getMin();
        }
        if (value > stats.getMax()) {
            value = stats.getMax();
        }
        return value;
    }

    private double fromLengthEdgeFilterSliderValue(int value) {
        double v = value;
        MinMax stats = getGraphStats().getEdgeLengthStats();
        if (v < stats.getMin()) {
            v = stats.getMin();
        }
        if (v > stats.getMax()) {
            v = stats.getMax();
        }
        return v;
    }


    private int toMaxClusterDistanceSliderValue(double value) {
        double max = getVisualFlowMap().getMaxNodeDistance();
        return (int) Math.round(value / (max / 100));
    }

    private double fromMaxClusterDistanceSliderValue(int value) {
        double max = getVisualFlowMap().getMaxNodeDistance();
        return ((double) value) * (max / 100);
    }

    private int toValueEdgeFilterSliderValue(double value) {
        return (int) Math.round(Math.log(value));
    }

    private int toEdgeLengthFilterSliderValue(double value) {
        return (int) value;
    }

    private int toEdgeMarkerSizeSliderValue(double value) {
        return (int) (Math.round(100 * value));
    }

    private double fromEdgeMarkerSizeSliderValue(int value) {
        return value / 100.0;
    }

    public JPanel getPanel() {
        return panel1;
    }

//    public void getData(FlowMapModel data) {
//        data.setAutoAdjustColorScale(autoAdjustColorScaleCheckBox.isSelected());
//        data.setUseLogWidthScale(useLogWidthScaleCheckbox.isSelected());
//        data.setUseLogColorScale(useLogColorScaleCheckbox.isSelected());
//        data.setLengthFilterMin((Double) minLengthFilterSpinner.getValue());
//        data.setLengthFilterMax((Double) maxLengthFilterSpinner.getValue());
//    }
//
//    public boolean isModified(FlowMapModel data) {
//        if (autoAdjustColorScaleCheckBox.isSelected() != data.getAutoAdjustColorScale()) return true;
//        if (useLogWidthScaleCheckbox.isSelected() != data.isUseLogWidthScale()) return true;
//        if (useLogColorScaleCheckbox.isSelected() != data.isUseLogColorScale()) return true;
//        return false;
//    }

    private void createUIComponents() {
        // custom component creation code here

        // clustersTable
        clustersTableModel = new ClustersTableModel();
        clustersTableSorter = new TableSorter(clustersTableModel);
        clustersTableSorter.setColumnSortable(0, true);
        clustersTableSorter.setColumnSortable(1, true);

        clustersTable = new FancyTable(clustersTableSorter);
        clustersTable.setDefaultRenderer(ClustersTableModel.ClusterIcon.class, clustersTable.new FancyIconRenderer());
        clustersTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
//        clustersTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        clustersTable.setAutoCreateColumnsFromModel(false);
        clustersTableSorter.setTableHeader(clustersTable.getTableHeader());

        TableColumnModel tcm1 = clustersTable.getColumnModel();
        tcm1.getColumn(0).setPreferredWidth(300);
        tcm1.getColumn(0).setResizable(false);
        tcm1.getColumn(0).setMaxWidth(350);
        tcm1.getColumn(1).setPreferredWidth(50);
        tcm1.getColumn(1).setMaxWidth(100);
        tcm1.getColumn(1).setResizable(false);
        
//        tcm1.getColumn(1).setCellRenderer(new TableCellRenderer() {
//           public Component getTableCellRendererComponent(JTable table,
//                    Object value, boolean isSelected, boolean hasFocus,
//                    final int row, int column) {
//                return new JComponent() {
//                    private static final long serialVersionUID = 1L;
//                    Dimension size = new Dimension(32, 16);
//                    @Override
//                    public Dimension getPreferredSize() {
//                        return size;
//                    }
//                    @Override
//                    public void paint(Graphics g) {
//                        super.paint(g);
//                        Rectangle b = g.getClipBounds();
//                        g.setColor(clustersTableModel.getVisualNode(clustersTableSorter.modelIndex(row)).getClusterColor());
////                        g.fillRoundRect(b.x + 1, b.y + 1, b.width - 2, b.height - 2, 7, 7);
//                        int r = 6;
//                        g.fillOval(b.x + b.width/2 - r, b.y + b.height/2 - r, r, r);
//                    }
//                };
//            } 
//        });


        // similarNodesTable
        similarNodesTableModel = new NodeSimilarityDistancesTableModel();
        similarNodesTableSorter = new TableSorter(similarNodesTableModel);
        similarNodesTableSorter.setColumnSortable(0, true);
        similarNodesTableSorter.setColumnSortable(1, true);
        similarNodesTableSorter.setColumnSortable(2, true);

        similarNodesTable = new FancyTable(similarNodesTableSorter);
        similarNodesTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        similarNodesTable.setAutoCreateColumnsFromModel(false);
        similarNodesTableSorter.setTableHeader(similarNodesTable.getTableHeader());

        TableColumnModel tcm2 = similarNodesTable.getColumnModel();
        tcm2.getColumn(0).setPreferredWidth(100);
        tcm2.getColumn(0).setMaxWidth(150);
        tcm2.getColumn(1).setPreferredWidth(100);
        tcm2.getColumn(1).setMaxWidth(150);
        tcm2.getColumn(1).setPreferredWidth(120);
        tcm2.getColumn(1).setMaxWidth(150);

        distanceMeasureCombo = new JComboBox(NodeDistanceMeasure.values());
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        panel1 = new JPanel();
        panel1.setLayout(new BorderLayout(0, 0));
        tabbedPane1 = new JTabbedPane();
        panel1.add(tabbedPane1, BorderLayout.CENTER);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new FormLayout("fill:max(d;4px):noGrow,left:4dlu:noGrow,fill:187px:noGrow,left:4dlu:noGrow,fill:20px:noGrow,left:4dlu:noGrow,fill:p:noGrow,left:4dlu:noGrow,fill:119px:noGrow,left:20dlu:noGrow,fill:max(d;4px):noGrow,left:4dlu:noGrow,fill:119px:noGrow,left:5dlu:noGrow,fill:max(d;4px):grow", "center:d:noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow"));
        tabbedPane1.addTab("Dataset", panel2);
        panel2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10), null));
        datasetCombo = new JComboBox();
        CellConstraints cc = new CellConstraints();
        panel2.add(datasetCombo, cc.xy(3, 1));
        final JLabel label1 = new JLabel();
        label1.setText("Dataset:");
        panel2.add(label1, cc.xy(1, 1));
        final JSeparator separator1 = new JSeparator();
        separator1.setOrientation(1);
        panel2.add(separator1, cc.xywh(5, 1, 1, 5, CellConstraints.CENTER, CellConstraints.FILL));
        comboBox2 = new JComboBox();
        comboBox2.setEnabled(false);
        panel2.add(comboBox2, cc.xy(13, 1));
        final JLabel label2 = new JLabel();
        label2.setEnabled(false);
        label2.setText("Node X coord field:");
        panel2.add(label2, cc.xy(11, 1, CellConstraints.RIGHT, CellConstraints.DEFAULT));
        final JLabel label3 = new JLabel();
        label3.setEnabled(false);
        label3.setText("Node Y coord field:");
        panel2.add(label3, cc.xy(11, 3, CellConstraints.RIGHT, CellConstraints.DEFAULT));
        comboBox3 = new JComboBox();
        comboBox3.setEnabled(false);
        panel2.add(comboBox3, cc.xy(13, 3));
        final JLabel label4 = new JLabel();
        label4.setEnabled(false);
        label4.setText("Edge value field:");
        panel2.add(label4, cc.xy(7, 1, CellConstraints.RIGHT, CellConstraints.DEFAULT));
        comboBox4 = new JComboBox();
        comboBox4.setEnabled(false);
        panel2.add(comboBox4, cc.xy(9, 1));
        final JLabel label5 = new JLabel();
        label5.setEnabled(false);
        label5.setText("Node label field:");
        panel2.add(label5, cc.xy(7, 3, CellConstraints.RIGHT, CellConstraints.DEFAULT));
        comboBox6 = new JComboBox();
        comboBox6.setEnabled(false);
        panel2.add(comboBox6, cc.xy(9, 3));
        final JSeparator separator2 = new JSeparator();
        separator2.setOrientation(1);
        panel2.add(separator2, cc.xywh(10, 1, 1, 5, CellConstraints.CENTER, CellConstraints.FILL));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new FormLayout("right:max(d;4px):noGrow,left:4dlu:noGrow,fill:d:grow(2.0),left:4dlu:noGrow,fill:p:noGrow,left:4dlu:noGrow,fill:20px:noGrow,left:4dlu:noGrow,right:max(d;4px):noGrow,left:4dlu:noGrow,fill:d:grow,left:4dlu:noGrow,fill:p:noGrow", "center:26px:noGrow,top:4dlu:noGrow,center:24px:noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:5dlu:noGrow,center:d:noGrow"));
        tabbedPane1.addTab("Filter", panel3);
        panel3.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10), null));
        minValueFilterSpinner = new JSpinner();
        panel3.add(minValueFilterSpinner, cc.xy(5, 1, CellConstraints.FILL, CellConstraints.DEFAULT));
        final JSeparator separator3 = new JSeparator();
        separator3.setOrientation(1);
        panel3.add(separator3, cc.xywh(7, 1, 1, 5, CellConstraints.CENTER, CellConstraints.FILL));
        final JLabel label6 = new JLabel();
        label6.setText("Min length:");
        panel3.add(label6, cc.xy(9, 1));
        minLengthFilterSlider = new JSlider();
        panel3.add(minLengthFilterSlider, cc.xy(11, 1, CellConstraints.FILL, CellConstraints.DEFAULT));
        minLengthFilterSpinner = new JSpinner();
        panel3.add(minLengthFilterSpinner, cc.xy(13, 1, CellConstraints.FILL, CellConstraints.DEFAULT));
        final JLabel label7 = new JLabel();
        label7.setText("Max value:");
        panel3.add(label7, cc.xy(1, 3));
        maxValueFilterSlider = new JSlider();
        panel3.add(maxValueFilterSlider, cc.xy(3, 3, CellConstraints.FILL, CellConstraints.CENTER));
        maxValueFilterSpinner = new JSpinner();
        panel3.add(maxValueFilterSpinner, cc.xy(5, 3, CellConstraints.FILL, CellConstraints.DEFAULT));
        autoAdjustColorScaleCheckBox = new JCheckBox();
        autoAdjustColorScaleCheckBox.setEnabled(false);
        autoAdjustColorScaleCheckBox.setText("Auto adjust color scale");
        panel3.add(autoAdjustColorScaleCheckBox, cc.xyw(3, 5, 3));
        final JLabel label8 = new JLabel();
        label8.setText("Max length:");
        panel3.add(label8, cc.xy(9, 3));
        maxLengthFilterSlider = new JSlider();
        panel3.add(maxLengthFilterSlider, cc.xy(11, 3, CellConstraints.FILL, CellConstraints.DEFAULT));
        maxLengthFilterSpinner = new JSpinner();
        panel3.add(maxLengthFilterSpinner, cc.xy(13, 3, CellConstraints.FILL, CellConstraints.DEFAULT));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new FormLayout("fill:d:grow", "center:d:grow"));
        panel3.add(panel4, cc.xy(5, 7));
        final JLabel label9 = new JLabel();
        label9.setText("Min value:");
        panel3.add(label9, cc.xy(1, 1));
        minValueFilterSlider = new JSlider();
        panel3.add(minValueFilterSlider, cc.xy(3, 1, CellConstraints.FILL, CellConstraints.DEFAULT));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new FormLayout("fill:d:noGrow,left:p:noGrow,fill:20px:noGrow,left:4dlu:noGrow,fill:p:noGrow,left:20dlu:noGrow,fill:max(d;4px):grow", "center:max(d;4px):noGrow,top:4dlu:noGrow,center:24px:noGrow,top:6dlu:noGrow,top:4dlu:noGrow"));
        tabbedPane1.addTab("Scales", panel5);
        panel5.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10), null));
        final JSeparator separator4 = new JSeparator();
        separator4.setOrientation(1);
        panel5.add(separator4, cc.xywh(3, 1, 1, 5, CellConstraints.CENTER, CellConstraints.FILL));
        useLogWidthScaleCheckbox = new JCheckBox();
        useLogWidthScaleCheckbox.setEnabled(false);
        useLogWidthScaleCheckbox.setText("Use log width scale");
        panel5.add(useLogWidthScaleCheckbox, cc.xy(2, 1));
        useLogColorScaleCheckbox = new JCheckBox();
        useLogColorScaleCheckbox.setEnabled(false);
        useLogColorScaleCheckbox.setText("Use log color scale");
        panel5.add(useLogColorScaleCheckbox, cc.xyw(1, 3, 2));
        mapEdgeValueToCheckBox = new JCheckBox();
        mapEdgeValueToCheckBox.setEnabled(false);
        mapEdgeValueToCheckBox.setText("Map edge value to color");
        panel5.add(mapEdgeValueToCheckBox, cc.xy(5, 1));
        mapEdgeValueToCheckBox1 = new JCheckBox();
        mapEdgeValueToCheckBox1.setEnabled(false);
        mapEdgeValueToCheckBox1.setText("Map edge value to width");
        panel5.add(mapEdgeValueToCheckBox1, cc.xy(5, 3));
        final JSeparator separator5 = new JSeparator();
        separator5.setOrientation(1);
        panel5.add(separator5, cc.xywh(6, 1, 1, 5, CellConstraints.CENTER, CellConstraints.FILL));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new FormLayout("fill:d:noGrow,left:4dlu:noGrow,fill:110px:noGrow,left:4dlu:noGrow,fill:20px:noGrow,left:4dlu:noGrow,fill:max(d;4px):noGrow,fill:20px:noGrow,left:4dlu:noGrow,fill:max(d;4px):noGrow,left:4dlu:noGrow,fill:max(d;4px):grow,left:4dlu:noGrow,fill:max(m;50px):noGrow", "center:d:noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow"));
        tabbedPane1.addTab("Aesthetics", panel6);
        panel6.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10), null));
        final JLabel label10 = new JLabel();
        label10.setEnabled(false);
        label10.setText("Color scheme:");
        panel6.add(label10, cc.xy(1, 1));
        comboBox5 = new JComboBox();
        comboBox5.setEnabled(false);
        panel6.add(comboBox5, cc.xy(3, 1));
        final JSeparator separator6 = new JSeparator();
        separator6.setOrientation(1);
        panel6.add(separator6, cc.xywh(5, 1, 1, 7, CellConstraints.CENTER, CellConstraints.FILL));
        final JLabel label11 = new JLabel();
        label11.setText("Edge width:");
        panel6.add(label11, cc.xy(10, 1, CellConstraints.RIGHT, CellConstraints.CENTER));
        maxEdgeWidthSlider = new JSlider();
        maxEdgeWidthSlider.setPaintLabels(false);
        maxEdgeWidthSlider.setPaintTicks(false);
        maxEdgeWidthSlider.setPaintTrack(true);
        panel6.add(maxEdgeWidthSlider, cc.xy(12, 1, CellConstraints.FILL, CellConstraints.DEFAULT));
        maxEdgeWidthSpinner = new JSpinner();
        panel6.add(maxEdgeWidthSpinner, cc.xy(14, 1, CellConstraints.FILL, CellConstraints.DEFAULT));
        final JLabel label12 = new JLabel();
        label12.setText("Edge opacity:");
        panel6.add(label12, cc.xy(10, 3, CellConstraints.RIGHT, CellConstraints.DEFAULT));
        edgeOpacitySlider = new JSlider();
        panel6.add(edgeOpacitySlider, cc.xy(12, 3, CellConstraints.FILL, CellConstraints.DEFAULT));
        edgeOpacitySpinner = new JSpinner();
        panel6.add(edgeOpacitySpinner, cc.xy(14, 3, CellConstraints.FILL, CellConstraints.DEFAULT));
        showNodesCheckBox = new JCheckBox();
        showNodesCheckBox.setText("Show nodes");
        panel6.add(showNodesCheckBox, cc.xy(7, 1));
        final JSeparator separator7 = new JSeparator();
        separator7.setOrientation(1);
        panel6.add(separator7, cc.xywh(8, 1, 1, 7, CellConstraints.CENTER, CellConstraints.FILL));
        showDirectionMarkersCheckBox = new JCheckBox();
        showDirectionMarkersCheckBox.setText("Show direction markers");
        panel6.add(showDirectionMarkersCheckBox, cc.xy(7, 5));
        fillEdgesWithGradientCheckBox = new JCheckBox();
        fillEdgesWithGradientCheckBox.setText("Fill edges with gradient");
        panel6.add(fillEdgesWithGradientCheckBox, cc.xy(7, 3));
        proportionalDirectionMarkersCheckBox = new JCheckBox();
        proportionalDirectionMarkersCheckBox.setText("Proportional direction markers");
        panel6.add(proportionalDirectionMarkersCheckBox, cc.xy(7, 7));
        edgeMarkerSizeSpinner = new JSpinner();
        panel6.add(edgeMarkerSizeSpinner, cc.xy(14, 5, CellConstraints.FILL, CellConstraints.DEFAULT));
        edgeMarkerSizeSlider = new JSlider();
        panel6.add(edgeMarkerSizeSlider, cc.xy(12, 5, CellConstraints.FILL, CellConstraints.DEFAULT));
        edgeMarkerOpacitySpinner = new JSpinner();
        panel6.add(edgeMarkerOpacitySpinner, cc.xy(14, 7, CellConstraints.FILL, CellConstraints.DEFAULT));
        edgeMarkerOpacitySlider = new JSlider();
        panel6.add(edgeMarkerOpacitySlider, cc.xy(12, 7, CellConstraints.FILL, CellConstraints.DEFAULT));
        edgeMarkerSizeLabel = new JLabel();
        edgeMarkerSizeLabel.setText("Direction marker size:");
        panel6.add(edgeMarkerSizeLabel, cc.xy(10, 5, CellConstraints.RIGHT, CellConstraints.DEFAULT));
        edgeMarkerOpacityLabel = new JLabel();
        edgeMarkerOpacityLabel.setText("Direction marker opacity:");
        panel6.add(edgeMarkerOpacityLabel, cc.xy(10, 7, CellConstraints.RIGHT, CellConstraints.DEFAULT));
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new FormLayout("fill:max(d;4px):noGrow,left:4dlu:noGrow,fill:12px:noGrow,left:4dlu:noGrow,fill:p:noGrow,left:4dlu:noGrow,fill:p:noGrow,left:4dlu:noGrow,fill:12px:noGrow,left:4dlu:noGrow,fill:max(d;4px):noGrow,left:4dlu:noGrow,fill:p:noGrow,left:12dlu:noGrow,fill:p:noGrow,fill:d:noGrow,left:d:noGrow", "center:max(d;4px):noGrow,top:4dlu:noGrow,center:25px:noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow"));
        tabbedPane1.addTab("Edge bundling", panel7);
        panel7.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10), null));
        final JSeparator separator8 = new JSeparator();
        separator8.setOrientation(1);
        panel7.add(separator8, cc.xywh(9, 1, 1, 7, CellConstraints.CENTER, CellConstraints.FILL));
        final JLabel label13 = new JLabel();
        label13.setHorizontalAlignment(4);
        label13.setText("Step damping factor:");
        panel7.add(label13, cc.xy(11, 3));
        stepDampingFactorSpinner = new JSpinner();
        panel7.add(stepDampingFactorSpinner, cc.xy(13, 3, CellConstraints.FILL, CellConstraints.DEFAULT));
        stepSizeSpinner = new JSpinner();
        panel7.add(stepSizeSpinner, cc.xy(13, 1, CellConstraints.FILL, CellConstraints.DEFAULT));
        final JLabel label14 = new JLabel();
        label14.setHorizontalAlignment(4);
        label14.setText("Step size (S):");
        panel7.add(label14, cc.xy(11, 1, CellConstraints.RIGHT, CellConstraints.DEFAULT));
        final JLabel label15 = new JLabel();
        label15.setHorizontalAlignment(4);
        label15.setText("Edge stiffness (K):");
        panel7.add(label15, cc.xy(5, 3, CellConstraints.RIGHT, CellConstraints.DEFAULT));
        edgeStiffnessSpinner = new JSpinner();
        panel7.add(edgeStiffnessSpinner, cc.xy(7, 3, CellConstraints.FILL, CellConstraints.DEFAULT));
        final JSeparator separator9 = new JSeparator();
        separator9.setOrientation(1);
        panel7.add(separator9, cc.xywh(14, 1, 1, 7, CellConstraints.CENTER, CellConstraints.FILL));
        final JLabel label16 = new JLabel();
        label16.setHorizontalAlignment(4);
        label16.setText("Number of cycles:");
        panel7.add(label16, cc.xy(5, 1));
        numberOfCyclesSpinner = new JSpinner();
        panel7.add(numberOfCyclesSpinner, cc.xy(7, 1, CellConstraints.FILL, CellConstraints.DEFAULT));
        final JLabel label17 = new JLabel();
        label17.setHorizontalAlignment(4);
        label17.setText("Compatibility threshold:");
        panel7.add(label17, cc.xy(5, 5));
        edgeCompatibilityThresholdSpinner = new JSpinner();
        panel7.add(edgeCompatibilityThresholdSpinner, cc.xy(7, 5, CellConstraints.FILL, CellConstraints.DEFAULT));
        final JLabel label18 = new JLabel();
        label18.setHorizontalAlignment(4);
        label18.setText("Steps in 1st cycle (I):");
        panel7.add(label18, cc.xy(11, 5));
        stepsInCycleSpinner = new JSpinner();
        panel7.add(stepsInCycleSpinner, cc.xy(13, 5, CellConstraints.FILL, CellConstraints.DEFAULT));
        bundleButton = new JButton();
        bundleButton.setText("Bundle");
        panel7.add(bundleButton, cc.xy(1, 1));
        resetButton = new JButton();
        resetButton.setText("Reset");
        panel7.add(resetButton, cc.xy(1, 3));
        defaultValuesButton = new JButton();
        defaultValuesButton.setText("Default Values");
        panel7.add(defaultValuesButton, cc.xy(1, 5));
        final JSeparator separator10 = new JSeparator();
        separator10.setOrientation(1);
        panel7.add(separator10, cc.xywh(3, 1, 1, 7, CellConstraints.CENTER, CellConstraints.FILL));
        repulsiveEdgesCheckBox = new JCheckBox();
        repulsiveEdgesCheckBox.setText("Repulsion:");
        panel7.add(repulsiveEdgesCheckBox, cc.xy(5, 7, CellConstraints.RIGHT, CellConstraints.DEFAULT));
        repulsionSpinner = new JSpinner();
        panel7.add(repulsionSpinner, cc.xy(7, 7, CellConstraints.FILL, CellConstraints.DEFAULT));
        binaryCompatibilityCheckBox = new JCheckBox();
        binaryCompatibilityCheckBox.setText("Binary compatibility");
        panel7.add(binaryCompatibilityCheckBox, cc.xy(17, 7, CellConstraints.LEFT, CellConstraints.DEFAULT));
        simpleCompatibilityMeasureCheckBox = new JCheckBox();
        simpleCompatibilityMeasureCheckBox.setText("Simple compatibility measure");
        panel7.add(simpleCompatibilityMeasureCheckBox, cc.xy(17, 5, CellConstraints.LEFT, CellConstraints.DEFAULT));
        inverseQuadraticModelCheckBox = new JCheckBox();
        inverseQuadraticModelCheckBox.setText("Inverse-quadratic model");
        panel7.add(inverseQuadraticModelCheckBox, cc.xyw(11, 7, 3, CellConstraints.LEFT, CellConstraints.DEFAULT));
        directionAffectsCompatibilityCheckBox = new JCheckBox();
        directionAffectsCompatibilityCheckBox.setText("Direction affects compatibility");
        panel7.add(directionAffectsCompatibilityCheckBox, cc.xy(17, 1, CellConstraints.LEFT, CellConstraints.DEFAULT));
        edgeValueAffectsAttractionCheckBox = new JCheckBox();
        edgeValueAffectsAttractionCheckBox.setText("Edge value affects attraction");
        panel7.add(edgeValueAffectsAttractionCheckBox, cc.xy(17, 3));
        final JPanel panel8 = new JPanel();
        panel8.setLayout(new FormLayout("fill:max(d;4px):noGrow,left:4dlu:noGrow,fill:20px:noGrow,left:4dlu:noGrow,fill:max(d;4px):noGrow,left:4dlu:noGrow,fill:d:grow,left:4dlu:noGrow,fill:max(p;50px):noGrow,left:4dlu:noGrow,fill:20px:noGrow,left:4dlu:noGrow,fill:400px:noGrow", "center:max(p;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:91px:noGrow"));
        tabbedPane1.addTab("Node clustering", panel8);
        panel8.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10), null));
        final JSeparator separator11 = new JSeparator();
        separator11.setOrientation(1);
        panel8.add(separator11, cc.xywh(3, 1, 1, 5, CellConstraints.CENTER, CellConstraints.FILL));
        maxClusterDistanceSlider = new JSlider();
        panel8.add(maxClusterDistanceSlider, cc.xy(7, 3, CellConstraints.FILL, CellConstraints.DEFAULT));
        final JLabel label19 = new JLabel();
        label19.setText("Max cluster distance:");
        panel8.add(label19, cc.xy(5, 3));
        panel8.add(distanceMeasureCombo, cc.xy(7, 1, CellConstraints.LEFT, CellConstraints.DEFAULT));
        final JLabel label20 = new JLabel();
        label20.setText("Distance measure:");
        panel8.add(label20, cc.xy(5, 1, CellConstraints.RIGHT, CellConstraints.DEFAULT));
        clusterButton = new JButton();
        clusterButton.setText("Cluster");
        panel8.add(clusterButton, cc.xy(1, 1));
        maxClusterDistanceSpinner = new JSpinner();
        panel8.add(maxClusterDistanceSpinner, cc.xy(9, 3, CellConstraints.FILL, CellConstraints.DEFAULT));
        tabbedPane2 = new JTabbedPane();
        tabbedPane2.setTabPlacement(3);
        panel8.add(tabbedPane2, cc.xywh(13, 1, 1, 5, CellConstraints.DEFAULT, CellConstraints.FILL));
        final JScrollPane scrollPane1 = new JScrollPane();
        tabbedPane2.addTab("Clusters", scrollPane1);
        scrollPane1.setBorder(BorderFactory.createTitledBorder(""));
        clustersTable.setPreferredScrollableViewportSize(new Dimension(450, 100));
        scrollPane1.setViewportView(clustersTable);
        final JScrollPane scrollPane2 = new JScrollPane();
        tabbedPane2.addTab("Distances", scrollPane2);
        scrollPane2.setBorder(BorderFactory.createTitledBorder(""));
        similarNodesTable.setPreferredScrollableViewportSize(new Dimension(450, 100));
        scrollPane2.setViewportView(similarNodesTable);
        final JSeparator separator12 = new JSeparator();
        separator12.setOrientation(1);
        panel8.add(separator12, cc.xywh(11, 1, 1, 5, CellConstraints.CENTER, CellConstraints.FILL));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel1;
    }
}
