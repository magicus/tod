/*
 * Created on Aug 2, 2006
 */
package tod.impl.dbgrid.queries;

import java.util.Iterator;

import tod.impl.dbgrid.dbnode.Indexes;
import tod.impl.dbgrid.dbnode.RoleIndexSet;
import tod.impl.dbgrid.dbnode.StdIndexSet.StdTuple;
import tod.impl.dbgrid.messages.GridEvent;

/**
 * Represents a condition on behavior id and corresponding role.
 * @author gpothier
 */
public class BehaviorCondition extends SimpleCondition
{
	private static final long serialVersionUID = -9029772284148605574L;
	private int itsBehaviorId;
	private byte itsRole;
	
	public BehaviorCondition(int aBehaviorId, byte aRole)
	{
		itsBehaviorId = aBehaviorId;
		itsRole = aRole;
	}
	
	@Override
	public Iterator<StdTuple> createTupleIterator(Indexes aIndexes, long aTimestamp)
	{
		Iterator<RoleIndexSet.RoleTuple> theTupleIterator = aIndexes.behaviorIndex.getIndex(itsBehaviorId).getTupleIterator(aTimestamp);
		switch (itsRole)
		{
		case RoleIndexSet.ROLE_BEHAVIOR_ANY:
			theTupleIterator = RoleIndexSet.createFilteredIterator(theTupleIterator);
			break;
			
		case RoleIndexSet.ROLE_BEHAVIOR_ANY_ENTER:
			theTupleIterator = RoleIndexSet.createFilteredIterator(theTupleIterator, RoleIndexSet.ROLE_BEHAVIOR_CALLED, RoleIndexSet.ROLE_BEHAVIOR_EXECUTED);
			break;
			
		default:
			theTupleIterator = RoleIndexSet.createFilteredIterator(theTupleIterator, itsRole);
		}
		
		return (Iterator) theTupleIterator;
	}
	
	@Override
	public boolean match(GridEvent aEvent)
	{
		return aEvent.matchBehaviorCondition(itsBehaviorId, itsRole);
	}
	
	@Override
	protected String toString(int aIndent)
	{
		return String.format("BehaviorId = %d (role %d)", itsBehaviorId, itsRole);
	}
}
