/*
 * Created on Aug 29, 2006
 */
package tod.impl.dbgrid.dbnode;

import tod.impl.dbgrid.dbnode.file.HardPagedFile;
import tod.impl.dbgrid.messages.ObjectCodec;

/**
 * An index set specialized for objects.
 * @author gpothier
 */
public class ObjectIndexSet extends RoleIndexSet
{
	public ObjectIndexSet(String aName, HardPagedFile aFile, int aIndexCount)
	{
		super(aName, aFile, aIndexCount);
		
	}

	public void addTuple(Object aIndex, RoleTuple aTuple) 
	{
		int theId = ObjectCodec.getObjectId(aIndex, false);
		if (theId != 0) super.addTuple(theId, aTuple);
	}
	
	@Override
	public void addTuple(int aIndex, RoleTuple aTuple)
	{
		throw new UnsupportedOperationException();
	}
}
