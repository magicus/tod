/*
 * Created on Nov 15, 2005
 */
package tod.core.database.structure;

import tod.core.database.browser.ILocationsRepository;

public class PrimitiveTypeInfo extends TypeInfo implements IPrimitiveTypeInfo
{
	private final int itsSize;

	public PrimitiveTypeInfo(String aName, int aSize)
	{
		super(-1, aName);
		itsSize = aSize;
	}

	public int getSize()
	{
		return itsSize;
	}

	public boolean isArray()
	{
		return false;
	}

	public boolean isPrimitive()
	{
		return true;
	}

	public boolean isVoid()
	{
		return "void".equals(getName());
	}

}
