/**
 * 
 */
package jflowmap.util;

/**
 * @author ilya
 *
 */
public final class MathUtils {

	private MathUtils() {
	}
	
	public static double log(double x, double base) {
		if (base == Math.E) {
			return Math.log(x);
		}
		if (base == 10) {
			return Math.log10(x);
		}
		return Math.log(x) / Math.log(base);
	}

	public enum Rounding {
		FLOOR {
			@Override
			public long round(double value) {
				return (long) Math.floor(value);
			}
		},
		CEIL {
			@Override
			public long round(double value) {
				return (long) Math.ceil(value);
			}
		},
		ROUND {
			@Override
			public long round(double value) {
				return Math.round(value);
			}
		};
		
		public abstract long round(double value);
	}

}
