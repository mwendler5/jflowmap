package ch.unifr.flowmap.data;

/**
 * @author Ilya Boyandin
 */
public interface IAttrValue<T> {

	T getValue();

	Class<T> getType();

}
