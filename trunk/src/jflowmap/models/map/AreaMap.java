package jflowmap.models.map;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import jflowmap.data.XmlAreaMapModelReader;

import org.apache.log4j.Logger;


/**
 * @author Ilya Boyandin
 *         Date: 25-Sep-2009
 */
public class AreaMap {

    private static Logger logger = Logger.getLogger(AreaMap.class);

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
        logger.info("Loading area map \"" + filename + "\"");
        XmlAreaMapModelReader reader = new XmlAreaMapModelReader();
        return reader.readMap(filename);
    }
}
