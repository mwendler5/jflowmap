package ch.unifr.flowmap.data;

import java.util.Iterator;
import prefuse.data.Tuple;
import prefuse.data.tuple.TupleSet;

/**
 * @author Ilya Boyandin
 */
public class Stats {
	public final double min;
	public final double max;

	public final double minLog;
    public final double maxLog;

    public Stats(double minValue, double maxValue) {
        this.min = minValue;
        this.max = maxValue;
        this.minLog = Math.log(minValue);
        this.maxLog = Math.log(maxValue);
    }
    
    @Override
    public String toString() {
    	return
    	 "[" +
    	 	"min = " + min + ", " +
    	 	"max = " + max + ", " +
    	 	"minLog = " + minLog + ", " +
    	 	"maxLog = " + maxLog +
    	 	", e^maxLog = " + Math.pow(Math.E, maxLog) +
    	 "]"
    	;
    }

    public static Stats getTupleStats(TupleSet tupleSet, String attrName) {
        Iterator<?> it = tupleSet.tuples();

        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;

        while (it.hasNext()) {
            Tuple tuple = (Tuple) it.next();
            final double v = tuple.getDouble(attrName);
            if (v > max) {
                max = v;
            }
            if (v < min) {
                min = v;
            }
        }
        return new Stats(min, max);
    }

}