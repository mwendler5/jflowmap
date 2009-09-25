package ch.unifr.flowmap.models.map;

import ch.unifr.flowmap.data.XmlMapModelReader;

import java.util.List;
import java.util.Collections;
import java.util.Collection;
import java.io.IOException;

/**
 * @author Ilya Boyandin
 *         Date: 25-Sep-2009
 */
public class MapModel {

    private String name;
    private List<MapAreaModel> areas;

    public MapModel(String name, List<MapAreaModel> areas) {
        this.name = name;
        this.areas = areas;
    }

    public String getName() {
        return name;
    }

    public Collection<MapAreaModel> getAreas() {
        return Collections.unmodifiableCollection(areas);
    }

    public static final MapModel load(String filename) throws IOException {
        XmlMapModelReader reader = new XmlMapModelReader();
        return reader.readMap(filename);
    }
}
