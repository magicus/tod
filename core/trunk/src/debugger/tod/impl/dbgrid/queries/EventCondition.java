/*
 * Created on Aug 2, 2006
 */
package tod.impl.dbgrid.queries;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Iterator;

import tod.core.database.browser.IEventFilter;
import tod.impl.dbgrid.dbnode.EventList;
import tod.impl.dbgrid.dbnode.EventsCounter;
import tod.impl.dbgrid.dbnode.Indexes;
import tod.impl.dbgrid.dbnode.StdIndexSet.StdTuple;
import tod.impl.dbgrid.messages.GridEvent;
import zz.utils.AbstractFilteredIterator;

/**
 * Represents a boolean filtering condition on event attributes.
 * @author gpothier
 */
public abstract class EventCondition
implements IEventFilter, Serializable
{
	/**
	 * Indicates if the specified event passes the condition.
	 * This method is used for testing purposes.
	 */
	public abstract boolean match(GridEvent aEvent);
	
	/**
	 * Creates an iterator over matching events, taking them from the specified
	 * {@link EventList} and {@link Indexes}.
	 */
	public final Iterator<GridEvent> createIterator(
			final EventList aEventList,
			Indexes aIndexes,
			long aTimestamp)
	{
		Iterator<StdTuple> theIterator = createTupleIterator(aIndexes, aTimestamp);
		return new AbstractFilteredIterator<StdTuple, GridEvent>(theIterator)
		{
			@Override
			protected Object transform(StdTuple aIn)
			{
				return aEventList.getEvent(aIn.getEventPointer());
			}
		};
	}
	
	/**
	 * Returns the number of clauses (terminal nodes) of this condition;
	 */
	public abstract int getClausesCount();

	/**
	 * Creates an iterator over matching events, taking them from the specified
	 * {@link EventList} and {@link Indexes}.
	 */
	public abstract Iterator<StdTuple> createTupleIterator(
			Indexes aIndexes,
			long aTimestamp);
	
	/**
	 * Returns the number of events that matches this condition.
	 * By default performs a merge count. Subclasses can override this method
	 * to provide a more efficient implementation.
	 */
	public long[] getEventCounts(Indexes aIndexes, long aT1, long aT2, int aSlotsCount, boolean aForceMergeCounts)
	{
		return EventsCounter.mergeCountEvents(this, aIndexes, aT1, aT2, aSlotsCount); 
	}

	
	protected abstract String toString(int aIndent);
	
	@Override
	public String toString()
	{
		return toString(0);
	}
	
}
