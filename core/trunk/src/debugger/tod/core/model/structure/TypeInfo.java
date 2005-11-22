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
public abstract class TypeInfo extends LocationInfo implements ITypeInfo
{
	public TypeInfo(ILocationTrace aTrace, int aId, String aName)
	{
		super(aTrace, aId, aName);
	}

	public TypeInfo(ILocationTrace aTrace, int aId)
	{
		super(aTrace, aId);
	}
}
