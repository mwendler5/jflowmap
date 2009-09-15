package ch.unifr.flowmap.data;

/**
 * @author Ilya Boyandin
 */
public interface INodeAttrValue<T> {

	T getValue();

	Class<T> getValueType();

}
