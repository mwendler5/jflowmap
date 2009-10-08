package jflowmap.data._old;

/**
 * @author Ilya Boyandin
 */
public interface INodeAttrValue<T> {

	T getValue();

	Class<T> getValueType();

}
