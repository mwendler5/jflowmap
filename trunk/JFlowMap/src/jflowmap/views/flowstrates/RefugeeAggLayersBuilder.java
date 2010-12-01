package jflowmap.views.flowstrates;

import jflowmap.FlowMapGraph;
import jflowmap.FlowMapGraphAggLayers;
import jflowmap.NodeEdgePos;
import jflowmap.FlowMapGraphAggLayers.Builder;
import jflowmap.data.FlowMapGraphEdgeAggregator;
import prefuse.data.Edge;

import com.google.common.base.Function;

/**
 * @author Ilya Boyandin
 */
public class RefugeeAggLayersBuilder extends DefaultAggLayersBuilder {

  private RefugeeAggLayersBuilder() {
  }

  @Override
  protected FlowMapGraphAggLayers.Builder createBuilder(FlowMapGraph flowMapGraph) {
      // aggregate by source node
      // flowMapGraph = new FlowMapGraphEdgeAggregator(flowMapGraph,
      // // FlowMapGraphEdgeAggregator.GroupFunctions.SRC_NODE)
      // FlowMapGraphEdgeAggregator.GroupFunctions.TARGET_NODE)
      // .withCustomValueAggregator("lat", ValueAggregators.DOUBLE_AVERAGE)
      // .withCustomValueAggregator("lon", ValueAggregators.DOUBLE_AVERAGE)
      // .withCustomValueAggregator("name", ValueAggregators.STRING_ONE_OR_NONE)
      // .aggregate();

    String labelAttr = flowMapGraph.getNodeLabelAttr();
    Builder builder = super.createBuilder(flowMapGraph);

      builder.addAggregationLayer("Origin/Subregion", null,
          builder.edgeAggregatorFor(new Function<Edge, Object>() {
            @Override
            public Object apply(Edge edge) {
              return edge.getSourceNode().getString("region2");
            }
          }, null)
          .withCustomValueAggregator(
              labelAttr,
              oneSideNodeLabelsAggregator(NodeEdgePos.SOURCE, "region2", "ALL"))
          );

      builder.addAggregationLayer("Origin/Region", null,
          builder.edgeAggregatorFor(new Function<Edge, Object>() {
            @Override
            public Object apply(Edge edge) {
              return edge.getSourceNode().getString("region1");
            }
          }, null)
          .withCustomValueAggregator(
              labelAttr,
              oneSideNodeLabelsAggregator(NodeEdgePos.SOURCE, "region1", "ALL"))
          );


      builder.addAggregationLayer("Dest/Subregion", null,
          builder.edgeAggregatorFor(new Function<Edge, Object>() {
            @Override
            public Object apply(Edge edge) {
              return edge.getTargetNode().getString("region2");
            }
          }, null)
          .withCustomValueAggregator(
              labelAttr,
              oneSideNodeLabelsAggregator(NodeEdgePos.TARGET, "region2", "ALL"))
          );


      builder.addAggregationLayer("Dest/Region", null,
          builder.edgeAggregatorFor(new Function<Edge, Object>() {
            @Override
            public Object apply(Edge edge) {
              return edge.getTargetNode().getString("region1");
            }
          }, null)
          .withCustomValueAggregator(
              labelAttr,
              oneSideNodeLabelsAggregator(NodeEdgePos.TARGET, "region1", "ALL"))
          );


      builder.addAggregationLayer("Origin/All", "Origin",
          builder.edgeAggregatorFor(FlowMapGraphEdgeAggregator.GroupFunctions.MERGE_ALL, "Origin")
              .withCustomValueAggregator(
                  labelAttr,
                  createAllForAllLabelsAggregator()));

  //    builder.addAggregationLayer("Dest/All", "Dest",
  //        builder.edgeAggregatorFor(FlowMapGraphEdgeAggregator.GroupFunctions.MERGE_ALL, "Dest")
  //            .withCustomValueAggregator(flowMapGraph.getNodeLabelAttr(), labelForAll));

//      FlowMapGraphAggLayers layers =
//        builder.build(null);
  //      builder.build("Origin/All");
  //      builder.build("Origin/Subregion");


      return builder;
    }


}