/*
evdbng - Fast database for TOD
Copyright (C) 2007 Sylapse
Proprietary and confidential
*/
package tod.impl.evdbng.queries;

import tod.core.database.structure.IStructureDatabase.ProbeInfo;
import tod.impl.database.IBidiIterator;
import tod.impl.evdbng.db.Indexes;
import tod.impl.evdbng.db.RoleIndexSet;
import tod.impl.evdbng.db.file.RoleTuple;
import tod.impl.evdbng.messages.GridEventNG;

/**
 * Represents a condition on behavior id and corresponding role.
 * @author gpothier
 */
public class BehaviorCondition extends SimpleCondition<RoleTuple>
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
	public IBidiIterator<RoleTuple> createTupleIterator(Indexes aIndexes, long aEventId)
	{
		IBidiIterator<RoleTuple> theTupleIterator = 
			aIndexes.getBehaviorIndex(itsBehaviorId).getTupleIterator(aEventId);
		
		switch (itsRole)
		{
		case RoleIndexSet.ROLE_BEHAVIOR_ANY:
			theTupleIterator = RoleIndexSet.createFilteredIterator(theTupleIterator);
			break;
			
		case RoleIndexSet.ROLE_BEHAVIOR_ANY_ENTER:
			theTupleIterator = RoleIndexSet.createFilteredIterator(
					theTupleIterator, 
					RoleIndexSet.ROLE_BEHAVIOR_CALLED, 
					RoleIndexSet.ROLE_BEHAVIOR_EXECUTED);
			break;
			
		default:
			theTupleIterator = RoleIndexSet.createFilteredIterator(
					theTupleIterator,
					itsRole);
		}
		
		return theTupleIterator;
	}
	
	@Override
	public boolean _match(GridEventNG aEvent)
	{
		ProbeInfo theProbeInfo = aEvent.getProbeInfo();
		return ((itsRole == RoleIndexSet.ROLE_BEHAVIOR_ANY || itsRole == RoleIndexSet.ROLE_BEHAVIOR_OPERATION) 
						&& (theProbeInfo != null && theProbeInfo.behaviorId == itsBehaviorId))
				|| aEvent.matchBehaviorCondition(itsBehaviorId, itsRole);
	}
	
	@Override
	protected String toString(int aIndent)
	{
		return String.format("BehaviorId = %d (role %d)", itsBehaviorId, itsRole);
	}
}
