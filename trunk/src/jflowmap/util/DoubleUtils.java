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
    public static final long getMantissa(double d) {
        long bits = Double.doubleToLongBits(d);
        return bits & 0x000fffffffffffffL;
    }

    public static final long getExponent(double d) {
        long bits = Double.doubleToLongBits(d);
        return (bits & 0x7ff0000000000000L) >> 52;
    }
    
}
