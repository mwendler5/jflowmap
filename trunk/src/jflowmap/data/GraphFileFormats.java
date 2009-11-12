package jflowmap.data;

import prefuse.data.io.DataIOException;
import prefuse.data.io.GraphMLReader;
import prefuse.data.io.GraphReader;
import at.fhj.utils.misc.FileUtils;

/**
 * @author Ilya Boyandin
 */
public enum GraphFileFormats {
    GRAPH_ML("graphml") {
        @Override
        public GraphReader createReader() {
            return new GraphMLReader();
        }
    },
    XML("xml") {
        @Override
        public GraphReader createReader() {
            return GRAPH_ML.createReader();
        }
    },
    CSV("csv") {
        @Override
        public GraphReader createReader() {
            return new CsvFlowMapReader();
        }
    }
    ;
    private String extension;

    private GraphFileFormats(String extension) {
        this.extension = extension;
    }
    
    public abstract GraphReader createReader();
    
    public static GraphReader createReaderFor(String filename) throws DataIOException {
        String ext = FileUtils.getExtension(filename).toLowerCase();
        for (GraphFileFormats fmt : values()) {
            if (fmt.extension.equals(ext)) {
                return fmt.createReader();
            }
        }
        throw new DataIOException("Unsupported graph file format extension: " + ext);
    }
}