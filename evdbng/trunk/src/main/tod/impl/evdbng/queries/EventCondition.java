/*
evdbng - Fast database for TOD
Copyright (C) 2007 Sylapse
Proprietary and confidential
*/
package tod.impl.evdbng.queries;

import java.io.Serializable;

import tod.core.database.browser.IEventFilter;
import tod.impl.database.AbstractFilteredBidiIterator;
import tod.impl.database.IBidiIterator;
import tod.impl.evdbng.db.EventList;
import tod.impl.evdbng.db.EventsCounter;
import tod.impl.evdbng.db.Indexes;
import tod.impl.evdbng.db.file.Tuple;
import tod.impl.evdbng.messages.GridEventNG;

/**
 * Represents a boolean filtering condition on event attributes.
 * @author gpothier
 */
public abstract class EventCondition<T extends Tuple>
implements IEventFilter, Serializable
{
	/**
	 * Indicates if the specified event passes the condition.
	 * This method is used for testing purposes.
	 */
	public abstract boolean _match(GridEventNG aEvent);
	
	/**
	 * Creates an iterator over matching events, taking them from the specified
	 * {@link EventList} and {@link Indexes}.
	 */
	public final IBidiIterator<GridEventNG> createIterator(
			final EventList aEventList,
			Indexes aIndexes,
			long aEventId)
	{
		IBidiIterator<T> theIterator = createTupleIterator(aIndexes, aEventId);
		return new AbstractFilteredBidiIterator<T, GridEventNG>(theIterator)
		{
			@Override
			protected Object transform(T aTuple)
			{
				return aEventList.getEvent((int) aTuple.getKey());
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
	public abstract IBidiIterator<T> createTupleIterator(
			Indexes aIndexes,
			long aEventId);
	
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
