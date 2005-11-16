/*
 * Created on Oct 25, 2004
 */
package tod.core.model.structure;

import tod.core.model.trace.ILocationTrace;

/**
 * Description of a type. This is an abstract class;
 * there are concrete subclasses for class, interface,
 * primitive type and array type.
 * @author gpothier
 */
public abstract class TypeInfo extends LocationInfo
{
	public TypeInfo(ILocationTrace aTrace, int aId, String aName)
	{
		super(aTrace, aId, aName);
	}

	public TypeInfo(ILocationTrace aTrace, int aId)
	{
		super(aTrace, aId);
	}
	
	/**
	 * Returns the number of JVM stack slots that an object of
	 * this type occupies.
	 * For instance, object reference is 1, long and double are 2, void is 0.
	 */
	public abstract int getSize();
	
	/**
	 * Indicates if ths type is a primitive type.
	 */
	public abstract boolean isPrimitive();
	
	/**
	 * Indicates if ths type is an array type.
	 */
	public abstract boolean isArray();
	
	/**
	 * Indicates if ths type is the void type.
	 */
	public abstract boolean isVoid();
}
