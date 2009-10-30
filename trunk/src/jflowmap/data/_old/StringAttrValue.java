package jflowmap.data._old;

/**
 * @author Ilya Boyandin
 */
public class StringAttrValue extends AbstractAttrValue<String> {

	public StringAttrValue(String value) {
		super(value);
	}
	
	public Class<String> getType() {
		return String.class;
	}

}
