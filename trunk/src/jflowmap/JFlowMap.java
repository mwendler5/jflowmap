package jflowmap;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import jflowmap.models.FlowMapParamsModel;
import jflowmap.models.map.AreaMap;
import jflowmap.ui.ControlPanel;
import jflowmap.util.PanHandler;
import jflowmap.util.ZoomHandler;
import jflowmap.visuals.VisualAreaMap;
import jflowmap.visuals.VisualFlowMap;

import org.apache.log4j.Logger;

import prefuse.data.Graph;
import prefuse.data.io.DataIOException;
import prefuse.data.io.GraphMLReader;
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

        JPanel bundleButs = new JPanel();
        add(bundleButs, BorderLayout.NORTH);
        
        JButton resetButton = new JButton("Reset");
        bundleButs.add(resetButton);
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                visualFlowMap.resetBundling();
            }
        });
        JButton bundleButton = new JButton("Bundle");
        bundleButs.add(bundleButton);
        bundleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    visualFlowMap.bundleEdges(6);
                } catch (Exception ex) {
                    logger.error("Bundling error", ex);
                    JOptionPane.showMessageDialog(JFlowMap.this,
                            "Bundling couldn't be performed: [" + ex.getClass().getSimpleName()+ "] " + ex.getMessage()
                    );
                }
            }
        });
        JButton bundlingStepButton = new JButton("Bundling cycle");
        bundleButs.add(bundlingStepButton);
        bundlingStepButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    visualFlowMap.bundlingCycle();
                } catch (Exception ex) {
                    logger.error("Bundling error", ex);
                    JOptionPane.showMessageDialog(JFlowMap.this,
                            "Bundling couldn't be performed: [" + ex.getClass().getSimpleName()+ "] " + ex.getMessage()
                    );
                }
            }
        });
        
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
        logger.info("> Loading flow map \"" + dataset + "\"");
        FlowMapParamsModel model = null;
        try {
            Graph graph = loadGraph(dataset.filename);
            model = new FlowMapParamsModel(graph, dataset.valueAttrName, dataset.labelAttrName);
            model.setValueFilterMin(1000);
            VisualFlowMap visualFlowMap = new VisualFlowMap(canvas, graph, model);
            if (dataset.areaMapFilename != null) {
                VisualAreaMap map = loadAreaMap(dataset.areaMapFilename);
                visualFlowMap.addChild(map);
                map.moveToBack();
            }
            return visualFlowMap;
        } catch (DataIOException e) {
            logger.error("Couldn't load flow map " + dataset, e);
            JOptionPane.showMessageDialog(this,  "Couldn't load flow map: [" + e.getClass().getSimpleName()+ "] " + e.getMessage());
        }
        return null;
    }

    private Graph loadGraph(String filename) throws DataIOException {
        logger.info("Loading graph \"" + filename + "\"");
        GraphMLReader reader = new GraphMLReader();
        return reader.readGraph(filename);
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
            new DatasetSpec("data/bundling-test.xml", "data", "name", null),
            new DatasetSpec("data/airlines.xml", "value", "tooltip", null),
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
