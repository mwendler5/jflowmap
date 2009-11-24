package jflowmap.visuals;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolox.util.PFixedWidthStroke;

import java.awt.*;

import jflowmap.models.map.Area;
import jflowmap.models.map.Polygon;

/**
 */
public class VisualArea extends PNode {

    private static final Color mapPaintColor = new Color(45, 45, 45);
    private static final Color mapStrokeColor = new Color(55, 55, 55);
//    private static final Color mapPaintColor = new Color(235, 235, 235);
//    private static final Color mapStrokeColor = new Color(225, 225, 225);
    private static final PFixedWidthStroke mapStroke = new PFixedWidthStroke(1);

    public VisualArea(Area area) {
        for (Polygon poly : area.getPolygons()) {
            PPath path = PPath.createPolyline(poly.getPoints());
            path.setPaint(mapPaintColor);
            path.setStrokePaint(mapStrokeColor);
            path.setStroke(mapStroke);
            addChild(path);
        }
    }
}
