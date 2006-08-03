/*
 * Created on Aug 2, 2006
 */
package tod.impl.dbgrid.queries;

import java.util.Iterator;

import tod.impl.dbgrid.dbnode.Indexes;
import tod.impl.dbgrid.dbnode.RoleIndexSet;
import tod.impl.dbgrid.dbnode.StdIndexSet.Tuple;
import tod.impl.dbgrid.messages.GridEvent;

/**
 * Represents a condition on behavior id and corresponding role.
 * @author gpothier
 */
public class BehaviorCondition extends EventCondition
{
	private int itsBehaviorId;
	private byte itsRole;
	
	public BehaviorCondition(int aBehaviorId, byte aRole)
	{
		itsBehaviorId = aBehaviorId;
		itsRole = aRole;
	}
	
	@Override
	protected Iterator<Tuple> createTupleIterator(Indexes aIndexes, long aTimestamp)
	{
		Iterator<RoleIndexSet.Tuple> theTupleIterator = aIndexes.behaviorIndex.getIndex(itsBehaviorId).getTupleIterator(aTimestamp);
		theTupleIterator = itsRole == RoleIndexSet.ROLE_BEHAVIOR_ANY ? 
				theTupleIterator
				: RoleIndexSet.createFilteredIterator(theTupleIterator, itsRole);
		
		return (Iterator) theTupleIterator;
	}
	
	@Override
	public boolean match(GridEvent aEvent)
	{
		return aEvent.matchBehaviorCondition(itsBehaviorId, itsRole);
	}
}
