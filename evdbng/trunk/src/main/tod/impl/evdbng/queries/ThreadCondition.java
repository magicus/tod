/*
evdbng - Fast database for TOD
Copyright (C) 2007 Sylapse
Proprietary and confidential
*/
package tod.impl.evdbng.queries;


import tod.impl.database.IBidiIterator;
import tod.impl.dbgrid.messages.GridEvent;
import tod.impl.evdbng.db.IEventList;
import tod.impl.evdbng.db.Indexes;
import tod.impl.evdbng.db.file.SimpleTuple;

/**
 * Represents a condition on event thread.
 * @author gpothier
 */
public class ThreadCondition extends SimpleCondition<SimpleTuple>
{
	private static final long serialVersionUID = -4695584777709297984L;
	private int itsThreadId;

	public ThreadCondition(int aThreadId)
	{
		itsThreadId = aThreadId;
	}

	@Override
	public IBidiIterator<SimpleTuple> createTupleIterator(IEventList aEventList, Indexes aIndexes, long aEventId)
	{
		return aIndexes.getThreadIndex(itsThreadId).getTupleIterator(aEventId);
	}

//	@Override
//	public long[] getEventCounts(Indexes aIndexes, long aT1, long aT2, int aSlotsCount, boolean aForceMergeCounts)
//	{
//		// TODO: check implementation
//		if (aForceMergeCounts) return super.getEventCounts(aIndexes, aT1, aT2, aSlotsCount, true);
//		
//		SimpleTree theTree = aIndexes.getThreadIndex(itsThreadId);
//		return theTree.fastCountTuples(aT1, aT2, aSlotsCount);
//	}

	@Override
	public boolean _match(GridEvent aEvent)
	{
		return aEvent.getThread() == itsThreadId;
	}
	
	@Override
	protected String toString(int aIndent)
	{
		return String.format("Thread number = %d", itsThreadId);
	}

}
