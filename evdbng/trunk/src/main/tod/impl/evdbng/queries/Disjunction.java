/*
evdbng - Fast database for TOD
Copyright (C) 2007 Sylapse
Proprietary and confidential
*/
package tod.impl.evdbng.queries;

import tod.impl.database.IBidiIterator;
import tod.impl.dbgrid.messages.GridEvent;
import tod.impl.evdbng.db.EventList;
import tod.impl.evdbng.db.IEventList;
import tod.impl.evdbng.db.IndexMerger;
import tod.impl.evdbng.db.Indexes;
import tod.impl.evdbng.db.file.SimpleTuple;

/**
 * A disjunctive condition: any subcondition must match.
 * @author gpothier
 */
public class Disjunction extends CompoundCondition
{
	private static final long serialVersionUID = -259387225693471171L;

	@Override
	public IBidiIterator<SimpleTuple> createTupleIterator(
			IEventList aEventList,
			Indexes aIndexes, long aEventId)
	{
		IBidiIterator<SimpleTuple>[] theIterators = new IBidiIterator[getConditions().size()];
		int i = 0;
		for (EventCondition theCondition : getConditions())
		{
			theIterators[i++] = theCondition.createTupleIterator(aEventList, aIndexes, aEventId);
		}
		
		return IndexMerger.disjunction(theIterators);
	}
	
	@Override
	public long[] getEventCounts(
			IEventList aEventList,
			Indexes aIndexes, 
			long aT1, 
			long aT2, 
			int aSlotsCount, 
			boolean aForceMergeCounts)
	{
		int theSize = getConditions().size();
		
		// Get sub counts
		long[][] theCounts = new long[theSize][];
		int i = 0;
		for (EventCondition theCondition : getConditions())
		{
			theCounts[i++] = theCondition.getEventCounts(aEventList, aIndexes, aT1, aT2, aSlotsCount, aForceMergeCounts);
		}
		
		// Sum up
		long[] theResult = new long[aSlotsCount];
		for (i=0;i<theSize;i++)
		{
			for (int j=0;j<aSlotsCount;j++) theResult[j] += theCounts[i][j];
		}
		
		return theResult;
	}

	@Override
	public boolean _match(GridEvent aEvent)
	{
		for (EventCondition theCondition : getConditions())
		{
			if (theCondition._match(aEvent)) return true;
		}
		return false;
	}
	
}
