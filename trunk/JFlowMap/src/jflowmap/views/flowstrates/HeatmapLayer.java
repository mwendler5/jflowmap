/*
 * This file is part of JFlowMap.
 *
 * Copyright 2009 Ilya Boyandin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jflowmap.views.flowstrates;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Map;

import jflowmap.FlowEndpoint;
import jflowmap.FlowMapGraph;
import jflowmap.data.SeqStat;
import jflowmap.geom.GeomUtils;
import jflowmap.util.Pair;
import jflowmap.util.piccolo.PLabel;
import jflowmap.util.piccolo.PNodes;
import jflowmap.util.piccolo.PPaths;
import jflowmap.util.piccolo.PTypedBasicInputEventHandler;
import prefuse.data.Edge;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventListener;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PBounds;

/**
 * @author Ilya Boyandin
 */
public class HeatmapLayer extends TemporalViewLayer {

  static final double cellWidth = 40;
  static final double cellHeight = 40;
  private static final Font HEATMAP_ROW_LABELS_FONT = new Font("Arial", Font.PLAIN, 22 /*18*/);
  private static final Font HEATMAP_COLUMN_LABELS_FONT = new Font("Arial", Font.PLAIN, 25 /*19*/);

  private final PInputEventListener heatmapCellTooltipListener;
  private final PInputEventListener heatmapCellHoverListener;

  private final PPath columnHighlightRect;
  private final PNode heatmapNode;
  private final Map<Edge, Pair<PText, PText>> edgesToLabels = Maps.newHashMap();
  private SeqStat weightAttrTotalsStat = null;

  public HeatmapLayer(FlowstratesView flowstratesView) {
    super(flowstratesView);

    heatmapNode = new PNode();
    addChild(heatmapNode);

    columnHighlightRect = PPaths.rect(0, 0, 1, 1);
    columnHighlightRect.setPaint(null);
    FlowstratesStyle style = getFlowstratesView().getStyle();
    columnHighlightRect.setStrokePaint(style.getHeatmapSelectedCellStrokeColor());
    columnHighlightRect.setStroke(style.getSelectedTimelineCellStroke());
    columnHighlightRect.setVisible(false);
    addChild(columnHighlightRect);

    heatmapCellTooltipListener = getFlowstratesView().createTooltipListener(HeatmapCell.class);
    heatmapCellHoverListener = createHeatMapCellHoverListener();
  }

  public FlowMapGraph getFlowMapGraph() {
    return getFlowstratesView().getFlowMapGraph();
  }

  @Override
  public Rectangle2D getEdgeLabelBounds(Edge edge, FlowEndpoint ep) {
    Pair<PText, PText> labels = edgesToLabels.get(edge);
    switch (ep) {
    case ORIGIN: return labels.first().getBounds();
    case DEST: return labels.second().getBounds();
    default: throw new AssertionError();
    }
  }

  private void createColumnLabels() {
    List<String> attrNames = getFlowMapGraph().getEdgeWeightAttrs();
    // String cp = StringUtils.getCommonPrefix(attrNames.toArray(new String[attrNames.size()]));
    int col = 0;
    for (String attr : attrNames) {
      // attr = attr.substring(cp.length());
      PLabel label = new PLabel(attr);
      label.setName(attr);
      label.setFont(HEATMAP_COLUMN_LABELS_FONT);
      PBounds b = label.getFullBoundsReference();
      double x = col * cellWidth; // + (cellWidth - b.getWidth()) / 2;
      double y = -b.getHeight() / 1.5;
      label.setPaint(Color.white);
      label.rotateAboutPoint(-Math.PI * .65 / 2, x, y);
      label.setX(x);
      label.setY(y);
//      label.setX(5 + col * 6.3);
//      label.setY(col * cellWidth + (cellWidth - b.getWidth()) / 2);
//      label.translate(5, col * cellWidth + (cellWidth - b.getWidth()) / 2);
      heatmapNode.addChild(label);

      label.addInputEventListener(new PBasicInputEventHandler() {
        @Override
        public void mouseEntered(PInputEvent event) {
          PLabel label = PNodes.getAncestorOfType(event.getPickedNode(), PLabel.class);
          label.moveToFront();
          final String attr = label.getName();

          Iterable<HeatmapCell> cells = getHeatMapColumnCells(attr);
          updateMapsOnHeatmapColumnHover(attr, true);

          columnHighlightRect.setBounds(
              GeomUtils.growRect(PNodes.fullBoundsOf(cells), 2));
          columnHighlightRect.moveToFront();
          columnHighlightRect.setVisible(true);
          columnHighlightRect.repaint();

          getFlowstratesView().getFlowLinesLayerNode().hideAllFlowLines();
        }

        @Override
        public void mouseExited(PInputEvent event) {
          PLabel label = PNodes.getAncestorOfType(event.getPickedNode(), PLabel.class);
          columnHighlightRect.setVisible(false);
          updateMapsOnHeatmapColumnHover(label.getName(), false);

          getFlowstratesView().getFlowLinesLayerNode().updateFlowLines();
        }
      });
      col++;
    }
  }

  /**
   * @return The point in the heatmap camera view coords.
   */
  @Override
  public Point2D.Double getFlowLineInPoint(int row, FlowEndpoint ep) {
    switch (ep) {

    case ORIGIN:
      return new Point2D.Double(-10, getTupleY(row) + HeatmapLayer.cellHeight / 2);

    case DEST:
      int numCols = getFlowMapGraph().getEdgeWeightAttrsCount();
      return new Point2D.Double(
          10 + HeatmapLayer.cellWidth * numCols, getTupleY(row) + HeatmapLayer.cellHeight / 2);

    default:
      throw new AssertionError();
    }
  }

  double getTupleY(int row) {
    return row * HeatmapLayer.cellHeight;
  }

  @Override
  public void renew() {
    resetWeightAttrTotals();

    heatmapNode.removeAllChildren();

    int row = 0, maxCol = 0;

    edgesToLabels.clear();

    for (Edge edge : getFlowstratesView().getVisibleEdges()) {
      int col = 0;

      double y = getTupleY(row);

      // "from" label
      PText srcLabel = new PText(getFlowMapGraph().getNodeLabel(edge.getSourceNode()));
      srcLabel.setFont(HEATMAP_ROW_LABELS_FONT);
      srcLabel.setX(-srcLabel.getFullBoundsReference().getWidth() - 6);
      srcLabel.setY(y + (cellHeight - srcLabel.getFullBoundsReference().getHeight()) / 2);
      heatmapNode.addChild(srcLabel);

      // "value" box node
      for (String weightAttr : getFlowMapGraph().getEdgeWeightAttrs()) {
        double x = col * cellWidth;

        HeatmapCell cell = new HeatmapCell(
            this, x, y, cellWidth, cellHeight, weightAttr,
            getFlowstratesView().getAggLayers().getFlowMapGraphOf(edge), edge);

        cell.addInputEventListener(heatmapCellHoverListener);
        // if (!Double.isNaN(cell.getWeight())) {
        cell.addInputEventListener(heatmapCellTooltipListener);
        // }
        heatmapNode.addChild(cell);

        col++;
        if (col > maxCol)
          maxCol = col;
      }

      // "to" label
      PText targetLabel = new PText(getFlowMapGraph().getNodeLabel(edge.getTargetNode()));
      targetLabel.setFont(HEATMAP_ROW_LABELS_FONT);
      targetLabel.setX(cellWidth * maxCol + 6);
      targetLabel.setY(y + (cellHeight - targetLabel.getFullBoundsReference().getHeight()) / 2);
      heatmapNode.addChild(targetLabel);

      edgesToLabels.put(edge, Pair.of(srcLabel, targetLabel));

      row++;
    }

    createColumnLabels();

    repaint();
  }

  @Override
  public void resetWeightAttrTotals() {
    weightAttrTotalsStat = null;
  }

  private Iterable<HeatmapCell> getHeatMapColumnCells(final String attr) {
    return
      Iterables.filter(
        PNodes.childrenOfType(heatmapNode, HeatmapCell.class),
        new Predicate<HeatmapCell>() {
          @Override
          public boolean apply(HeatmapCell cell) {
            return attr.equals(cell.getWeightAttr());
          }
        });
  }

  @Override
  public void updateColors() {
    for (HeatmapCell cell : PNodes.childrenOfType(heatmapNode, HeatmapCell.class)) {
      cell.updateColor();
    }
  }

  @Override
  public void fitInView() {
    fitBoundsInCameraView(heatmapNode.getFullBounds(), getCamera());
  }

  public static void fitBoundsInCameraView(PBounds bounds, PCamera camera) {
    if (bounds.height > bounds.width * 10) {
      PBounds camb = camera.getViewBounds();
      bounds.height = bounds.width * (camb.height / camb.width);
    }
    camera.setViewBounds(GeomUtils.growRectByRelativeSize(bounds, .025, .1, .025, .1));
  }


  void updateMapsOnHeatmapCellHover(HeatmapCell cell, boolean hover) {
    MapLayer originMap = getFlowstratesView().getMapLayer(FlowEndpoint.ORIGIN);
    MapLayer destMap = getFlowstratesView().getMapLayer(FlowEndpoint.DEST);

    originMap.updateMapAreaColorsOnHeatmapCellHover(cell, hover);
    destMap.updateMapAreaColorsOnHeatmapCellHover(cell, hover);

    originMap.setEdgeCentroidsHighlighted(cell, hover);
    destMap.setEdgeCentroidsHighlighted(cell, hover);
  }

  void updateMapsOnHeatmapColumnHover(String columnAttr, boolean hover) {
    MapLayer originMap = getFlowstratesView().getMapLayer(FlowEndpoint.ORIGIN);
    MapLayer destMap = getFlowstratesView().getMapLayer(FlowEndpoint.DEST);

    List<Edge> edges = getFlowstratesView().getVisibleEdges();

    SeqStat wstat = getFlowstratesView().getValueStat();

    if (hover) {
      if (weightAttrTotalsStat == null) {
         for (String attr : getFlowMapGraph().getEdgeWeightAttrs()) {
          // "merge" the value stats with the max value of the sums, to construct a color
          // scale in which we can represent the totals for the nodes
          wstat = wstat
              .mergeWith(originMap.calcNodeTotalsFor(edges, attr).values())
              .mergeWith(destMap.calcNodeTotalsFor(edges, attr).values());
        }
        weightAttrTotalsStat = wstat;
      }
      getFlowstratesView().setValueStat(weightAttrTotalsStat);
    } else {
      getFlowstratesView().resetValueStat();
    }


    originMap.updateOnHeatmapColumnHover(columnAttr, hover);
    destMap.updateOnHeatmapColumnHover(columnAttr, hover);

    //updateHeatmapColors();
  }

  PTypedBasicInputEventHandler<HeatmapCell> createHeatMapCellHoverListener() {
    return new PTypedBasicInputEventHandler<HeatmapCell>(HeatmapCell.class) {
      @Override
      public void mouseEntered(PInputEvent event) {
        HeatmapCell cell = node(event);

        FlowstratesStyle style = getFlowstratesView().getStyle();

        // highlight cell
        cell.moveToFront();
        cell.setStroke(style.getSelectedTimelineCellStroke());
        cell.setStrokePaint(style.getHeatmapSelectedCellStrokeColor());

        getFlowstratesView().getFlowLinesLayerNode().setFlowLinesOfEdgeHighlighted(cell.getEdge(), true);

        updateMapsOnHeatmapCellHover(cell, true);
      }

      @Override
      public void mouseExited(PInputEvent event) {
        HeatmapCell cell = node(event);
        FlowstratesStyle style = getFlowstratesView().getStyle();

        cell.setStroke(style.getTimelineCellStroke());
        cell.setStrokePaint(style.getTimelineCellStrokeColor());

        getFlowstratesView().getFlowLinesLayerNode().setFlowLinesOfEdgeHighlighted(cell.getEdge(), false);

        updateMapsOnHeatmapCellHover(cell, false);
      }


      @Override
      public void mouseClicked(PInputEvent event) {
//        if (event.isControlDown()) {
//          getFlowstratesView().setEgdeForSimilaritySorting(node(event).getEdge());
//        }
        HeatmapCell cell = node(event);
        Edge edge = cell.getEdge();
        FlowMapGraph fmg = cell.getFlowMapGraph();

        String srcId = fmg.getSourceNodeId(edge);
        String targetId = fmg.getTargetNodeId(edge);

        getFlowstratesView().getMapLayer(FlowEndpoint.ORIGIN).focusOnNode(srcId);
        getFlowstratesView().getMapLayer(FlowEndpoint.DEST).focusOnNode(targetId);
      }
    };
  }

}
