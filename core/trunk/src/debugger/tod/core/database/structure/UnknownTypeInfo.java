/*
 * Created on Oct 15, 2006
 */
package tod.core.database.structure;

/**
 * Information for types that are not known o the instrumenter.
 * @author gpothier
 */
public class UnknownTypeInfo extends TypeInfo
{
	public UnknownTypeInfo(int aId, String aName)
	{
		super(aId, aName);
	}

	public int getSize()
	{
		return 1;
	}

	public boolean isArray()
	{
		return false;
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
