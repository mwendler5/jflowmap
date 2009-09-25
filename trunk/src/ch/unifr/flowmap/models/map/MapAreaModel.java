package ch.unifr.flowmap.models.map;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;

/**
 * @author Ilya Boyandin
 *         Date: 25-Sep-2009
 */
public class MapAreaModel {

    private final String id;
    private final String name;
    private final Polygon[] polygons;

    public MapAreaModel(String id, String name, Polygon[] polygons) {
        this.id = id;
        this.name = name;
        this.polygons = polygons.clone();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Polygon[] getPolygons() {
        return polygons.clone();
    }
}
