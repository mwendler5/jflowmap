package ch.unifr.flowmap.visuals;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolox.util.PFixedWidthStroke;
import ch.unifr.flowmap.models.map.Polygon;
import ch.unifr.flowmap.models.map.Area;

import java.awt.*;

/**
 */
public class VisualArea extends PNode {

    private static final Color mapPaintColor = new Color(15, 15, 15);
    private static final Color mapStrokeColor = new Color(25, 25, 25);
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
