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
        if (minValue > maxValue) {
            throw new IllegalArgumentException("minValue > maxValue");
        }
        this.min = minValue;
        this.max = maxValue;
        this.minLog = Math.log(min);
        this.maxLog = Math.log(max);
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

    /**
     * Returns a normalized value between 0 and 1.
     */
    public double normalize(double value) {
        return (value - min) / (max - min);
    }

    /**
     * Returns a normalized log(value) between 0 and 1.
     */
    public double normalizeLog(double value) {
        return (Math.log(value) - minLog) / (maxLog - minLog);
    }
   
    @Override
    public String toString() {
    	return
    	 "[" +
    	 	"min = " + min + ", " +
    	 	"max = " + max + ", " +
    	 "]"
    	;
    }

}