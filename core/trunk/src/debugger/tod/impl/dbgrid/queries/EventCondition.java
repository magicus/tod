/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier (gpothier@dcc.uchile.cl)

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

Parts of this work rely on the MD5 algorithm "derived from the 
RSA Data Security, Inc. MD5 Message-Digest Algorithm".
*/
package tod.impl.dbgrid.queries;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Iterator;

import tod.core.database.browser.IEventFilter;
import tod.impl.dbgrid.AbstractFilteredBidiIterator;
import tod.impl.dbgrid.BidiIterator;
import tod.impl.dbgrid.db.EventList;
import tod.impl.dbgrid.db.EventsCounter;
import tod.impl.dbgrid.db.Indexes;
import tod.impl.dbgrid.db.StdIndexSet.StdTuple;
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
	public final BidiIterator<GridEvent> createIterator(
			final EventList aEventList,
			Indexes aIndexes,
			long aTimestamp)
	{
		BidiIterator<StdTuple> theIterator = createTupleIterator(aIndexes, aTimestamp);
		return new AbstractFilteredBidiIterator<StdTuple, GridEvent>(theIterator)
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
	public abstract BidiIterator<StdTuple> createTupleIterator(
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
