/*
evdbng - Fast database for TOD
Copyright (C) 2007 Sylapse
Proprietary and confidential
*/
package tod.impl.evdbng.queries;

import tod.impl.database.IBidiIterator;
import tod.impl.evdbng.db.IndexMerger;
import tod.impl.evdbng.db.Indexes;
import tod.impl.evdbng.db.file.SimpleTuple;
import tod.impl.evdbng.messages.GridEventNG;

/**
 * A conjunctive condition: all subconditions must match.
 * @author gpothier
 */
public class Conjunction extends CompoundCondition
{
	private static final long serialVersionUID = 6155046517220795498L;
	
	private boolean itsMatchRoles; 

	public Conjunction(boolean aMatchRoles)
	{
		itsMatchRoles = aMatchRoles;
	}

	@Override
	public IBidiIterator<SimpleTuple> createTupleIterator(
			Indexes aIndexes,
			long aEventId)
	{
		IBidiIterator<SimpleTuple>[] theIterators = new IBidiIterator[getConditions().size()];
		int i = 0;
		for (EventCondition theCondition : getConditions())
		{
			theIterators[i++] = theCondition.createTupleIterator(aIndexes, aEventId);
		}
		
		return IndexMerger.conjunction(itsMatchRoles, theIterators);
	}

	@Override
	public boolean _match(GridEventNG aEvent)
	{
		for (EventCondition theCondition : getConditions())
		{
			if (! theCondition._match(aEvent)) return false;
		}
		return true;
	}
	
}
