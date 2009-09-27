package ch.unifr.flowmap;

import ch.unifr.flowmap.models.FlowMapModel;
import ch.unifr.flowmap.models.map.*;
import ch.unifr.flowmap.visuals.VisualFlowMap;
import ch.unifr.flowmap.visuals.VisualAreaMap;
import ch.unifr.flowmap.ui.ControlPanel;
import ch.unifr.flowmap.util.ZoomHandler;
import ch.unifr.flowmap.util.PanHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;

import prefuse.data.io.DataIOException;
import org.apache.log4j.Logger;
import edu.umd.cs.piccolo.PCanvas;

/**
 * @author Ilya Boyandin
 *         Date: 23-Sep-2009
 */
public class JFlowMap extends JComponent {

    private static Logger logger = Logger.getLogger(JFlowMap.class);

    private PCanvas canvas;
    private ControlPanel controlPanel;
    private VisualFlowMap visualFlowMap;

    public JFlowMap() {
        setLayout(new BorderLayout());

        canvas = new PCanvas();
        canvas.setBackground(Color.BLACK);
        canvas.addInputEventListener(new ZoomHandler(.5, 50));
        canvas.setPanEventHandler(new PanHandler());
        add(canvas, BorderLayout.CENTER);
        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                visualFlowMap.fitInCameraView();
            }
        });

        VisualFlowMap visFlowMap = loadFlowMap(datasetSpecs[0]);
        controlPanel = new ControlPanel(this, visFlowMap.getModel());
        add(controlPanel.getPanel(), BorderLayout.SOUTH);
        setVisualFlowMap(visFlowMap);
    }

    public void setVisualFlowMap(VisualFlowMap newFlowMap) {
        if (visualFlowMap != null) {
            canvas.getLayer().removeChild(visualFlowMap);
        }
        canvas.getLayer().addChild(newFlowMap);
        visualFlowMap = newFlowMap;

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                visualFlowMap.fitInCameraView();
            }
        });
    }

    public VisualFlowMap loadFlowMap(DatasetSpec dataset) {
        FlowMapModel model = null;
        try {
            model = FlowMapModel.load(dataset);
            VisualFlowMap visualFlowMap = new VisualFlowMap(canvas, model);
            if (dataset.areaMapFilename != null) {
                VisualAreaMap map = loadAreaMap(dataset.areaMapFilename);
                visualFlowMap.addChild(map);
                map.moveToBack();
            }
            return visualFlowMap;
        } catch (DataIOException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
            logger.error("Couldn't load flow map " + dataset, e);
        }
        return null;
    }

    private VisualAreaMap loadAreaMap(String areaMapFilename) {
        try {
            return new VisualAreaMap(AreaMap.load(areaMapFilename));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Couldn't load area map " + areaMapFilename + ":\n" + e.getMessage()
            );
            logger.error("Couldn't load area map " + areaMapFilename, e);
        }
        return null;
    }

    public static class DatasetSpec {
        public DatasetSpec(String filename, String valueAttrName, String labelAttrName, String areaMapFilename) {
            this.filename = filename;
            this.valueAttrName = valueAttrName;
            this.labelAttrName = labelAttrName;
            this.areaMapFilename = areaMapFilename;
        }
        public final String filename;
        public final String areaMapFilename;
        public final String valueAttrName;
        public final String labelAttrName;

        @Override
        public String toString() {
            return filename;
        }
    }
    
    public static final DatasetSpec[] datasetSpecs = new DatasetSpec[] {
            new DatasetSpec("data/migrations-unique.xml", "value", "tooltip", null),
            new DatasetSpec("data/refugee-flows-2008.xml", "refugees", "name", "data/countries-areas.xml"),
            new DatasetSpec("data/refugee-flows-2007.xml", "refugees", "name", "data/countries-areas.xml"),
            new DatasetSpec("data/refugee-flows-2006.xml", "refugees", "name", "data/countries-areas.xml"),
            new DatasetSpec("data/refugee-flows-2005.xml", "refugees", "name", "data/countries-areas.xml"),
            new DatasetSpec("data/refugee-flows-2004.xml", "refugees", "name", "data/countries-areas.xml"),
            new DatasetSpec("data/refugee-flows-2003.xml", "refugees", "name", "data/countries-areas.xml"),
            new DatasetSpec("data/refugee-flows-2002.xml", "refugees", "name", "data/countries-areas.xml"),
            new DatasetSpec("data/refugee-flows-2001.xml", "refugees", "name", "data/countries-areas.xml"),
            new DatasetSpec("data/refugee-flows-2000.xml", "refugees", "name", "data/countries-areas.xml"),
            new DatasetSpec("data/refugee-flows-1999.xml", "refugees", "name", "data/countries-areas.xml"),
            new DatasetSpec("data/refugee-flows-1998.xml", "refugees", "name", "data/countries-areas.xml"),
            new DatasetSpec("data/refugee-flows-1997.xml", "refugees", "name", "data/countries-areas.xml"),
            new DatasetSpec("data/refugee-flows-1996.xml", "refugees", "name", "data/countries-areas.xml"),
            new DatasetSpec("data/refugee-flows-1995.xml", "refugees", "name", "data/countries-areas.xml"),
            new DatasetSpec("data/refugee-flows-1994.xml", "refugees", "name", "data/countries-areas.xml"),
            new DatasetSpec("data/refugee-flows-1993.xml", "refugees", "name", "data/countries-areas.xml"),
            new DatasetSpec("data/refugee-flows-1992.xml", "refugees", "name", "data/countries-areas.xml"),
            new DatasetSpec("data/refugee-flows-1991.xml", "refugees", "name", "data/countries-areas.xml"),
            new DatasetSpec("data/refugee-flows-1990.xml", "refugees", "name", "data/countries-areas.xml"),
            new DatasetSpec("data/refugee-flows-1989.xml", "refugees", "name", "data/countries-areas.xml"),
            new DatasetSpec("data/refugee-flows-1988.xml", "refugees", "name", "data/countries-areas.xml"),
            new DatasetSpec("data/refugee-flows-1987.xml", "refugees", "name", "data/countries-areas.xml"),
            new DatasetSpec("data/refugee-flows-1986.xml", "refugees", "name", "data/countries-areas.xml"),
            new DatasetSpec("data/refugee-flows-1985.xml", "refugees", "name", "data/countries-areas.xml"),
            new DatasetSpec("data/refugee-flows-1984.xml", "refugees", "name", "data/countries-areas.xml"),
            new DatasetSpec("data/refugee-flows-1983.xml", "refugees", "name", "data/countries-areas.xml"),
            new DatasetSpec("data/refugee-flows-1982.xml", "refugees", "name", "data/countries-areas.xml"),
            new DatasetSpec("data/refugee-flows-1981.xml", "refugees", "name", "data/countries-areas.xml"),
            new DatasetSpec("data/refugee-flows-1980.xml", "refugees", "name", "data/countries-areas.xml"),
            new DatasetSpec("data/refugee-flows-1979.xml", "refugees", "name", "data/countries-areas.xml"),
            new DatasetSpec("data/refugee-flows-1978.xml", "refugees", "name", "data/countries-areas.xml"),
            new DatasetSpec("data/refugee-flows-1977.xml", "refugees", "name", "data/countries-areas.xml"),
            new DatasetSpec("data/refugee-flows-1976.xml", "refugees", "name", "data/countries-areas.xml"),
            new DatasetSpec("data/refugee-flows-1975.xml", "refugees", "name", "data/countries-areas.xml"),
    };

}
