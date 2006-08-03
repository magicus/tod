/*
 * Created on Aug 2, 2006
 */
package tod.impl.dbgrid.queries;

import java.util.Iterator;

import tod.impl.dbgrid.dbnode.EventList;
import tod.impl.dbgrid.dbnode.Indexes;
import tod.impl.dbgrid.dbnode.StdIndexSet.Tuple;
import tod.impl.dbgrid.messages.GridEvent;
import zz.utils.AbstractFilteredIterator;

/**
 * Represents a boolean filtering condition on event attributes.
 * @author gpothier
 */
public abstract class EventCondition
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
		Iterator<Tuple> theIterator = createTupleIterator(aIndexes, aTimestamp);
		return new AbstractFilteredIterator<Tuple, GridEvent>(theIterator)
		{
			@Override
			protected Object transform(Tuple aIn)
			{
				return aEventList.getEvent(aIn.getEventPointer());
			}
		};
	}

	/**
	 * Creates an iterator over matching events, taking them from the specified
	 * {@link EventList} and {@link Indexes}.
	 */
	protected abstract Iterator<Tuple> createTupleIterator(
			Indexes aIndexes,
			long aTimestamp);
	
}
