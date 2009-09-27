package ch.unifr.flowmap.models.map;

/**
 * @author Ilya Boyandin
 *         Date: 25-Sep-2009
 */
public class Area {

    private final String id;
    private final String name;
    private final Polygon[] polygons;

    public Area(String id, String name, Polygon[] polygons) {
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
