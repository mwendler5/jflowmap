package ch.unifr.flowmap.data;

import ch.unifr.flowmap.models.map.Area;
import ch.unifr.flowmap.models.map.AreaMap;
import ch.unifr.flowmap.models.map.Polygon;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.xpath.Xb1XPath;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Ilya Boyandin
 *         Date: 25-Sep-2009
 */
public class XmlAreaMapModelReader {

    public AreaMap readMap(String filename) throws IOException {
        XmlInfosetBuilder builder = XmlInfosetBuilder.newInstance();
        try {
            File file = new File(filename);
            return loadFrom(file.getName(), builder.parseReader(new FileReader(file)));
        } catch (XmlPullParserException e) {
            throw new IOException(e);
        }
    }

    private static AreaMap loadFrom(String name, XmlDocument doc) throws XmlPullParserException, IOException {
        Xb1XPath
                areaPath = new Xb1XPath("/areas/area"),
                polygonPath = new Xb1XPath("polygons"),
                polyPath = new Xb1XPath("poly");


        List<Area> areas = new ArrayList<Area>();
        List<XmlElement> areaNodes = (List<XmlElement>) areaPath.selectNodes(doc);
        for (XmlElement areaNode : areaNodes) {
            String id = areaNode.getAttributeValue(null, "id");
            for (XmlElement polygonsNode : (List<XmlElement>) polygonPath.selectNodes(areaNode)) {
                List<XmlElement> polyNodes = (List<XmlElement>) polyPath.selectNodes(polygonsNode);
                Polygon[] polygons = new Polygon[polyNodes.size()];
                int polyCnt = 0;
                for (XmlElement polyNode : polyNodes) {
                    Iterator it = polyNode.children();
                    if (it.hasNext()) {
                        java.awt.geom.Area poly = new java.awt.geom.Area();
                        String coordsStr = it.next().toString().trim();
                        String[] coords = coordsStr.split("\\s*,\\s*");
                        Point2D[] points = new Point2D[coords.length / 2];
                        double x = Double.NaN, y;
                        int coordCnt = 0;
                        for (String point : coords) {
                            if (coordCnt % 2 == 0) {
                                x = Double.parseDouble(point);
                            } else {
                                y = Double.parseDouble(point);
                                points[coordCnt / 2] = new Point2D.Double(x, y);
                            }
                            coordCnt++;
                        }
                        polygons[polyCnt] = new Polygon(points);
                        polyCnt++;
                    }
                    
                }
                areas.add(new Area(id, "", polygons));
            }
        }

        return new AreaMap(name, areas);
    }
}
