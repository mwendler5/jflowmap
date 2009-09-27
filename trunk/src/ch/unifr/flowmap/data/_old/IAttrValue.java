package ch.unifr.flowmap.data._old;

/**
 * @author Ilya Boyandin
 */
public interface IAttrValue<T> {

	T getValue();

	Class<T> getType();

}
