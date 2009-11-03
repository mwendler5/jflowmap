package jflowmap;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import jflowmap.data.GraphFileFormats;
import jflowmap.models.FlowMapParamsModel;
import jflowmap.models.map.AreaMap;
import jflowmap.ui.ControlPanel;
import jflowmap.util.GraphStats;
import jflowmap.util.PanHandler;
import jflowmap.util.ZoomHandler;
import jflowmap.visuals.VisualAreaMap;
import jflowmap.visuals.VisualFlowMap;

import org.apache.log4j.Logger;

import prefuse.data.Graph;
import prefuse.data.io.DataIOException;
import edu.umd.cs.piccolo.PCanvas;

/**
 * @author Ilya Boyandin
 *         Date: 23-Sep-2009
 */
public class JFlowMap extends JComponent {

    private static Logger logger = Logger.getLogger(JFlowMap.class);

    private final PCanvas canvas;
    private final ControlPanel controlPanel;
    private VisualFlowMap visualFlowMap;
    private final Frame app;

    public JFlowMap(FlowMapMain app) {
        setLayout(new BorderLayout());

        this.app = app;
        
        canvas = new PCanvas();
        canvas.setBackground(Color.BLACK);
//        canvas.setBackground(Color.WHITE);
//        canvas.addInputEventListener(new ZoomHandler(.5, 50));
        canvas.addInputEventListener(new ZoomHandler());
        canvas.setPanEventHandler(new PanHandler());
        add(canvas, BorderLayout.CENTER);
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                visualFlowMap.fitInCameraView();
            }
        });
        
        visualFlowMap = loadFlowMap(datasetSpecs[0]);
        canvas.getLayer().addChild(visualFlowMap);

        controlPanel = new ControlPanel(this);
        add(controlPanel.getPanel(), BorderLayout.SOUTH);
        
        fitFlowMapInView();
    }

    public void resetBundling() {
        visualFlowMap.resetBundling();
    }

    public Frame getApp() {
        return app;
    }
    
    public VisualFlowMap getVisualFlowMap() {
		return visualFlowMap;
	}

    public void setVisualFlowMap(VisualFlowMap newFlowMap) {
        if (visualFlowMap != null) {
            canvas.getLayer().removeChild(visualFlowMap);
        }
        canvas.getLayer().addChild(newFlowMap);
        visualFlowMap = newFlowMap;
        fitFlowMapInView();
    }

	public void fitFlowMapInView() {
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
            GraphStats stats = makeGraphStats(dataset, graph);
            model = new FlowMapParamsModel(stats, dataset.valueAttrName, 
            		dataset.xNodeAttr, dataset.yNodeAttr, dataset.labelAttrName);
            if (!Double.isNaN(dataset.valueFilterMin)) {
                model.setValueFilterMin(dataset.valueFilterMin);
            }
            VisualFlowMap visualFlowMap = new VisualFlowMap(this, canvas, graph, stats, model);
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

	private GraphStats makeGraphStats(DatasetSpec dataset, Graph graph) {
		return GraphStats.createFor(
				graph, dataset.valueAttrName, dataset.xNodeAttr, dataset.yNodeAttr);
	}

    private Graph loadGraph(String filename) throws DataIOException {
        logger.info("Loading graph \"" + filename + "\"");
        Graph graph = GraphFileFormats.createReaderFor(filename).readGraph(filename);
        logger.info("Loaded graph \"" + filename + "\": " + graph.getNodeCount() + " nodes, " + graph.getEdgeCount() + " edges");
        return graph;
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
        public DatasetSpec(String filename, String valueAttrName, String xNodeAttr, String yNodeAttr,
        		String labelAttrName, String areaMapFilename) {
            this(filename, valueAttrName, xNodeAttr, yNodeAttr, 
            		labelAttrName, areaMapFilename, Double.NaN);
        }
        
        public DatasetSpec(String filename, String valueAttrName,
        		String xNodeAttr, String yNodeAttr, 
        		String labelAttrName, String areaMapFilename, double valueFilterMin) {
            this.filename = filename;
            this.valueAttrName = valueAttrName;
            this.xNodeAttr = xNodeAttr;
            this.yNodeAttr = yNodeAttr;
            this.labelAttrName = labelAttrName;
            this.areaMapFilename = areaMapFilename;
            this.valueFilterMin = valueFilterMin;
        }
        public final String filename;
        public final String areaMapFilename;
        public final String valueAttrName;
        public final String labelAttrName;
        public final String xNodeAttr, yNodeAttr;
        public final double valueFilterMin;

        @Override
        public String toString() {
            return filename;
        }
    }
    
    public static final DatasetSpec[] datasetSpecs = new DatasetSpec[] {
            new DatasetSpec("data/refugees-2008-no-various.xml", "refugees", "x", "y", "name", "data/countries-areas.xml", 1000),
            new DatasetSpec("C:/Data/uni-konstanz/PhotoTrails/sline_test_1.csv", "value", "x", "y", null, null),
            new DatasetSpec("data/migrations-unique.xml", "value", "x", "y", "tooltip", null, 1000),
            new DatasetSpec("data/airlines.xml", "value", "x", "y", "tooltip", null),
//            new DatasetSpec("data/bundling-test2.xml", "data", "name", null),
//            new DatasetSpec("data/bundling-test6.xml", "data", "name", null),
            new DatasetSpec("data/refugees-2008.xml", "refugees", "x", "y", "name", "data/countries-areas.xml", 1000),
            new DatasetSpec("data/refugees-2007.xml", "refugees", "x", "y", "name", "data/countries-areas.xml", 1000),
            new DatasetSpec("data/refugees-2006.xml", "refugees", "x", "y", "name", "data/countries-areas.xml", 1000),
            new DatasetSpec("data/refugees-2005.xml", "refugees", "x", "y", "name", "data/countries-areas.xml", 1000),
            new DatasetSpec("data/refugees-2004.xml", "refugees", "x", "y", "name", "data/countries-areas.xml", 1000),
            new DatasetSpec("data/refugees-2003.xml", "refugees", "x", "y", "name", "data/countries-areas.xml", 1000),
            new DatasetSpec("data/refugees-2002.xml", "refugees", "x", "y", "name", "data/countries-areas.xml", 1000),
            new DatasetSpec("data/refugees-2001.xml", "refugees", "x", "y", "name", "data/countries-areas.xml", 1000),
            new DatasetSpec("data/refugees-2000.xml", "refugees", "x", "y", "name", "data/countries-areas.xml", 1000),
            new DatasetSpec("data/refugees-1999.xml", "refugees", "x", "y", "name", "data/countries-areas.xml"),
            new DatasetSpec("data/refugees-1998.xml", "refugees", "x", "y", "name", "data/countries-areas.xml"),
            new DatasetSpec("data/refugees-1997.xml", "refugees", "x", "y", "name", "data/countries-areas.xml"),
            new DatasetSpec("data/refugees-1996.xml", "refugees", "x", "y", "name", "data/countries-areas.xml"),
            new DatasetSpec("data/refugees-1995.xml", "refugees", "x", "y", "name", "data/countries-areas.xml"),
            new DatasetSpec("data/refugees-1994.xml", "refugees", "x", "y", "name", "data/countries-areas.xml"),
            new DatasetSpec("data/refugees-1993.xml", "refugees", "x", "y", "name", "data/countries-areas.xml"),
            new DatasetSpec("data/refugees-1992.xml", "refugees", "x", "y", "name", "data/countries-areas.xml"),
            new DatasetSpec("data/refugees-1991.xml", "refugees", "x", "y", "name", "data/countries-areas.xml"),
            new DatasetSpec("data/refugees-1990.xml", "refugees", "x", "y", "name", "data/countries-areas.xml"),
            new DatasetSpec("data/refugees-1989.xml", "refugees", "x", "y", "name", "data/countries-areas.xml"),
            new DatasetSpec("data/refugees-1988.xml", "refugees", "x", "y", "name", "data/countries-areas.xml"),
            new DatasetSpec("data/refugees-1987.xml", "refugees", "x", "y", "name", "data/countries-areas.xml"),
            new DatasetSpec("data/refugees-1986.xml", "refugees", "x", "y", "name", "data/countries-areas.xml"),
            new DatasetSpec("data/refugees-1985.xml", "refugees", "x", "y", "name", "data/countries-areas.xml"),
            new DatasetSpec("data/refugees-1984.xml", "refugees", "x", "y", "name", "data/countries-areas.xml"),
            new DatasetSpec("data/refugees-1983.xml", "refugees", "x", "y", "name", "data/countries-areas.xml"),
            new DatasetSpec("data/refugees-1982.xml", "refugees", "x", "y", "name", "data/countries-areas.xml"),
            new DatasetSpec("data/refugees-1981.xml", "refugees", "x", "y", "name", "data/countries-areas.xml"),
            new DatasetSpec("data/refugees-1980.xml", "refugees", "x", "y", "name", "data/countries-areas.xml"),
            new DatasetSpec("data/refugees-1979.xml", "refugees", "x", "y", "name", "data/countries-areas.xml"),
            new DatasetSpec("data/refugees-1978.xml", "refugees", "x", "y", "name", "data/countries-areas.xml"),
            new DatasetSpec("data/refugees-1977.xml", "refugees", "x", "y", "name", "data/countries-areas.xml"),
            new DatasetSpec("data/refugees-1976.xml", "refugees", "x", "y", "name", "data/countries-areas.xml"),
            new DatasetSpec("data/refugees-1975.xml", "refugees", "x", "y", "name", "data/countries-areas.xml"),
    };

}
