package jflowmap.util;

import java.util.Iterator;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;

import prefuse.data.Tuple;
import prefuse.data.tuple.TupleSet;

/**
 * @author Ilya Boyandin
 */
public class TupleStats {
    
    private TupleStats() {
    }

    public static MinMax createFor(TupleSet tupleSet, final String attrName) {
        return MinMax.createFor(iteratorFor(tupleSet, attrName));
    }
    
    @SuppressWarnings("unchecked")
    static Iterator<Double> iteratorFor(TupleSet tupleSet, final String attrName) {
        return Iterators.transform(
                tupleSet.tuples(), 
                new Function<Tuple, Double>() {
                    public Double apply(Tuple from) {
                        return from.getDouble(attrName);
                    }
                }
        );
    }

}
