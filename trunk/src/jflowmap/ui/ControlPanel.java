package jflowmap.ui;

import java.awt.BorderLayout;
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
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import jflowmap.JFlowMap;
import jflowmap.bundling.ForceDirectedBundlerParameters;
import jflowmap.models.FlowMapParamsModel;
import jflowmap.util.Stats;
import jflowmap.visuals.VisualFlowMap;

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
    private FlowMapParamsModel flowMapModel;
    private final JFlowMap jFlowMap;
    private boolean initializing;
    private final ForceDirectedBundlerParameters fdBundlingParams = new ForceDirectedBundlerParameters();

    public ControlPanel(JFlowMap flowMap, FlowMapParamsModel model) {
        this.jFlowMap = flowMap;
        initModelsOnce();
        setFlowMapParamsModel(model);
        initChangeListeners();
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
                jFlowMap.bundleEdges(fdBundlingParams);
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
        updateDirectionAffectsCompatibilityCheckBox();
        updateDirectionAffectsCompatibilityCheckBox();
        updateRepulsionSpinner();
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

    public void setFlowMapParamsModel(FlowMapParamsModel model) {
        this.flowMapModel = model;
        initModels();
        setData(model);
    }

    private void initModelsOnce() {
        datasetCombo.setModel(new DefaultComboBoxModel(JFlowMap.datasetSpecs));
        initForceDirectedEdgeBundlerParamsModels();
    }

    public void initForceDirectedEdgeBundlerParamsModels() {
        numberOfCyclesSpinner.setModel(new SpinnerNumberModel(fdBundlingParams.getNumCycles(), 1, 10, 1));
        stepsInCycleSpinner.setModel(new SpinnerNumberModel(fdBundlingParams.getI(), 1, 1000, 1));
        edgeStiffnessSpinner.setModel(new SpinnerNumberModel(fdBundlingParams.getK(), 0.0, 1000.0, 1.0));
        edgeCompatibilityThresholdSpinner.setModel(new SpinnerNumberModel(fdBundlingParams.getEdgeCompatibilityThreshold(), 0.0, 1.0, 0.1));
        stepSizeSpinner.setModel(new SpinnerNumberModel(fdBundlingParams.getS(), 0.0, 1.0, 0.1));
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
        Stats valueStats = flowMapModel.getGraphStats().getValueEdgeAttrStats();

        minValueFilterSpinner.setModel(new SpinnerNumberModel(valueStats.min, valueStats.min, valueStats.max, 1));
        maxValueFilterSpinner.setModel(new SpinnerNumberModel(valueStats.max, valueStats.min, valueStats.max, 1));

        minValueFilterSlider.setMinimum(toValueEdgeFilterSliderValue(valueStats.min));
        minValueFilterSlider.setMaximum(toValueEdgeFilterSliderValue(valueStats.max));
        maxValueFilterSlider.setMinimum(toValueEdgeFilterSliderValue(valueStats.min));
        maxValueFilterSlider.setMaximum(toValueEdgeFilterSliderValue(valueStats.max));

        Stats lengthStats = flowMapModel.getGraphStats().getEdgeLengthStats();

        minLengthFilterSpinner.setModel(new SpinnerNumberModel(lengthStats.min, lengthStats.min, lengthStats.max, 1));
        maxLengthFilterSpinner.setModel(new SpinnerNumberModel(lengthStats.max, lengthStats.min, lengthStats.max, 1));

        minLengthFilterSlider.setMinimum(toEdgeLengthFilterSliderValue(lengthStats.min));
        minLengthFilterSlider.setMaximum(toEdgeLengthFilterSliderValue(lengthStats.max));
        maxLengthFilterSlider.setMinimum(toEdgeLengthFilterSliderValue(lengthStats.min));
        maxLengthFilterSlider.setMaximum(toEdgeLengthFilterSliderValue(lengthStats.max));

        edgeOpacitySpinner.setModel(new SpinnerNumberModel(0, 0, 255, 1));
        edgeOpacitySlider.setMinimum(0);
        edgeOpacitySlider.setMaximum(255);

        edgeMarkerOpacitySpinner.setModel(new SpinnerNumberModel(0, 0, 255, 1));
        edgeMarkerOpacitySlider.setMinimum(0);
        edgeMarkerOpacitySlider.setMaximum(255);

        maxEdgeWidthSpinner.setModel(new SpinnerNumberModel(0, 0, 100, 1));
        maxEdgeWidthSlider.setMinimum(0);
        maxEdgeWidthSlider.setMaximum(100);
        initializing = false;
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
                getFlowMapModel().setEdgeMarkerAlpha(value);
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
    }

    private void loadFlowMap(JFlowMap.DatasetSpec dataset) {
        VisualFlowMap visualFlowMap = jFlowMap.loadFlowMap(dataset);
        jFlowMap.setVisualFlowMap(visualFlowMap);
        setFlowMapParamsModel(visualFlowMap.getModel());
    }

    public FlowMapParamsModel getFlowMapModel() {
        return flowMapModel;
    }

    private double fromValueEdgeFilterSliderValue(final int logValue) {
        Stats stats = flowMapModel.getGraphStats().getValueEdgeAttrStats();
        double value = Math.round(Math.pow(Math.E, logValue));
        if (value < stats.min) {
            value = stats.min;
        }
        if (value > stats.max) {
            value = stats.max;
        }
        return value;
    }

    private double fromLengthEdgeFilterSliderValue(int value) {
        double v = value;
        Stats stats = flowMapModel.getGraphStats().getEdgeLengthStats();
        if (v < stats.min) {
            v = stats.min;
        }
        if (v > stats.max) {
            v = stats.max;
        }
        return v;
    }


    private int toValueEdgeFilterSliderValue(double value) {
        return (int) Math.round(Math.log(value));
    }

    private int toEdgeLengthFilterSliderValue(double value) {
        return (int) value;
    }

    public JPanel getPanel() {
        return panel1;
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
        edgeMarkerOpacitySpinner.setValue(data.getEdgeMarkerAlpha());
        edgeMarkerOpacitySlider.setValue(data.getEdgeMarkerAlpha());

        maxEdgeWidthSpinner.setValue(data.getMaxEdgeWidth());
        maxEdgeWidthSlider.setValue((int) Math.round(data.getMaxEdgeWidth()));
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
        // TODO: place custom component creation code here
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
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
        panel3.setLayout(new FormLayout("right:max(d;4px):noGrow,left:4dlu:noGrow,fill:d:grow(2.0),left:4dlu:noGrow,fill:92px:noGrow,left:4dlu:noGrow,fill:20px:noGrow,left:4dlu:noGrow,right:max(d;4px):noGrow,left:4dlu:noGrow,fill:d:grow,left:4dlu:noGrow,fill:50px:noGrow", "center:26px:noGrow,top:4dlu:noGrow,center:24px:noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:5dlu:noGrow,center:d:noGrow"));
        tabbedPane1.addTab("Filter", panel3);
        panel3.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10), null));
        minValueFilterSpinner = new JSpinner();
        panel3.add(minValueFilterSpinner, cc.xy(5, 1, CellConstraints.FILL, CellConstraints.DEFAULT));
        final JSeparator separator3 = new JSeparator();
        separator3.setOrientation(1);
        panel3.add(separator3, cc.xywh(7, 1, 1, 8, CellConstraints.CENTER, CellConstraints.FILL));
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
        panel3.add(panel4, cc.xy(5, 9));
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
        panel6.setLayout(new FormLayout("fill:d:noGrow,left:4dlu:noGrow,fill:110px:noGrow,left:4dlu:noGrow,fill:20px:noGrow,left:4dlu:noGrow,fill:max(d;4px):noGrow,left:4dlu:noGrow,fill:max(d;4px):noGrow,left:4dlu:noGrow,fill:39px:noGrow,left:4dlu:noGrow,fill:20px:noGrow,left:4dlu:noGrow,fill:max(d;4px):noGrow,left:6dlu:noGrow,fill:max(d;4px):grow", "center:d:noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow"));
        tabbedPane1.addTab("Aesthetics", panel6);
        panel6.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10), null));
        final JLabel label10 = new JLabel();
        label10.setEnabled(false);
        label10.setText("Color scheme:");
        panel6.add(label10, cc.xy(1, 1));
        comboBox5 = new JComboBox();
        comboBox5.setEnabled(false);
        panel6.add(comboBox5, cc.xy(3, 1));
        final JLabel label11 = new JLabel();
        label11.setText("Edge opacity:");
        panel6.add(label11, cc.xy(7, 1, CellConstraints.RIGHT, CellConstraints.DEFAULT));
        final JLabel label12 = new JLabel();
        label12.setText("Edge marker opacity:");
        panel6.add(label12, cc.xy(7, 3, CellConstraints.RIGHT, CellConstraints.DEFAULT));
        edgeOpacitySlider = new JSlider();
        panel6.add(edgeOpacitySlider, cc.xy(9, 1, CellConstraints.FILL, CellConstraints.DEFAULT));
        edgeMarkerOpacitySlider = new JSlider();
        panel6.add(edgeMarkerOpacitySlider, cc.xy(9, 3, CellConstraints.FILL, CellConstraints.DEFAULT));
        edgeOpacitySpinner = new JSpinner();
        panel6.add(edgeOpacitySpinner, cc.xy(11, 1, CellConstraints.FILL, CellConstraints.DEFAULT));
        edgeMarkerOpacitySpinner = new JSpinner();
        panel6.add(edgeMarkerOpacitySpinner, cc.xy(11, 3, CellConstraints.FILL, CellConstraints.DEFAULT));
        final JSeparator separator6 = new JSeparator();
        separator6.setOrientation(1);
        panel6.add(separator6, cc.xywh(5, 1, 1, 5, CellConstraints.CENTER, CellConstraints.FILL));
        maxEdgeWidthSpinner = new JSpinner();
        panel6.add(maxEdgeWidthSpinner, cc.xy(11, 5, CellConstraints.FILL, CellConstraints.DEFAULT));
        maxEdgeWidthSlider = new JSlider();
        maxEdgeWidthSlider.setPaintLabels(false);
        maxEdgeWidthSlider.setPaintTicks(false);
        maxEdgeWidthSlider.setPaintTrack(true);
        panel6.add(maxEdgeWidthSlider, cc.xy(9, 5, CellConstraints.FILL, CellConstraints.DEFAULT));
        final JLabel label13 = new JLabel();
        label13.setText("Max edge width:");
        panel6.add(label13, cc.xy(7, 5, CellConstraints.RIGHT, CellConstraints.CENTER));
        final JSeparator separator7 = new JSeparator();
        separator7.setOrientation(1);
        panel6.add(separator7, cc.xywh(13, 1, 1, 5, CellConstraints.CENTER, CellConstraints.FILL));
        showNodesCheckBox = new JCheckBox();
        showNodesCheckBox.setText("Show nodes");
        panel6.add(showNodesCheckBox, cc.xy(15, 1));
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new FormLayout("fill:max(d;4px):noGrow,left:4dlu:noGrow,fill:12px:noGrow,left:4dlu:noGrow,fill:p:noGrow,left:4dlu:noGrow,fill:p:noGrow,left:4dlu:noGrow,fill:12px:noGrow,left:4dlu:noGrow,fill:max(d;4px):noGrow,left:4dlu:noGrow,fill:p:noGrow,left:12dlu:noGrow,fill:p:noGrow,fill:d:noGrow,left:d:noGrow,left:6dlu:noGrow,fill:m:noGrow,left:4dlu:noGrow,fill:d:noGrow,left:4dlu:noGrow,fill:p:noGrow,left:4dlu:noGrow,fill:69px:noGrow", "center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow"));
        tabbedPane1.addTab("Force-Directed Edge Bundling", panel7);
        panel7.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10), null));
        final JSeparator separator8 = new JSeparator();
        separator8.setOrientation(1);
        panel7.add(separator8, cc.xywh(9, 1, 1, 6, CellConstraints.CENTER, CellConstraints.FILL));
        final JLabel label14 = new JLabel();
        label14.setHorizontalAlignment(4);
        label14.setText("Step damping factor:");
        panel7.add(label14, cc.xy(11, 3));
        stepDampingFactorSpinner = new JSpinner();
        panel7.add(stepDampingFactorSpinner, cc.xy(13, 3, CellConstraints.FILL, CellConstraints.DEFAULT));
        stepSizeSpinner = new JSpinner();
        panel7.add(stepSizeSpinner, cc.xy(13, 1, CellConstraints.FILL, CellConstraints.DEFAULT));
        final JLabel label15 = new JLabel();
        label15.setHorizontalAlignment(4);
        label15.setText("Step size (S):");
        panel7.add(label15, cc.xy(11, 1, CellConstraints.RIGHT, CellConstraints.DEFAULT));
        final JLabel label16 = new JLabel();
        label16.setHorizontalAlignment(4);
        label16.setText("Edge stiffness (K):");
        panel7.add(label16, cc.xy(5, 3, CellConstraints.RIGHT, CellConstraints.DEFAULT));
        edgeStiffnessSpinner = new JSpinner();
        panel7.add(edgeStiffnessSpinner, cc.xy(7, 3, CellConstraints.FILL, CellConstraints.DEFAULT));
        final JSeparator separator9 = new JSeparator();
        separator9.setOrientation(1);
        panel7.add(separator9, cc.xywh(14, 1, 1, 6, CellConstraints.CENTER, CellConstraints.FILL));
        final JLabel label17 = new JLabel();
        label17.setHorizontalAlignment(4);
        label17.setText("Number of cycles:");
        panel7.add(label17, cc.xy(5, 1));
        numberOfCyclesSpinner = new JSpinner();
        panel7.add(numberOfCyclesSpinner, cc.xy(7, 1, CellConstraints.FILL, CellConstraints.DEFAULT));
        final JLabel label18 = new JLabel();
        label18.setHorizontalAlignment(4);
        label18.setText("Compatibility threshold:");
        panel7.add(label18, cc.xy(5, 5));
        edgeCompatibilityThresholdSpinner = new JSpinner();
        panel7.add(edgeCompatibilityThresholdSpinner, cc.xy(7, 5, CellConstraints.FILL, CellConstraints.DEFAULT));
        final JLabel label19 = new JLabel();
        label19.setHorizontalAlignment(4);
        label19.setText("Steps in 1st cycle (I):");
        panel7.add(label19, cc.xy(11, 5));
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
        panel7.add(separator10, cc.xywh(3, 1, 1, 5, CellConstraints.CENTER, CellConstraints.FILL));
        directionAffectsCompatibilityCheckBox = new JCheckBox();
        directionAffectsCompatibilityCheckBox.setText("Direction affects compatibility");
        panel7.add(directionAffectsCompatibilityCheckBox, cc.xy(17, 1, CellConstraints.LEFT, CellConstraints.DEFAULT));
        simpleCompatibilityMeasureCheckBox = new JCheckBox();
        simpleCompatibilityMeasureCheckBox.setText("Simple compatibility measure");
        panel7.add(simpleCompatibilityMeasureCheckBox, cc.xy(17, 3, CellConstraints.LEFT, CellConstraints.DEFAULT));
        repulsionSpinner = new JSpinner();
        panel7.add(repulsionSpinner, cc.xy(23, 3, CellConstraints.FILL, CellConstraints.DEFAULT));
        repulsiveEdgesCheckBox = new JCheckBox();
        repulsiveEdgesCheckBox.setText("Repulsion:");
        panel7.add(repulsiveEdgesCheckBox, cc.xy(21, 3, CellConstraints.LEFT, CellConstraints.DEFAULT));
        binaryCompatibilityCheckBox = new JCheckBox();
        binaryCompatibilityCheckBox.setText("Binary compatibility");
        panel7.add(binaryCompatibilityCheckBox, cc.xy(17, 5, CellConstraints.LEFT, CellConstraints.DEFAULT));
        inverseQuadraticModelCheckBox = new JCheckBox();
        inverseQuadraticModelCheckBox.setText("Inverse-quadratic model");
        panel7.add(inverseQuadraticModelCheckBox, cc.xyw(21, 1, 3, CellConstraints.LEFT, CellConstraints.DEFAULT));
        final JSeparator separator11 = new JSeparator();
        separator11.setOrientation(1);
        panel7.add(separator11, cc.xywh(19, 1, 1, 6, CellConstraints.CENTER, CellConstraints.FILL));
        edgeValueAffectsAttractionCheckBox = new JCheckBox();
        edgeValueAffectsAttractionCheckBox.setText("Edge value affects attraction");
        panel7.add(edgeValueAffectsAttractionCheckBox, cc.xyw(21, 5, 5));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel1;
    }
}