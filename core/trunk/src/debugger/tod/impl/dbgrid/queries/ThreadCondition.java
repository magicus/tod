/*
 * Created on Aug 2, 2006
 */
package tod.impl.dbgrid.queries;

import java.util.Iterator;

import tod.impl.dbgrid.dbnode.Indexes;
import tod.impl.dbgrid.dbnode.StdIndexSet.Tuple;
import tod.impl.dbgrid.messages.GridEvent;

/**
 * Represents a condition on event thread.
 * @author gpothier
 */
public class ThreadCondition extends EventCondition
{
	private int itsThread;

	public ThreadCondition(int aThread)
	{
		itsThread = aThread;
	}

	@Override
	protected Iterator<Tuple> createTupleIterator(Indexes aIndexes, long aTimestamp)
	{
		return aIndexes.threadIndex.getIndex(itsThread).getTupleIterator(aTimestamp);
	}

	@Override
	public boolean match(GridEvent aEvent)
	{
		return aEvent.getThread() == itsThread;
	}
}
