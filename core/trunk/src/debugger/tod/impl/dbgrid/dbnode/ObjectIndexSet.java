/*
 * Created on Aug 29, 2006
 */
package tod.impl.dbgrid.dbnode;

import tod.DebugFlags;
import tod.impl.dbgrid.dbnode.file.HardPagedFile;
import tod.impl.dbgrid.messages.ObjectCodec;
import tod.impl.dbgrid.monitoring.AggregationType;
import tod.impl.dbgrid.monitoring.Probe;

/**
 * An index set specialized for objects.
 * @author gpothier
 */
public class ObjectIndexSet extends RoleIndexSet
{
	private long itsMaxId = 0;
	
	public ObjectIndexSet(String aName, HardPagedFile aFile, int aIndexCount)
	{
		super(aName, aFile, aIndexCount);
		
	}

	public void addTuple(Object aIndex, RoleTuple aTuple) 
	{
		int theId = ObjectCodec.getObjectId(aIndex, false);
		itsMaxId = Math.max(itsMaxId, theId);
		if (DebugFlags.ALIAS_OBJECTS > 0) theId %= DebugFlags.ALIAS_OBJECTS;
		if (theId != 0) super.addTuple(theId, aTuple);
	}
	
	@Override
	public void addTuple(int aIndex, RoleTuple aTuple)
	{
		throw new UnsupportedOperationException();
	}
	
	@Probe(key = "max object id", aggr = AggregationType.MAX)
	public long getMaxObjectId()
	{
		return itsMaxId;
	}

}
