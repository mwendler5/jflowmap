package ch.unifr.flowmap.visuals;

import ch.unifr.flowmap.models.map.*;
import ch.unifr.flowmap.models.map.Polygon;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolox.util.PFixedWidthStroke;

import java.awt.*;

/**
 * @author Ilya Boyandin
 */
public class VisualAreaMap extends PNode {

    public VisualAreaMap(AreaMap mapModel) {
        for (Area area : mapModel.getAreas()) {
            addChild(new VisualArea(area));
        }
    }

}
