/*
evdbng - Fast database for TOD
Copyright (C) 2007 Sylapse
Proprietary and confidential
*/
package tod.impl.evdbng.queries;

import tod.impl.database.IBidiIterator;
import tod.impl.dbgrid.messages.GridEvent;
import tod.impl.evdbng.db.EventList;
import tod.impl.evdbng.db.IndexMerger;
import tod.impl.evdbng.db.IndexSet;
import tod.impl.evdbng.db.Indexes;
import tod.impl.evdbng.db.file.SimpleTuple;

/**
 * A conjunctive condition: all subconditions must match.
 * @author gpothier
 */
public class Conjunction extends CompoundCondition
{
	private static final long serialVersionUID = 6155046517220795498L;
	
	private final boolean itsMatchRoles;
	private final boolean itsFitlerDuplicates;

	public Conjunction(boolean aMatchRoles, boolean aFilterDuplicates)
	{
		itsMatchRoles = aMatchRoles;
		itsFitlerDuplicates = aFilterDuplicates;
	}

	@Override
	public IBidiIterator<SimpleTuple> createTupleIterator(
			EventList aEventList,
			Indexes aIndexes, long aEventId)
	{
		IBidiIterator<SimpleTuple>[] theIterators = new IBidiIterator[getConditions().size()];
		int i = 0;
		for (EventCondition theCondition : getConditions())
		{
			theIterators[i++] = theCondition.createTupleIterator(aEventList, aIndexes, aEventId);
		}
		
		IBidiIterator<SimpleTuple> theIterator = IndexMerger.conjunction(itsMatchRoles, theIterators);
		return itsFitlerDuplicates ? IndexSet.createFilteredIterator(theIterator) : theIterator;
	}

	@Override
	public boolean _match(GridEvent aEvent)
	{
		for (EventCondition theCondition : getConditions())
		{
			if (! theCondition._match(aEvent)) return false;
		}
		return true;
	}
	
}
