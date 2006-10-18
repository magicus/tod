/*
 * Created on Aug 2, 2006
 */
package tod.impl.dbgrid.queries;

import java.util.Iterator;

import tod.impl.dbgrid.dbnode.EventsCounter;
import tod.impl.dbgrid.dbnode.HierarchicalIndex;
import tod.impl.dbgrid.dbnode.Indexes;
import tod.impl.dbgrid.dbnode.StdIndexSet.StdTuple;
import tod.impl.dbgrid.messages.GridEvent;

/**
 * Represents a condition on event thread.
 * @author gpothier
 */
public class ThreadCondition extends SimpleCondition
{
	private static final long serialVersionUID = -4695584777709297984L;
	private int itsThreadId;

	public ThreadCondition(int aThreadId)
	{
		itsThreadId = aThreadId;
	}

	@Override
	public Iterator<StdTuple> createTupleIterator(Indexes aIndexes, long aTimestamp)
	{
		return aIndexes.threadIndex.getIndex(itsThreadId).getTupleIterator(aTimestamp);
	}

	@Override
	public long[] getEventCounts(Indexes aIndexes, long aT1, long aT2, int aSlotsCount)
	{
		if (EventsCounter.FORCE_MERGE_COUNTS) return super.getEventCounts(aIndexes, aT1, aT2, aSlotsCount);
		
		HierarchicalIndex<StdTuple> theIndex = aIndexes.threadIndex.getIndex(itsThreadId);
		return theIndex.fastCountTuples(aT1, aT2, aSlotsCount);
	}

	@Override
	public boolean match(GridEvent aEvent)
	{
		return aEvent.getThread() == itsThreadId;
	}
	
	@Override
	protected String toString(int aIndent)
	{
		return String.format("Thread number = %d", itsThreadId);
	}

}
