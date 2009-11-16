package jflowmap.util;

/**
 * @author Ilya Boyandin
 */
public final class DoubleUtils {
    
    private DoubleUtils() {
    }
    
    /**
     * See http://stackoverflow.com/questions/343584
     */
    public static final long mantissa(double d) {
        long bits = Double.doubleToLongBits(d);
        return bits & 0x000fffffffffffffL;
    }
    
    public static final long exponent(double d) {
    	long bits = Double.doubleToLongBits(d);
    	return (bits & 0x7ff0000000000000L) >> 52;
    }

    public static final double magnitude(double d) {
    	return Math.pow(10, exponent(d));
    }
    
}
