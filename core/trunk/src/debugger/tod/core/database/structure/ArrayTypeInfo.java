/*
 * Created on Nov 15, 2005
 */
package tod.core.database.structure;

import tod.core.database.browser.ILocationsRepository;

public class ArrayTypeInfo extends TypeInfo implements IArrayTypeInfo
{
	private ITypeInfo itsElementType;
	private int itsDimensions;
	
	public ArrayTypeInfo(ITypeInfo aElementType, int aDimensions)
	{
		super(-1);
		itsElementType = aElementType;
		itsDimensions = aDimensions;
	}

	public int getDimensions()
	{
		return itsDimensions;
	}

	public ITypeInfo getElementType()
	{
		return itsElementType;
	}

	public int getSize()
	{
		return 1;
	}

	public boolean isArray()
	{
		return true;
	}

	public boolean isPrimitive()
	{
		return false;
	}

	public boolean isVoid()
	{
		return false;
	}
	
	
}
