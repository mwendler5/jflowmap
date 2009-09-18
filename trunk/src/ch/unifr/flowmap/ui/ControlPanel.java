/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ControlPanel.java
 *
 * Created on 15-Sep-2009, 14:46:54
 */

package ch.unifr.flowmap.ui;

import ch.unifr.flowmap.data.Stats;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

/**
 *
 * @author boyandii
 */
public class ControlPanel extends javax.swing.JPanel {
    
    private final FlowMapCanvas flowMapCanvas;

    /** Creates new form ControlPanel */
    public ControlPanel(FlowMapCanvas flowMapCanvas) {
        this.flowMapCanvas = flowMapCanvas;
        initComponents();
    }

    private SpinnerModel createMinValueFilterSpinnerModel() {
        Stats stats = flowMapCanvas.getEdgeValueAttrStats();
        return new SpinnerNumberModel(
                flowMapCanvas.getValueFilterMin(), stats.min, stats.max, 1);
    }

    private SpinnerModel createMaxValueFilterSpinnerModel() {
        Stats stats = flowMapCanvas.getEdgeValueAttrStats();
        return new SpinnerNumberModel(
                flowMapCanvas.getValueFilterMax(), stats.min, stats.max, 1);
    }

    private double fromLogValueFilter(final int v) {
        double value = Math.round(Math.pow(Math.E, v));
        final Stats stats = flowMapCanvas.getEdgeValueAttrStats();
        if (value < stats.min) {
            value = stats.min;
        }
        if (value > stats.max) {
            value = stats.max;
        }
        return value;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        jCheckBox1 = new javax.swing.JCheckBox();
        minValueFilterSlider = new javax.swing.JSlider();
        maxValueFilterSlider = new javax.swing.JSlider();
        datasetCombo = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        maxEdgeWidthSlider = new javax.swing.JSlider();
        jLabel1 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        edgeOpacitySlider = new javax.swing.JSlider();
        jLabel6 = new javax.swing.JLabel();
        edgeMarkerOpacitySlider = new javax.swing.JSlider();
        minValueFilterSpinner = new javax.swing.JSpinner();
        maxValueFilterSpinner = new javax.swing.JSpinner();
        edgeMarkerOpacitySpinner = new javax.swing.JSpinner();
        edgeOpacitySpinner = new javax.swing.JSpinner();
        autoAdjustColorScaleChk = new javax.swing.JCheckBox();
        maxEdgeWidthSpinner = new javax.swing.JSpinner();

        jCheckBox1.setText("jCheckBox1");

        minValueFilterSlider.setMaximum((int)Math.round(flowMapCanvas.getEdgeValueAttrStats().maxLog));
        minValueFilterSlider.setMinimum((int)Math.round(flowMapCanvas.getEdgeValueAttrStats().minLog));
        minValueFilterSlider.setValue((int)flowMapCanvas.getValueFilterMin());
        minValueFilterSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                minValueFilterSliderStateChanged(evt);
            }
        });

        maxValueFilterSlider.setMaximum((int)Math.round(flowMapCanvas.getEdgeValueAttrStats().maxLog));
        maxValueFilterSlider.setMinimum((int)Math.round(flowMapCanvas.getEdgeValueAttrStats().minLog));
        maxValueFilterSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                maxValueFilterSliderStateChanged(evt);
            }
        });

        datasetCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "refugee-flows-2008.xml", "migrations.xml", " " }));
        datasetCombo.setEnabled(false);
        datasetCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                datasetComboActionPerformed(evt);
            }
        });

        jLabel3.setText("Dataset:");
        jLabel3.setEnabled(false);

        jLabel2.setText("Filter min:");

        jLabel4.setText("Filter max:");

        maxEdgeWidthSlider.setMinimum(1);
        maxEdgeWidthSlider.setValue((int)flowMapCanvas.getMaxEdgeWidth());
        maxEdgeWidthSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                maxEdgeWidthSliderStateChanged(evt);
            }
        });

        jLabel1.setText("Maximal edge width:");

        jLabel5.setText("Edge opacity:");

        edgeOpacitySlider.setMaximum(255);
        edgeOpacitySlider.setValue(flowMapCanvas.getEdgeAlpha());
        edgeOpacitySlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                edgeOpacitySliderStateChanged(evt);
            }
        });

        jLabel6.setText("Edge marker opacity:");

        edgeMarkerOpacitySlider.setMaximum(255);
        edgeMarkerOpacitySlider.setValue(flowMapCanvas.getEdgeMarkerAlpha());
        edgeMarkerOpacitySlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                edgeMarkerOpacitySliderStateChanged(evt);
            }
        });

        minValueFilterSpinner.setModel(createMinValueFilterSpinnerModel());
        minValueFilterSpinner.setValue(flowMapCanvas.getValueFilterMin());
        minValueFilterSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                minValueFilterSpinnerStateChanged(evt);
            }
        });

        maxValueFilterSpinner.setModel(createMaxValueFilterSpinnerModel());
        maxValueFilterSpinner.setValue(flowMapCanvas.getValueFilterMax());

        edgeMarkerOpacitySpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, 255, 1));
        edgeMarkerOpacitySpinner.setOpaque(false);
        edgeMarkerOpacitySpinner.setValue(flowMapCanvas.getEdgeMarkerAlpha());
        edgeMarkerOpacitySpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                edgeMarkerOpacitySpinnerStateChanged(evt);
            }
        });

        edgeOpacitySpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, 255, 1));
        edgeOpacitySpinner.setValue(flowMapCanvas.getEdgeAlpha());
        edgeOpacitySpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                edgeOpacitySpinnerStateChanged(evt);
            }
        });

        autoAdjustColorScaleChk.setSelected(flowMapCanvas.getAutoAdjustEdgeColorScale());
        autoAdjustColorScaleChk.setText("Auto adjust color scale");
        autoAdjustColorScaleChk.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                autoAdjustColorScaleChkStateChanged(evt);
            }
        });

        maxEdgeWidthSpinner.setModel(new javax.swing.SpinnerNumberModel(1, 1, 100, 1));
        maxEdgeWidthSpinner.setValue((int)flowMapCanvas.getMaxEdgeWidth());
        maxEdgeWidthSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                maxEdgeWidthSpinnerStateChanged(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(datasetCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addGap(8, 8, 8))
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(maxValueFilterSlider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(minValueFilterSlider, javax.swing.GroupLayout.DEFAULT_SIZE, 324, Short.MAX_VALUE)
                            .addComponent(autoAdjustColorScaleChk))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(maxValueFilterSpinner)
                            .addComponent(minValueFilterSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, 72, Short.MAX_VALUE))))
                .addGap(33, 33, 33)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addComponent(jLabel1))
                    .addComponent(jLabel5)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(maxEdgeWidthSlider, javax.swing.GroupLayout.DEFAULT_SIZE, 173, Short.MAX_VALUE)
                    .addComponent(edgeMarkerOpacitySlider, javax.swing.GroupLayout.DEFAULT_SIZE, 173, Short.MAX_VALUE)
                    .addComponent(edgeOpacitySlider, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 173, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(edgeMarkerOpacitySpinner)
                        .addComponent(edgeOpacitySpinner))
                    .addComponent(maxEdgeWidthSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, 44, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(datasetCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(3, 3, 3)
                                .addComponent(jLabel2))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(minValueFilterSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, 24, Short.MAX_VALUE)
                                    .addComponent(minValueFilterSlider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(maxValueFilterSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(maxValueFilterSlider, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
                                    .addComponent(jLabel4)))))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addGap(16, 16, 16))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(maxEdgeWidthSpinner, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 26, Short.MAX_VALUE)
                                    .addComponent(maxEdgeWidthSlider, javax.swing.GroupLayout.DEFAULT_SIZE, 26, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(edgeOpacitySlider, javax.swing.GroupLayout.DEFAULT_SIZE, 26, Short.MAX_VALUE)
                            .addComponent(edgeOpacitySpinner, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 26, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addGap(10, 10, 10)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(edgeMarkerOpacitySpinner, javax.swing.GroupLayout.DEFAULT_SIZE, 24, Short.MAX_VALUE)
                            .addComponent(jLabel6)
                            .addComponent(edgeMarkerOpacitySlider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(autoAdjustColorScaleChk))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void datasetComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_datasetComboActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_datasetComboActionPerformed


    private void edgeOpacitySliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_edgeOpacitySliderStateChanged
        flowMapCanvas.setEdgeAlpha(edgeOpacitySlider.getValue());
        edgeOpacitySpinner.setValue(edgeOpacitySlider.getValue());
    }//GEN-LAST:event_edgeOpacitySliderStateChanged

    private void edgeMarkerOpacitySliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_edgeMarkerOpacitySliderStateChanged
        flowMapCanvas.setEdgeMarkerAlpha(edgeMarkerOpacitySlider.getValue());
        edgeMarkerOpacitySpinner.setValue(edgeMarkerOpacitySlider.getValue());
    }//GEN-LAST:event_edgeMarkerOpacitySliderStateChanged

    private void edgeMarkerOpacitySpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_edgeMarkerOpacitySpinnerStateChanged
        edgeMarkerOpacitySlider.setValue((Integer)edgeMarkerOpacitySpinner.getValue());
    }//GEN-LAST:event_edgeMarkerOpacitySpinnerStateChanged

    private void edgeOpacitySpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_edgeOpacitySpinnerStateChanged
        edgeOpacitySlider.setValue((Integer)edgeOpacitySpinner.getValue());
    }//GEN-LAST:event_edgeOpacitySpinnerStateChanged

    private void minValueFilterSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_minValueFilterSpinnerStateChanged
        minValueFilterSlider.setValue((int)Math.round(Math.log((Double)minValueFilterSpinner.getValue())));
    }//GEN-LAST:event_minValueFilterSpinnerStateChanged

    private void minValueFilterSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_minValueFilterSliderStateChanged
        double value = fromLogValueFilter(minValueFilterSlider.getValue());
        flowMapCanvas.setValueFilterMin(value);
        minValueFilterSpinner.setValue(value);
    }//GEN-LAST:event_minValueFilterSliderStateChanged

    private void maxValueFilterSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_maxValueFilterSliderStateChanged
        double value = fromLogValueFilter(maxValueFilterSlider.getValue());
        flowMapCanvas.setValueFilterMax(value);
        maxValueFilterSpinner.setValue(value);
    }//GEN-LAST:event_maxValueFilterSliderStateChanged

    private void autoAdjustColorScaleChkStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_autoAdjustColorScaleChkStateChanged
        flowMapCanvas.setAutoAdjustEdgeColorScale(autoAdjustColorScaleChk.isSelected());
    }//GEN-LAST:event_autoAdjustColorScaleChkStateChanged

    private void maxEdgeWidthSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_maxEdgeWidthSliderStateChanged
        flowMapCanvas.setMaxEdgeWidth(maxEdgeWidthSlider.getValue());
        maxEdgeWidthSpinner.setValue(maxEdgeWidthSlider.getValue());
    }//GEN-LAST:event_maxEdgeWidthSliderStateChanged

    private void maxEdgeWidthSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_maxEdgeWidthSpinnerStateChanged
        maxEdgeWidthSlider.setValue((Integer)maxEdgeWidthSpinner.getValue());
    }//GEN-LAST:event_maxEdgeWidthSpinnerStateChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox autoAdjustColorScaleChk;
    private javax.swing.JComboBox datasetCombo;
    private javax.swing.JSlider edgeMarkerOpacitySlider;
    private javax.swing.JSpinner edgeMarkerOpacitySpinner;
    private javax.swing.JSlider edgeOpacitySlider;
    private javax.swing.JSpinner edgeOpacitySpinner;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSlider maxEdgeWidthSlider;
    private javax.swing.JSpinner maxEdgeWidthSpinner;
    private javax.swing.JSlider maxValueFilterSlider;
    private javax.swing.JSpinner maxValueFilterSpinner;
    private javax.swing.JSlider minValueFilterSlider;
    private javax.swing.JSpinner minValueFilterSpinner;
    // End of variables declaration//GEN-END:variables

}
