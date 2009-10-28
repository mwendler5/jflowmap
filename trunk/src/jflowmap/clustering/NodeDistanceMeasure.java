package jflowmap.clustering;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jflowmap.util.GeomUtils;
import jflowmap.visuals.VisualEdge;
import jflowmap.visuals.VisualNode;
import ch.unifr.dmlib.cluster.DistanceMeasure;

import com.google.common.collect.ImmutableSet;

/**
 * @author Ilya Boyandin
 */
public enum NodeDistanceMeasure implements DistanceMeasure<VisualNode> {

    EUCLIDEAN("Euclidean", NodeFilter.ALL) {
        @Override
        public double distance(VisualNode t1, VisualNode t2) {
            double dx = t1.getValueX() - t2.getValueX();
            double dy = t1.getValueY() - t2.getValueY();
            double dist = Math.sqrt(dx * dx + dy * dy);
            return dist;
        }        
    },
    COMMON_EDGES_IN("Common edges: incoming", NodeFilter.IN) {
        @Override
        public double distance(VisualNode t1, VisualNode t2) {
            return CommonEdges.IN.distance(t1, t2);
        }        
    },
    COMMON_EDGES_OUT("Common edges: outgoing", NodeFilter.OUT) {
        @Override
        public double distance(VisualNode t1, VisualNode t2) {
            return CommonEdges.OUT.distance(t1, t2);
        }        
    },
    COMMON_EDGES_WEIGHTED_IN("Common edges weighted: incoming", NodeFilter.IN) {
        @Override
        public double distance(VisualNode t1, VisualNode t2) {
            return Cosine.IN.distance(t1, t2);
        }        
    },
    COMMON_EDGES_WEIGHTED_OUT("Common edges weighted: outgoing", NodeFilter.OUT) {
        @Override
        public double distance(VisualNode t1, VisualNode t2) {
            return Cosine.OUT.distance(t1, t2);
        }        
    },
    INCOMING_AND_OUTGOING_EDGES_WITH_WEIGHTS("Edge barycenter: Incoming and outgoing with weights", NodeFilter.IN_OR_OUT) {
        @Override
        public double distance(VisualNode n1, VisualNode n2) {
            return (EdgeCombinations.IN_WITH_WEIGHTS.distance(n1, n2) + EdgeCombinations.OUT_WITH_WEIGHTS.distance(n1, n2)) / 2;
        }
    },
    INCOMING_EDGES_WITH_WEIGHTS("Edge barycenter: Incoming with weights", NodeFilter.IN) {
        @Override
        public double distance(VisualNode n1, VisualNode n2) {
            return EdgeCombinations.IN_WITH_WEIGHTS.distance(n1, n2);
        }
    },
    OUTGOING_EDGES_WITH_WEIGHTS("Edge barycenter: Outgoing with weights", NodeFilter.OUT) {
        @Override
        public double distance(VisualNode n1, VisualNode n2) {
            return EdgeCombinations.OUT_WITH_WEIGHTS.distance(n1, n2);
        }
    },
    INCOMING_AND_OUTGOING_EDGES("Edge barycenter: Incoming and outgoing", NodeFilter.IN_OR_OUT) {
        @Override
        public double distance(VisualNode n1, VisualNode n2) {
            return (EdgeCombinations.IN.distance(n1, n2) + EdgeCombinations.OUT.distance(n1, n2)) / 2;
        }
    },
    INCOMING_EDGES("Edge barycenter: Incoming", NodeFilter.IN) {
        @Override
        public double distance(VisualNode n1, VisualNode n2) {
            return EdgeCombinations.IN.distance(n1, n2);
        }
    },
    OUTGOING_EDGES("Edge barycenter: Outgoing", NodeFilter.OUT) {
        @Override
        public double distance(VisualNode n1, VisualNode n2) {
            return EdgeCombinations.OUT.distance(n1, n2);
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
    
    private enum CommonEdges implements DistanceMeasure<VisualNode> {
        IN(true),
        OUT(false)
        ;
        
        private boolean incomingNotOutgoing;
        
        private CommonEdges(boolean incomingNotOutgoing) {
            this.incomingNotOutgoing = incomingNotOutgoing;
        }
        
        @Override
        public double distance(VisualNode node1, VisualNode node2) {
            List<VisualNode> oppositeNodes1 = node1.getOppositeNodes(incomingNotOutgoing);
            List<VisualNode> oppositeNodes2 = node2.getOppositeNodes(incomingNotOutgoing);
            
            int intersectionSize = 0;
            for (VisualNode node : oppositeNodes1) {
                if (oppositeNodes2.contains(node)) {
                    intersectionSize++;
                }
            }
            int unionSize = oppositeNodes1.size() + oppositeNodes2.size() - intersectionSize;
            
            double dist = 1.0 - (double)intersectionSize / unionSize;
            
//            if (dist < .16) System.out.println(
//                    node1.getLabel() + " - " + node2.getLabel() + ": common = " + intersectionSize + " of " +
//                    unionSize + ", dist = " + dist);
            
            return dist;
        }
    }
    
    
    /**
     * See http://www.miislita.com/information-retrieval-tutorial/cosine-similarity-tutorial.html
     * @author Ilya Boyandin
     */
    private enum Cosine implements DistanceMeasure<VisualNode> {
        IN(true),
        OUT(false)
        ;

        private boolean useEdgeWeights;
        private boolean incomingNotOutgoing;

        private Cosine(boolean incomingNotOutgoing) {
            this.incomingNotOutgoing = incomingNotOutgoing;
        }
        
        @Override
        public double distance(VisualNode node1, VisualNode node2) {
            Set<VisualEdge> edges1;
            Set<VisualEdge> edges2;
            if (incomingNotOutgoing) {
                edges1 = ImmutableSet.copyOf(node1.getIncomingEdges());
                edges2 = ImmutableSet.copyOf(node2.getIncomingEdges());
            } else {
                edges1 = ImmutableSet.copyOf(node1.getOutgoingEdges());
                edges2 = ImmutableSet.copyOf(node2.getOutgoingEdges());
            }
            
            double dist = 0;
            
            /*
            int intersectionSize = 0;
            double weightedIntersectionSize = 0;
            List<VisualEdge> edges1;
            List<VisualEdge> edges2;
            if (incomingNotOutgoing) {
                edges1 = node1.getIncomingEdges();
                edges2 = node2.getIncomingEdges();
            } else {
                edges1 = node1.getOutgoingEdges();
                edges2 = node2.getOutgoingEdges();
            }
            for (VisualEdge edge1 : edges1) {
                VisualNode opposite1;
                if (incomingNotOutgoing) {
                    opposite1 = edge1.getSourceNode();
                } else {
                    opposite1 = edge1.getTargetNode();
                }
                for (VisualEdge edge2 : edges2) {
                    VisualNode opposite2;
                    if (incomingNotOutgoing) {
                        opposite2 = edge2.getSourceNode();
                    } else {
                        opposite2 = edge2.getTargetNode();
                    }
                    if (opposite1 == opposite2) {
                        intersectionSize++;
                        double value2 = edge2.getValue();
                        double value1 = edge1.getValue();
                        weightedIntersectionSize += (value1 > value2 ? value2 / value1 : value1 / value2);
                        break;
                    }
                }
            }
            int unionSize = edges1.size() + edges2.size() - intersectionSize;
            
            double dist = 1.0 - (double)weightedIntersectionSize / unionSize;
            */
            return dist;
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
