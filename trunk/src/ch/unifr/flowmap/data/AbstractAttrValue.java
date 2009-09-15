package ch.unifr.flowmap.data;

/**
 * @author Ilya Boyandin
 */
public abstract class AbstractAttrValue<T> implements IAttrValue<T> {

	private T value;
	
	public AbstractAttrValue(T value) {
		this.value = value;
	}

	@Override
	public T getValue() {
		return value;
	}

}
