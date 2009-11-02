package jflowmap.data;

import java.awt.geom.Point2D;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.io.AbstractGraphReader;
import prefuse.data.io.CSVTableReader;
import prefuse.data.io.DataIOException;

/**
 * @author Ilya Boyandin
 */
public class CsvGraphReader extends AbstractGraphReader {

    private static final String COLUMN_X = "x";
    private static final String COLUMN_Y = "y";
    private static final String COLUMN_VALUE = "value";
    
    private Map<Point2D, Node> nodes;
    private Graph graph;

    @Override
    public Graph readGraph(InputStream is) throws DataIOException {
        CSVTableReader reader = new CSVTableReader();
        Table table = reader.readTable(is);

        nodes = new HashMap<Point2D, Node>();
        graph = new Graph();
        graph.addColumn(COLUMN_X, double.class);
        graph.addColumn(COLUMN_Y, double.class);
        graph.addColumn(COLUMN_VALUE, double.class);
        
        for (int i = 0, tuples = table.getTupleCount(); i < tuples; i++) {
            Tuple tuple = table.getTuple(i);
            if (tuple.getColumnCount() < 4) {
                throw new DataIOException("Not enough data columns in line " + (i + 1) + ". Must be at least 4.");
            }
            Point2D from = new Point2D.Double(tuple.getDouble(0), tuple.getDouble(1));
            Point2D to = new Point2D.Double(tuple.getDouble(2), tuple.getDouble(3)); 
            Edge edge = graph.addEdge(getNode(from), getNode(to));
            if (tuple.getColumnCount() >= 4) {
                edge.set(COLUMN_VALUE, tuple.getDouble(4));
            }
        }

        return graph;
    }

    private Node getNode(Point2D point) {
        Node node = nodes.get(point);
        if (node == null) {
            node = graph.addNode();
            node.set(COLUMN_X, point.getX());
            node.set(COLUMN_Y, point.getY());
            nodes.put(point, node);
        }
        return node;
    }

    
    
}



/*
package jflowmap.data;

import java.awt.geom.Point2D;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.io.AbstractGraphReader;
import prefuse.data.io.CSVTableReader;
import prefuse.data.io.DataIOException;

**
 * @author Ilya Boyandin
 *
public class CsvGraphReader extends AbstractGraphReader {

    private static final String SRCID = Graph.DEFAULT_SOURCE_KEY + "_id";
    private static final String TRGID = Graph.DEFAULT_TARGET_KEY + "_id";

    private Map<Point2D, Integer> positionsToNodes;
    protected Table nodes;
    protected Table edges;

    @Override
    public Graph readGraph(InputStream is) throws DataIOException {
        CSVTableReader reader = new CSVTableReader();
        Table table = reader.readTable(is);

        nodes = new Table();
        edges = new Table();
        positionsToNodes = new HashMap<Point2D, Integer>();
        
        Graph graph = new Graph();
        for (int i = 0, tuples = table.getTupleCount(); i < tuples; i++) {
            Tuple tuple = table.getTuple(i);
            if (tuple.getColumnCount() < 4) {
                throw new DataIOException("Not enough data columns in line " + (i + 1) + ". Must be at least 4.");
            }
            
            int edgesRowId = edges.addRow();
            edges.setString(edgesRowId, SRCID, atts.getValue(SRC));
            edges.setString(edgesRowId, TRGID, atts.getValue(TRG));

            Point2D from = new Point2D.Double(tuple.getDouble(0), tuple.getDouble(1));
            Point2D to = new Point2D.Double(tuple.getDouble(2), tuple.getDouble(3)); 
            graph.addEdge(getNode(from), getNode(to));
        }

        graph.addNode()
    }

    private Node getNode(Point2D point) {
        Node node = positionsToNodes.get(point);
        if (node == null) {
            int nodesRowId = nodes.addRow();
            positionsToNodes.put(point, nodesRowId);
            node.set("x", point.getX());
            node.set("y", point.getY());
        }
        return node;
    }

    
    
}
*/