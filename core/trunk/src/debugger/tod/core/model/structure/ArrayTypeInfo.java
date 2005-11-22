/*
 * Created on Nov 15, 2005
 */
package tod.core.model.structure;

import tod.core.model.trace.ILocationTrace;

public class ArrayTypeInfo extends TypeInfo implements IArrayTypeInfo
{
	private ITypeInfo itsElementType;
	private int itsDimensions;
	
	public ArrayTypeInfo(ILocationTrace aTrace, ITypeInfo aElementType, int aDimensions)
	{
		super(aTrace, -1);
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
