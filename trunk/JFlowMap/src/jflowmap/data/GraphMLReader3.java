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

package jflowmap.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import jflowmap.FlowMapGraph;

import org.apache.log4j.Logger;

import prefuse.data.Graph;
import prefuse.data.Schema;
import prefuse.data.Table;
import prefuse.data.parser.DataParseException;
import prefuse.data.parser.ParserFactory;
import prefuse.util.collections.IntIterator;
import prefuse.util.io.IOLib;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * GraphML reader able of loading several graphs in one file. It's based on StAX and
 * therefore requires much less memory than GraphMLReader2 and works faster.
 *
 * @author Ilya Boyandin
 */
public class GraphMLReader3 {

  public static Logger logger = Logger.getLogger(GraphMLReader3.class);

  private static final String DEFAULT_CHARSET = "utf-8";
  public static final String GRAPH_CLIENT_PROPERTY__ID = "id";
  private static final String SRC = Graph.DEFAULT_SOURCE_KEY;
  private static final String TRG = Graph.DEFAULT_TARGET_KEY;
  private static final String SRCID = SRC + "_id";
  private static final String TRGID = TRG + "_id";

  private static final String NAMESPACE = null; //  "http://graphml.graphdrawing.org/xmlns"

  private String charset = DEFAULT_CHARSET;

  private final ParserFactory dataParser;
  private LineNumberReader lineNumberReader;

  private Schema nodeSchema, edgeSchema;
  private Map<String, String> attrIdToName;
  private Map<String, Integer> nodeIdToIndex;

  public GraphMLReader3() {
    dataParser = ParserFactory.getDefaultFactory();
  }

  public void setCharset(String charset) {
    this.charset = charset;
  }

  public Iterable<Graph> readFromFile(String filename) throws IOException {
    logger.info("Loading file \"" + filename + "\"");
    Iterable<Graph> graphs = readFromStream(IOLib.streamFromString(filename));
    logger.info("Finished loading file");
    return graphs;
  }

  public Iterable<Graph> readFromStream(InputStream is) throws IOException {
    XMLInputFactory inputFactory = XMLInputFactory.newInstance();
    XMLStreamReader in;
    try {
      lineNumberReader = new LineNumberReader(new InputStreamReader(is, charset));
      in = inputFactory.createXMLStreamReader(lineNumberReader);

      List<Graph> graphs = Lists.newArrayList();
      initSchemas();

      attrIdToName = Maps.newHashMap();
      String graphId = null;
      boolean graphDirected = false;
      Table nodeTable = null, edgeTable = null;

      OUTER: while (in.hasNext()) {
        int eventType = in.nextTag();
        String tag = in.getLocalName();

        switch (eventType) {
          case XMLStreamReader.START_ELEMENT:
            if (tag.equals("key")) {
              readKey(in);

            } else if (tag.equals("graph")) {
              lockSchemas();

              nodeIdToIndex = Maps.newHashMap();
              nodeTable = nodeSchema.instantiate();
              edgeTable = edgeSchema.instantiate();

              graphId = in.getAttributeValue(NAMESPACE, "id");
              graphDirected = ("directed".equals(in.getAttributeValue(NAMESPACE, "edgedefault")));

            } else if (tag.equals("node")) {

              int ri = nodeTable.addRow();

              String nodeId = in.getAttributeValue(NAMESPACE, "id");
              nodeIdToIndex.put(nodeId, ri);

              nodeTable.set(ri, FlowMapGraph.GRAPH_NODE_TABLE_COLUMN_NAME__ID, nodeId);

              readData(in, nodeTable, ri, "node");

            } else if (tag.equals("edge")) {

              int ri = edgeTable.addRow();

              edgeTable.setString(ri, SRCID, in.getAttributeValue(NAMESPACE, "source"));
              edgeTable.setString(ri, TRGID, in.getAttributeValue(NAMESPACE, "target"));

              readData(in, edgeTable, ri, "edge");
            }
            break;

          case XMLStreamReader.END_ELEMENT:

            if (tag.equals("graph")) {
              assert(nodeTable != null);
              assert(edgeTable != null);

              // Map edge starts/ends to nodes
              IntIterator rows = edgeTable.rows();
              while (rows.hasNext()) {
                int ri = rows.nextInt();

                String src = edgeTable.getString(ri, SRCID);
                if (!nodeIdToIndex.containsKey(src)) {
                  throw new IOException(
                    "Tried to create edge with source node id=" + src
                    + " which does not exist.");
                }
                edgeTable.setInt(ri, SRC, nodeIdToIndex.get(src));

                String trg = edgeTable.getString(ri, TRGID);
                if (!nodeIdToIndex.containsKey(trg)) {
                  throw new IOException(
                    "Tried to create edge with target node id=" + trg
                    + " which does not exist.");
                }
                edgeTable.setInt(ri, TRG, nodeIdToIndex.get(trg));
              }
              edgeTable.removeColumn(SRCID);
              edgeTable.removeColumn(TRGID);


              // Finally, create the graph
              Graph graph = new Graph(nodeTable, edgeTable, graphDirected);
              FlowMapGraph.setGraphId(graph, graphId);
              graphs.add(graph);

            } else if (tag.equals("graphml")) {
              break OUTER;
            }
            break;
        }
      }

      return graphs;
    } catch (XMLStreamException e) {
      throw new IOException("Parse error in line " + lineNumberReader.getLineNumber() + ": " + e.getMessage(), e);
    }
  }

  private void readData(XMLStreamReader in, Table table, int tableRowIdx, String untilEndOf)
    throws IOException, XMLStreamException {

    OUTER: while (in.hasNext()) {
      int eventType = in.nextTag();
      String tag = in.getLocalName();

      switch (eventType) {
        case XMLStreamReader.START_ELEMENT:
          if (tag.equals("data")) {
            String key = attrIdToName.get(in.getAttributeValue(NAMESPACE, "key"));
            String valueStr = in.getElementText();
            Object value;
            if (valueStr == null) {
              value = null;
            } else {
              Class<?> columnType = table.getColumnType(key);
              if (columnType == null) {
                throw new IOException("Column type for " + key + " not found");
              }
              value = parseData(valueStr, columnType);
              table.set(tableRowIdx, key, value);
            }
          }
          break;

        case XMLStreamReader.END_ELEMENT:
          if (tag.equals(untilEndOf)) {
            break OUTER;
          }
          break;
      }

    }
  }

  private void lockSchemas() {
    nodeSchema.lockSchema();
    edgeSchema.lockSchema();
  }

  private void initSchemas() {
    nodeSchema = new Schema();
    nodeSchema.addColumn(FlowMapGraph.GRAPH_NODE_TABLE_COLUMN_NAME__ID, String.class);
    edgeSchema = new Schema();
    edgeSchema.addColumn(SRC, int.class);
    edgeSchema.addColumn(TRG, int.class);
    edgeSchema.addColumn(SRCID, String.class);
    edgeSchema.addColumn(TRGID, String.class);
  }

  private void readKey(XMLStreamReader in) throws IOException {
    String id = in.getAttributeValue(NAMESPACE, "id");
    String forWhat = in.getAttributeValue(NAMESPACE, "for");
    String name = in.getAttributeValue(NAMESPACE, "attr.name");
    GraphMLDataTypes type = GraphMLDataTypes.parse(in.getAttributeValue(NAMESPACE, "attr.type"));

    attrIdToName.put(id, name);

    String defaultValStr = in.getAttributeValue(NAMESPACE, "default");
    final Object defaultVal;
    if (defaultValStr == null) {
      defaultVal = null;
    } else {
      defaultVal = parseData(defaultValStr, type.klass);
    }

    if (forWhat == null  ||  forWhat.equals("all")) {
      nodeSchema.addColumn(name, type.klass, defaultVal);
      edgeSchema.addColumn(name, type.klass, defaultVal);
    } else if (forWhat.equals("node")) {
      nodeSchema.addColumn(name, type.klass, defaultVal);
    } else if (forWhat.equals("edge")) {
      edgeSchema.addColumn(name, type.klass, defaultVal);
    } else {
      throw new IOException("Unrecognized 'for' value: " + forWhat);
    }
  }

  private Object parseData(String defaultValStr, Class<?> klass) throws IOException {
    try {
      return dataParser.getParser(klass).parse(defaultValStr);
    } catch (DataParseException e) {
      throw new IOException(e);
    }
  }

}
