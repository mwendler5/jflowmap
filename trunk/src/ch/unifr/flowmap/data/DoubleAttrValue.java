package ch.unifr.flowmap.data;

/**
 * @author Ilya Boyandin
 */
public class DoubleAttrValue extends AbstractAttrValue<Double> {
	
	public DoubleAttrValue(double value) {
		super(value);
	}

	public DoubleAttrValue(Double value) {
		super(value);
	}

	@Override
	public Class<Double> getType() {
		return Double.class;
	}

	public static double asDouble(IAttrValue<?> value) {
        if (value == null) {
        	return Double.NaN;
        }
		if (value.getType() != Double.class) {
			throw new IllegalArgumentException("Value '" + value.getValue() + "' is not of type double, but " + value.getType());
		}
        if (value.getValue() == null) {
        	return Double.NaN;
        }
        return ((DoubleAttrValue)value).getValue();
	}
}
