package jflowmap.clustering;

import java.util.ArrayList;
import java.util.List;

import jflowmap.util.GeomUtils;
import jflowmap.visuals.VisualEdge;
import jflowmap.visuals.VisualNode;
import ch.unifr.dmlib.cluster.DistanceMeasure;

/**
 * @author Ilya Boyandin
 */
public enum NodeDistanceMeasure implements DistanceMeasure<VisualNode> {

    INCOMING_AND_OUTGOING_EDGES_WITH_WEIGHTS("Incoming and outgoing edges with weights", NodeFilter.IN_OR_OUT) {
        @Override
        public double distance(VisualNode n1, VisualNode n2) {
            return (EdgeCombinations.IN_WITH_WEIGHTS.distance(n1, n2) + EdgeCombinations.OUT_WITH_WEIGHTS.distance(n1, n2)) / 2;
        }
    },
    INCOMING_EDGES_WITH_WEIGHTS("Incoming edges with weights", NodeFilter.IN) {
        @Override
        public double distance(VisualNode n1, VisualNode n2) {
            return EdgeCombinations.IN_WITH_WEIGHTS.distance(n1, n2);
        }
    },
    OUTGOING_EDGES_WITH_WEIGHTS("Outgoing edges with weights", NodeFilter.OUT) {
        @Override
        public double distance(VisualNode n1, VisualNode n2) {
            return EdgeCombinations.OUT_WITH_WEIGHTS.distance(n1, n2);
        }
    },
    INCOMING_AND_OUTGOING_EDGES("Incoming and outgoing edges", NodeFilter.IN_OR_OUT) {
        @Override
        public double distance(VisualNode n1, VisualNode n2) {
            return (EdgeCombinations.IN.distance(n1, n2) + EdgeCombinations.OUT.distance(n1, n2)) / 2;
        }
    },
    INCOMING_EDGES("Incoming edges", NodeFilter.IN) {
        @Override
        public double distance(VisualNode n1, VisualNode n2) {
            return EdgeCombinations.IN.distance(n1, n2);
        }
    },
    OUTGOING_EDGES("Outgoing edges", NodeFilter.OUT) {
        @Override
        public double distance(VisualNode n1, VisualNode n2) {
            return EdgeCombinations.OUT.distance(n1, n2);
        }
    },
//    COSINE_IN("Cosine incoming", NodeFilter.IN) {
//        @Override
//        public double distance(VisualNode t1, VisualNode t2) {
//            return Cosine.IN.distance(t1, t2);
//        }        
//    },
    EUCLIDEAN("Euclidean", NodeFilter.ALL) {
        @Override
        public double distance(VisualNode t1, VisualNode t2) {
            double dx = t1.getValueX() - t2.getValueX();
            double dy = t1.getValueY() - t2.getValueY();
            double dist = Math.sqrt(dx * dx + dy * dy);
            return dist;
        }        
    }
    ;

    public List<VisualNode> filterNodes(List<VisualNode> nodes) {
        return filter.filterNodes(nodes);
    }
    
    private NodeDistanceMeasure(String name, NodeFilter filter) {
        this.name = name;
        this.filter = filter;
    }
    
    private String name;
    private NodeFilter filter;
    
    @Override
    public String toString() {
        return name;
    }
    
    private enum Cosine implements DistanceMeasure<VisualNode> {
        IN(false),
        OUT(true)
        ;

        private boolean incomingNotOutgoing;

        private Cosine(boolean incomingNotOutgoing) {
            this.incomingNotOutgoing = incomingNotOutgoing;
        }
        
        @Override
        public double distance(VisualNode t1, VisualNode t2) {
            int common = 0;
            List<VisualEdge> inc1 = t1.getIncomingEdges();
            List<VisualEdge> inc2 = t2.getIncomingEdges();
            for (VisualEdge e1 : inc1) {
                VisualNode target1 = e1.getTargetNode();
                for (VisualEdge e2 : inc2) {
                    if (target1 == e2.getTargetNode()) {
                        common++;
                    }
                }
            }
            return (double)common / (inc1.size() + inc2.size());
        }
    }

    private enum EdgeCombinations implements DistanceMeasure<VisualNode> {
        IN(true, false),
        OUT(false, false),
        IN_WITH_WEIGHTS(true, true),
        OUT_WITH_WEIGHTS(false, true);

        private boolean useEdgeWeights;
        private boolean incomingNotOutgoing;

        private EdgeCombinations(boolean incomingNotOutgoing, boolean useEdgeWeights) {
            this.useEdgeWeights = useEdgeWeights;
            this.incomingNotOutgoing = incomingNotOutgoing;
        }
        
        @Override
        public double distance(VisualNode n1, VisualNode n2) {
            double numerator = 0;
            double denominator = 0;
//            double denominator = 1.0;
            int count = 0;
            for (VisualEdge e1 : getEdges(n1))
            for (VisualEdge e2 : getEdges(n2)) {
                VisualNode t1 = getOppositeNode(e1);
                VisualNode t2 = getOppositeNode(e2);
                double d = GeomUtils.distance(
                        t1.getValueX(), t1.getValueY(), t2.getValueX(), t2.getValueY());
                if (useEdgeWeights) {
                    double w = e1.getValue() * e2.getValue();
                    numerator += d * w;
                    denominator += w;
                } else {
                    numerator += d;
                    denominator++;
                }
                count++;
            }
            double distance;
            if (count == 0) {
                distance = Double.POSITIVE_INFINITY;
            } else {
                distance = numerator / denominator;
            }
            return distance;
        }

        private List<VisualEdge> getEdges(VisualNode n) {
            if (incomingNotOutgoing) {
                return n.getIncomingEdges();
            } else {
                return n.getOutgoingEdges();
            }
        }

        private VisualNode getOppositeNode(VisualEdge e) {
            if (incomingNotOutgoing) {
                return e.getSourceNode();
            } else {
                return e.getTargetNode();
            }
        }
    }

    
    /**
     * Filters a given list of nodes so that only
     * nodes having in or out edges are left in the list.
     */
    private enum NodeFilter {
        ALL {
            @Override
            protected boolean accept(VisualNode node) {
                return true;
            }
        },
        IN {
            @Override
            protected boolean accept(VisualNode node) {
                return node.getIncomingEdges().size() > 0;
            }
        },
        OUT {
            @Override
            protected boolean accept(VisualNode node) {
                return node.getOutgoingEdges().size() > 0;
            }
        },
        IN_OR_OUT {
            @Override
            protected boolean accept(VisualNode node) {
                return (node.getOutgoingEdges().size() > 0)  ||  (node.getIncomingEdges().size() > 0);
            }
        };
        
        protected abstract boolean accept(VisualNode node);

        public List<VisualNode> filterNodes(List<VisualNode> nodes) {
            List<VisualNode> filtered = new ArrayList<VisualNode>(); 
            for (VisualNode node : nodes) {
                if (accept(node)) {
                    filtered.add(node);
                }
            }
            return filtered;
        }
    }
}
