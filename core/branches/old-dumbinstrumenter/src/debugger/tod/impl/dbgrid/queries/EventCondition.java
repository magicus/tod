/*
 * Created on Aug 2, 2006
 */
package tod.impl.dbgrid.queries;

import java.util.Iterator;

import tod.core.database.browser.IEventFilter;
import tod.impl.dbgrid.dbnode.EventList;
import tod.impl.dbgrid.dbnode.Indexes;
import tod.impl.dbgrid.dbnode.StdIndexSet.StdTuple;
import tod.impl.dbgrid.messages.GridEvent;
import zz.utils.AbstractFilteredIterator;

/**
 * Represents a boolean filtering condition on event attributes.
 * @author gpothier
 */
public abstract class EventCondition
implements IEventFilter
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
	protected abstract Iterator<StdTuple> createTupleIterator(
			Indexes aIndexes,
			long aTimestamp);
	
	protected abstract String toString(int aIndent);
	
	@Override
	public String toString()
	{
		return toString(0);
	}
	
}
