package ch.unifr.flowmap.ui;

import javax.swing.*;

public class ControlPanel2 {
    private JTabbedPane tabbedPane1;
    private JPanel panel1;
    private JCheckBox useLogColorScaleCheckBox;
    private JSlider maxEdgeWidthSlider;
    private JSpinner maxEdgeWidthSpinner;
    private JSlider edgeOpacitySlider;
    private JSpinner edgeOpacitySpinner;
    private JSpinner minValueFilterSpinner;
    private JSlider minLengthFilterSlider;
    private JSpinner minLengthFilterSpinner;
    private JSlider maxValueFilterSlider;
    private JSpinner maxValueFilterSpinner;
    private JCheckBox autoAdjustColorScaleCheckBox;
    private JSlider maxLengthFilterSlider;
    private JSpinner maxLengthFilterSpinner;
    private JSlider minValueFilterSlider;

    public ControlPanel2(FlowMapCanvas canvas) {
    }

    public JPanel getPanel() {
        return panel1;
    }

    public void setData(FlowMapModel data) {
    }

    public void getData(FlowMapModel data) {
    }

    public boolean isModified(FlowMapModel data) {
        return false;
    }
}
