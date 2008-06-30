/*
 * Created on Jan 22, 2008
 */
package tod.impl.evdbng.db.file;

public class RoleTuple extends Tuple
{
	private byte itsRole;

	public RoleTuple(long aKey, byte aRole)
	{
		super(aKey);
		itsRole = aRole;
	}

	public byte getRole()
	{
		return itsRole;
	}
	
	@Override
	public boolean equals(Object aObj)
	{
		if (aObj instanceof RoleTuple)
		{
			RoleTuple theOther = (RoleTuple) aObj;
			return theOther.getKey() == getKey()
					&& theOther.getRole() == getRole();
		}
		else return super.equals(aObj);
	}

}
