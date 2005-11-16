/*
 * Created on Nov 15, 2005
 */
package tod.core.model.structure;

import tod.core.model.trace.ILocationTrace;

public class PrimitiveTypeInfo extends TypeInfo
{
	private final int itsSize;

	public PrimitiveTypeInfo(ILocationTrace aTrace, String aName, int aSize)
	{
		super(aTrace, -1, aName);
		itsSize = aSize;
	}

	public int getSize()
	{
		return itsSize;
	}

	@Override
	public boolean isArray()
	{
		return false;
	}

	@Override
	public boolean isPrimitive()
	{
		return true;
	}

	@Override
	public boolean isVoid()
	{
		return "void".equals(getName());
	}

}
