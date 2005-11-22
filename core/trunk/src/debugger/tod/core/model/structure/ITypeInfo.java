/*
 * Created on Nov 22, 2005
 */
package tod.core.model.structure;

public interface ITypeInfo extends ILocationInfo
{

	/**
	 * Returns the number of JVM stack slots that an object of
	 * this type occupies.
	 * For instance, object reference is 1, long and double are 2, void is 0.
	 */
	public int getSize();

	/**
	 * Indicates if ths type is a primitive type.
	 */
	public boolean isPrimitive();

	/**
	 * Indicates if ths type is an array type.
	 */
	public boolean isArray();

	/**
	 * Indicates if ths type is the void type.
	 */
	public boolean isVoid();

}