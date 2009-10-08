package jflowmap.data._old;

/**
 * @author Ilya Boyandin
 */
public final class NodeAttrValues {

	private NodeAttrValues() {
	}

	public static IAttrValue<?> parseValue(String s) {
		IAttrValue<?> value = null;
		double d = Double.NaN;
		try {
			d = Double.parseDouble(s);
			value = doubleValue(d);
		} catch (NumberFormatException nfe) {
			value = stringValue(s);
		}
		return value;
	}

	private static IAttrValue<String> stringValue(String s) {
		return new StringAttrValue(s);
	}

	private static IAttrValue<Double> doubleValue(double d) {
		return new DoubleAttrValue(d);
	}
	
	
}
