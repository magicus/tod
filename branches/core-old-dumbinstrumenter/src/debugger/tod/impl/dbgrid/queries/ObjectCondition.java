/*
 * Created on Aug 2, 2006
 */
package tod.impl.dbgrid.queries;

import java.util.Iterator;

import tod.impl.dbgrid.dbnode.Indexes;
import tod.impl.dbgrid.dbnode.RoleIndexSet;
import tod.impl.dbgrid.dbnode.RoleIndexSet.RoleTuple;
import tod.impl.dbgrid.dbnode.StdIndexSet.StdTuple;
import tod.impl.dbgrid.messages.GridEvent;
import zz.utils.AbstractFilteredIterator;

/**
 * Represents a condition on an object, with a corresponding role.
 * @author gpothier
 */
public class ObjectCondition extends SimpleCondition
{
	private int itsObjectId;
	private byte itsRole;
	
	public ObjectCondition(int aObjectId, byte aRole)
	{
		itsObjectId = aObjectId;
		itsRole = aRole;
	}

	@Override
	protected Iterator<StdTuple> createTupleIterator(Indexes aIndexes, long aTimestamp)
	{
		Iterator<RoleIndexSet.RoleTuple> theTupleIterator = aIndexes.objectIndex.getIndex(itsObjectId).getTupleIterator(aTimestamp);
		if (itsRole == RoleIndexSet.ROLE_OBJECT_ANYARG)
		{
			theTupleIterator = new AbstractFilteredIterator<RoleTuple, RoleTuple>(theTupleIterator)
			{
				@Override
				protected Object transform(RoleTuple aIn)
				{
					return aIn.getRole() >= 0 ? aIn : REJECT;
				}
			};
		}
		else
		{
			theTupleIterator = RoleIndexSet.createFilteredIterator(theTupleIterator, itsRole);
		}
		
		return (Iterator) theTupleIterator;
	}

	@Override
	public boolean match(GridEvent aEvent)
	{
		return aEvent.matchObjectCondition(itsObjectId, itsRole);
	}
	
	@Override
	protected String toString(int aIndent)
	{
		return String.format("ObjectId = %d (role %d)", itsObjectId, itsRole);
	}

}