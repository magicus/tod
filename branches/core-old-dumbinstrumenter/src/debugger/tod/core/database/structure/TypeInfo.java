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
	public TypeInfo(ILocationsRepository aTrace, int aId, String aName)
	{
		super(aTrace, aId, aName);
	}

	public TypeInfo(ILocationsRepository aTrace, int aId)
	{
		super(aTrace, aId);
	}
}
