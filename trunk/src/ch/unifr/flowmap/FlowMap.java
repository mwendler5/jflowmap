package ch.unifr.flowmap;

import ch.unifr.flowmap.models.FlowMapModel;
import ch.unifr.flowmap.models.map.MapModel;
import ch.unifr.flowmap.visuals.FlowMapCanvas;
import ch.unifr.flowmap.ui.ControlPanel;

import javax.swing.*;
import java.awt.*;

import prefuse.data.Graph;
import prefuse.data.io.DataIOException;
import prefuse.data.io.GraphMLReader;

/**
 * @author Ilya Boyandin
 *         Date: 23-Sep-2009
 */
public class FlowMap extends JComponent {

    private FlowMapCanvas canvas;
    private ControlPanel controlPanel;

    public FlowMap() {
        setLayout(new BorderLayout());
        controlPanel = new ControlPanel(this);
        add(controlPanel.getPanel(), BorderLayout.SOUTH);
    }

    public FlowMapCanvas getCanvas() {
        return canvas;
    }

    public void setCanvas(FlowMapCanvas canvas) {
        if (this.canvas != null) {
            remove(this.canvas);
        }
        add(canvas, BorderLayout.CENTER);
        this.canvas = canvas;
    }

    public static class DatasetSpec {
        public DatasetSpec(String filename, String valueAttrName, String labelAttrName) {
            this.filename = filename;
            this.valueAttrName = valueAttrName;
            this.labelAttrName = labelAttrName;
        }
        public final String filename;
        public final String valueAttrName;
        public final String labelAttrName;

        @Override
        public String toString() {
            return filename;
        }
    }

}
