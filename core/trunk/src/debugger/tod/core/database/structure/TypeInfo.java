/*
 * Created on Oct 25, 2004
 */
package tod.core.database.structure;

import tod.core.database.browser.ILocationsRepository;

/**
 * Description of a type. This is an abstract class;
 * there are concrete subclasses for class, interface,
 * primitive type and array type.
 * @author gpothier
 */
public abstract class TypeInfo extends LocationInfo implements ITypeInfo
{
	public TypeInfo(int aId, String aName)
	{
		super(aId, aName);
	}

	public TypeInfo(int aId)
	{
		super(aId);
	}
	
	@Override
	public String toString()
	{
		return "Type ("+getId()+", "+getName()+")";
	}
}
