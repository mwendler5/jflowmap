package ch.unifr.flowmap.data;

/**
 * @author Ilya Boyandin
 */
public class StringAttrValue extends AbstractAttrValue<String> {

	public StringAttrValue(String value) {
		super(value);
	}
	
	@Override
	public Class<String> getType() {
		return String.class;
	}

}
