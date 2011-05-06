package jflowmap.views.flowstrates;

import jflowmap.FlowMapGraph;
import jflowmap.FlowMapGraphAggLayers;
import jflowmap.FlowMapGraphAggLayers.Builder;
import jflowmap.FlowEndpoint;
import jflowmap.data.FlowMapGraphEdgeAggregator.AggEntity;
import jflowmap.data.FlowMapGraphEdgeAggregator.GroupFunctions;
import jflowmap.data.FlowMapGraphEdgeAggregator.ValueAggregator;
import prefuse.data.Tuple;

/**
 * @author Ilya Boyandin
 */
public class DefaultAggLayersBuilder implements AggLayersBuilder {

  @Override
  public FlowMapGraphAggLayers build(FlowMapGraph flowMapGraph) {
    return createBuilder(flowMapGraph).build(null);
  }

  protected Builder createBuilder(FlowMapGraph flowMapGraph) {
    FlowMapGraphAggLayers.Builder builder = new FlowMapGraphAggLayers.Builder(
        "<No grouping>", flowMapGraph);

//    builder.addAggregationLayer("Origin", null, FlowMapGraphEdgeAggregator.GroupFunctions.SRC_NODE);
//    builder.addAggregationLayer("Dest", null, FlowMapGraphEdgeAggregator.GroupFunctions.TARGET_NODE);

    String labelAttr = flowMapGraph.getNodeLabelAttr();
    builder.addAggregationLayer("Origin", null,
        builder.edgeAggregatorFor(GroupFunctions.SRC_NODE, null)
          .withCustomValueAggregator(
              labelAttr,
              oneSideNodeLabelsAggregator(FlowEndpoint.ORIGIN, labelAttr, "ALL"))
          );

    builder.addAggregationLayer("Dest", null,
        builder.edgeAggregatorFor(GroupFunctions.TARGET_NODE, null)
          .withCustomValueAggregator(
              labelAttr,
              oneSideNodeLabelsAggregator(FlowEndpoint.DEST, labelAttr, "ALL"))
          );

    return builder;
  }

  protected static ValueAggregator createAllForAllLabelsAggregator() {
    return new ValueAggregator() {
      @Override
      public Object aggregate(Iterable<Object> values, Iterable<Tuple> tuples, AggEntity entity) {
        return "ALL";
      }
    };
  }

  protected static ValueAggregator oneSideNodeLabelsAggregator(FlowEndpoint s, final String attr,
      final String aggLabel) {
    final AggEntity ae;
    switch (s) {
      case ORIGIN: ae = AggEntity.SOURCE_NODE; break;
      case DEST: ae = AggEntity.TARGET_NODE; break;
      default: throw new AssertionError();
    }
    return new ValueAggregator() {
      @Override
      public Object aggregate(Iterable<Object> values, Iterable<Tuple> tuples, AggEntity entity) {
        if (entity == ae) {
          return tuples.iterator().next().get(attr);
        } else {
          return aggLabel;
        }
      }
    };
  }


}