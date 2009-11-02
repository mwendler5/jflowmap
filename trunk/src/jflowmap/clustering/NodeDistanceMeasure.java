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

    EUCLIDEAN("Euclidean", NodeFilter.ALL) {
        public double distance(VisualNode t1, VisualNode t2) {
            double dx = t1.getValueX() - t2.getValueX();
            double dy = t1.getValueY() - t2.getValueY();
            double dist = Math.sqrt(dx * dx + dy * dy);
            return dist;
        }        
    },
    COSINE_IN("Cosine: incoming", NodeFilter.IN) {
        public double distance(VisualNode t1, VisualNode t2) {
            return Cosine.IN.distance(t1, t2);
        }        
    },
    COSINE_OUT("Cosine: outgoing", NodeFilter.OUT) {
        public double distance(VisualNode t1, VisualNode t2) {
            return Cosine.OUT.distance(t1, t2);
        }        
    },
    COSINE_IN_OUT("Cosine: incoming and outgoing", NodeFilter.IN_OR_OUT) {
        public double distance(VisualNode t1, VisualNode t2) {
            return Cosine.IN_AND_OUT.distance(t1, t2);
        }        
    },
//    COSINE_WITH_NODE_PROXIMITY_IN("Cosine with node proximity: incoming", NodeFilter.IN) {
//        public double distance(VisualNode t1, VisualNode t2) {
//            return CosineWithNodeProximity.IN.distance(t1, t2);
//        }        
//    },
//    COSINE_WITH_NODE_PROXIMITY_OUT("Cosine with node proximity: outgoing", NodeFilter.OUT) {
//        public double distance(VisualNode t1, VisualNode t2) {
//            return CosineWithNodeProximity.OUT.distance(t1, t2);
//        }        
//    },
//    COSINE_WITH_NODE_PROXIMITY_IN_OUT("Cosine with node proximity: incoming and outgoing", NodeFilter.IN_OR_OUT) {
//        public double distance(VisualNode t1, VisualNode t2) {
//            return CosineWithNodeProximity.IN_AND_OUT.distance(t1, t2);
//        }        
//    },
    COMMON_EDGES_IN("Common edges: incoming", NodeFilter.IN) {
        public double distance(VisualNode t1, VisualNode t2) {
            return CommonEdges.IN.distance(t1, t2);
        }        
    },
    COMMON_EDGES_OUT("Common edges: outgoing", NodeFilter.OUT) {
        public double distance(VisualNode t1, VisualNode t2) {
            return CommonEdges.OUT.distance(t1, t2);
        }        
    },
//    COMMON_EDGES_WEIGHTED_IN("Common edges weighted: incoming", NodeFilter.IN) {
//        public double distance(VisualNode t1, VisualNode t2) {
//            return CommonEdges.IN_PRECISE.distance(t1, t2);
//        }        
//    },
//    COMMON_EDGES_WEIGHTED_OUT("Common edges weighted: outgoing", NodeFilter.OUT) {
//        public double distance(VisualNode t1, VisualNode t2) {
//            return CommonEdges.OUT_PRECISE.distance(t1, t2);
//        }        
//    },
    INCOMING_AND_OUTGOING_EDGES_WITH_WEIGHTS("Edge barycenter: Incoming and outgoing with weights", NodeFilter.IN_OR_OUT) {
        public double distance(VisualNode n1, VisualNode n2) {
            return (EdgeCombinations.IN_WITH_WEIGHTS.distance(n1, n2) + EdgeCombinations.OUT_WITH_WEIGHTS.distance(n1, n2)) / 2;
        }
    },
    INCOMING_EDGES_WITH_WEIGHTS("Edge barycenter: Incoming with weights", NodeFilter.IN) {
        public double distance(VisualNode n1, VisualNode n2) {
            return EdgeCombinations.IN_WITH_WEIGHTS.distance(n1, n2);
        }
    },
    OUTGOING_EDGES_WITH_WEIGHTS("Edge barycenter: Outgoing with weights", NodeFilter.OUT) {
        public double distance(VisualNode n1, VisualNode n2) {
            return EdgeCombinations.OUT_WITH_WEIGHTS.distance(n1, n2);
        }
    },
    INCOMING_AND_OUTGOING_EDGES("Edge barycenter: Incoming and outgoing", NodeFilter.IN_OR_OUT) {
        public double distance(VisualNode n1, VisualNode n2) {
            return (EdgeCombinations.IN.distance(n1, n2) + EdgeCombinations.OUT.distance(n1, n2)) / 2;
        }
    },
    INCOMING_EDGES("Edge barycenter: Incoming", NodeFilter.IN) {
        public double distance(VisualNode n1, VisualNode n2) {
            return EdgeCombinations.IN.distance(n1, n2);
        }
    },
    OUTGOING_EDGES("Edge barycenter: Outgoing", NodeFilter.OUT) {
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
        IN(true, false),
        OUT(false, true),
        IN_AND_OUT(true, true),
        ;

        private boolean includeIncoming;
        private boolean includeOutgoing;

        private Cosine(boolean incoming, boolean outgoing) {
            this.includeIncoming = incoming;
            this.includeOutgoing = outgoing;
        }

        private double valueSquareSum(VisualNode node, boolean incoming) {
            double sum = 0;
            for (VisualEdge e : node.getEdges(incoming)) {
                double v = e.getValue();
                sum += v * v;
            }
            return sum;
        }
        
        public double distance(VisualNode node1, VisualNode node2) {
            double numerator = 0;
            if (includeIncoming) {
                numerator += valueProductsSum(node1, node2, true);
            }
            if (includeOutgoing) {
                numerator += valueProductsSum(node1, node2, false);
            }

            double denominator = 0;
            double denomSum1 = 0;
            double denomSum2 = 0;
            if (includeIncoming) {
                denomSum1 += valueSquareSum(node1, true);
                denomSum2 += valueSquareSum(node2, true);
            }
            if (includeOutgoing) {
                denomSum1 += valueSquareSum(node1, false);
                denomSum2 += valueSquareSum(node2, false);
            }
            denominator = Math.sqrt(denomSum1) * Math.sqrt(denomSum2);
            
            double similarity = numerator / denominator;
            return 1.0 - similarity;
        }

        private double valueProductsSum(VisualNode node1, VisualNode node2, boolean incoming) {
            double sum = 0;
            List<VisualEdge> edges1 = node1.getEdges(incoming);
            for (VisualEdge edge1 : edges1) {
                VisualEdge matchingEdge = findMatchingEdge(edge1, node2, incoming);
                if (matchingEdge != null) {
                    sum += edge1.getValue() * matchingEdge.getValue();
                }
            }
            return sum;
        }

        /**
         * Finds an incoming or outgoing (depending on the incoming parameter) edge of 
         * node2 which matches the given edge1. Meaning that the returned edge goes from/to
         * the same node as edge1. 
         */
        private VisualEdge findMatchingEdge(VisualEdge edge1, VisualNode node2, boolean incoming) {
            VisualEdge matchingEdge = null;
            VisualNode opposite1 = edge1.getNode(incoming ? true : false);    // source if incoming, target if outgoing
            // find an edge 
            for (VisualEdge edge2 : node2.getEdges(incoming)) {
                VisualNode opposite2 = edge2.getOppositeNode(node2);
                if (opposite1 == opposite2) {
                    matchingEdge = edge2;
                    break;
                }
            }
            return matchingEdge;
        }
    }

    
    private enum CosineWithNodeProximity implements DistanceMeasure<VisualNode> {
        IN(true, false),
        OUT(false, true),
        IN_AND_OUT(true, true),
        ;

        private boolean includeIncoming;
        private boolean includeOutgoing;

        private CosineWithNodeProximity(boolean incoming, boolean outgoing) {
            this.includeIncoming = incoming;
            this.includeOutgoing = outgoing;
        }
        
        public double distance(VisualNode node1, VisualNode node2) {
            double numerator = 0;
            if (includeIncoming) {
                numerator += valuesProductSum(node1, node2, true);
            }
            if (includeOutgoing) {
                numerator += valuesProductSum(node1, node2, false);
            }

            double denominator = 0;
            double denomSum1 = 0;
            double denomSum2 = 0;
            if (includeIncoming) {
                denomSum1 += valueSquareSum(node1, true);
                denomSum2 += valueSquareSum(node2, true);
            }
            if (includeOutgoing) {
                denomSum1 += valueSquareSum(node1, false);
                denomSum2 += valueSquareSum(node2, false);
            }
            denominator = Math.sqrt(denomSum1) * Math.sqrt(denomSum2);
            
            double similarity = numerator / denominator;
            return 1.0 - similarity;
        }

        private double valueSquareSum(VisualNode node, boolean incoming) {
            double sum = 0;
            for (VisualEdge e : node.getEdges(incoming)) {
                double v = e.getValue();
                sum += v * v;
            }
            return sum;
        }

        private double valuesProductSum(VisualNode node1, VisualNode node2, boolean incoming) {
            double sum = 0;
            List<VisualEdge> edges1 = node1.getEdges(incoming);
            for (VisualEdge edge1 : edges1) {

                VisualEdge matchingEdge = null;
                VisualNode opposite1 = edge1.getNode(incoming ? true : false);    // source if incoming, target if outgoing
                
                // find an edge
                double minDist = Double.POSITIVE_INFINITY;
                for (VisualEdge edge2 : node2.getEdges(incoming)) {
                    VisualNode opposite2 = edge2.getOppositeNode(node2);
                    if (opposite1 == opposite2) {
                        minDist = 0;
                        matchingEdge = edge2;
                        break;
                    } else {
                        double dist = opposite2.distanceTo(opposite1);
                        if (dist < minDist) {
                            minDist = dist;
                            matchingEdge = edge2;
                        }
                    }
                }
                if (matchingEdge != null) {
                    sum += edge1.getValue() * matchingEdge.getValue() * (1.0 - minDist);
                }
            }
            return sum;
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
                return node.hasIncomingEdges();
            }
        },
        OUT {
            @Override
            protected boolean accept(VisualNode node) {
                return node.hasOutgoingEdges();
            }
        },
        IN_OR_OUT {
            @Override
            protected boolean accept(VisualNode node) {
                return node.hasEdges();
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
