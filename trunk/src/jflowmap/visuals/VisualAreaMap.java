package jflowmap.visuals;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolox.util.PFixedWidthStroke;

import java.awt.*;

import jflowmap.models.map.*;

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
