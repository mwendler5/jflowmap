package jflowmap.data._old;

/**
 * @author Ilya Boyandin
 */
public abstract class AbstractAttrValue<T> implements IAttrValue<T> {

	private T value;
	
	public AbstractAttrValue(T value) {
		this.value = value;
	}

	public T getValue() {
		return value;
	}

}
