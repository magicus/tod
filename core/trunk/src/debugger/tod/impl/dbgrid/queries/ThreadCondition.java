/*
 * Created on Aug 2, 2006
 */
package tod.impl.dbgrid.queries;

import java.util.Iterator;

import tod.impl.dbgrid.dbnode.Indexes;
import tod.impl.dbgrid.dbnode.StdIndexSet.StdTuple;
import tod.impl.dbgrid.messages.GridEvent;

/**
 * Represents a condition on event thread.
 * @author gpothier
 */
public class ThreadCondition extends SimpleCondition
{
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
