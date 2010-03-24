/*
 * This file is part of JFlowMap.
 *
 * Copyright 2009 Ilya Boyandin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jflowmap;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;

import jflowmap.data.FlowMapLoader;
import jflowmap.data.FlowMapStats;
import jflowmap.data.XmlRegionsReader;
import jflowmap.util.PanHandler;
import jflowmap.util.ZoomHandler;
import jflowmap.visuals.timeline.VisualFlowTimeline;

import org.apache.log4j.Logger;

import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.util.ColorLib;
import at.fhj.utils.swing.JMsgPane;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PCanvas;

/**
 * @author Ilya Boyandin
 */
public class JFlowTimeline extends JComponent {
    public static final String NODE_COLUMN__REGION = "region";
    public static final String NODE_COLUMN__REGION_COLOR = "regionColor";

    public static Logger logger = Logger.getLogger(JFlowTimeline.class);

    public static final Color CANVAS_BACKGROUND_COLOR =
        Color.white;
//        new Color(47, 89, 134);
    private final PCanvas canvas;
    private final VisualFlowTimeline visualTimeline;
    private final FlowMapStats globalStats;
    private Map<String, String> countryToRegion;

    public JFlowTimeline(Iterable<Graph> graphs, FlowMapAttrsSpec attrSpec) {
        setLayout(new BorderLayout());

        canvas = new PCanvas();
        canvas.setBackground(CANVAS_BACKGROUND_COLOR);
//        canvas.setBackground(colorScheme.get(ColorCodes.BACKGROUND));
        canvas.addInputEventListener(new ZoomHandler());
        canvas.setPanEventHandler(new PanHandler());
        add(canvas, BorderLayout.CENTER);


        // TODO: introduce regions as node attrs in GraphML
        try {
            countryToRegion = XmlRegionsReader.readFrom("data/refugees/regions.xml");

            HashSet<String> regions = Sets.newHashSet(countryToRegion.values());
            int[] palette = ColorLib.getCategoryPalette(regions.size(), 1.f, 0.4f, 1.f, .15f);

            int colorIdx = 0;
            Map<String, Integer> regionToColor = Maps.newHashMap();
            for (String region : regions) {
                regionToColor.put(region, palette[colorIdx]);
                colorIdx++;
            }


            for (Graph graph : graphs) {
                graph.getNodeTable().addColumn(NODE_COLUMN__REGION, String.class);
                graph.getNodeTable().addColumn(NODE_COLUMN__REGION_COLOR, int.class);

                for (Map.Entry<String, String> e : countryToRegion.entrySet()) {
                    Node node = FlowMapLoader.findNodeById(graph, e.getKey());
                    if (node != null) {
                        String region = e.getValue();
                        node.set(NODE_COLUMN__REGION, region);
                        node.setInt(NODE_COLUMN__REGION_COLOR, regionToColor.get(region));
                    }
                }
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            JMsgPane.showErrorDialog(this, e.getClass().getSimpleName() + ": " + e.getMessage());
        }


        List<FlowMapGraphWithAttrSpecs> graphsAndSpecs = Lists.newArrayList();
        for (Graph graph : graphs) {
            FlowMapGraphWithAttrSpecs gs = new FlowMapGraphWithAttrSpecs(graph, attrSpec);
            FlowMapStats.supplyNodesWithStats(gs);
            graphsAndSpecs.add(gs);
        }

        FlowMapStats.supplyNodesWithDiffStats(graphs, attrSpec);

        globalStats = FlowMapStats.createFor(graphsAndSpecs);

        visualTimeline = new VisualFlowTimeline(this, graphs, attrSpec);
        canvas.getLayer().addChild(visualTimeline);

    }

    public FlowMapStats getGlobalStats() {
        return globalStats;
    }

    public PCanvas getCanvas() {
        return canvas;
    }

    public PCamera getCamera() {
        return canvas.getCamera();
    }

    public Map<String, String> getRegions() {
        return countryToRegion;
    }

}