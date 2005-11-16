/*
 * Created on Nov 15, 2005
 */
package tod.core.model.structure;

import tod.core.model.trace.ILocationTrace;

public class ArrayTypeInfo extends TypeInfo
{
	private TypeInfo itsElementType;
	private int itsDimensions;
	
	public ArrayTypeInfo(ILocationTrace aTrace, TypeInfo aElementType, int aDimensions)
	{
		super(aTrace, -1);
		itsElementType = aElementType;
		itsDimensions = aDimensions;
	}

	public int getDimensions()
	{
		return itsDimensions;
	}

	public TypeInfo getElementType()
	{
		return itsElementType;
	}

	@Override
	public int getSize()
	{
		return 1;
	}

	@Override
	public boolean isArray()
	{
		return true;
	}

	@Override
	public boolean isPrimitive()
	{
		return false;
	}

	@Override
	public boolean isVoid()
	{
		return false;
	}
	
	
}
