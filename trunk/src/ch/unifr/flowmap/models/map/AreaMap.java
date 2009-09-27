package ch.unifr.flowmap.models.map;

import ch.unifr.flowmap.data.XmlAreaMapModelReader;

import java.util.List;
import java.util.Collections;
import java.util.Collection;
import java.io.IOException;

/**
 * @author Ilya Boyandin
 *         Date: 25-Sep-2009
 */
public class AreaMap {

    private String name;
    private List<Area> areas;

    public AreaMap(String name, List<Area> areas) {
        this.name = name;
        this.areas = areas;
    }

    public String getName() {
        return name;
    }

    public Collection<Area> getAreas() {
        return Collections.unmodifiableCollection(areas);
    }

    public static final AreaMap load(String filename) throws IOException {
        XmlAreaMapModelReader reader = new XmlAreaMapModelReader();
        return reader.readMap(filename);
    }
}
