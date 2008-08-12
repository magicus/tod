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
package tod.impl.dbgrid.db;

import java.rmi.RemoteException;
import java.util.Comparator;

import tod.core.DebugFlags;
import tod.core.database.structure.IStructureDatabase;
import tod.impl.database.IBidiIterator;
import tod.impl.dbgrid.IGridEventFilter;
import tod.impl.dbgrid.db.EventReorderingBuffer.ReorderingBufferListener;
import tod.impl.dbgrid.messages.GridEvent;
import zz.utils.monitoring.AggregationType;
import zz.utils.monitoring.Monitor;
import zz.utils.monitoring.Probe;

/**
 * This class manages an event database for a debugging session.
 * An event database consists in an event list and a number
 * of indexes.
 * @author gpothier
 */
public abstract class EventDatabase 
implements ReorderingBufferListener
{
	private final IStructureDatabase itsStructureDatabase;
	
	/**
	 * Timestamp of the last processed event
	 */
	private long itsLastProcessedTimestamp;	
	private long itsProcessedEventsCount = 0;
	
	private long itsLastAddedTimestamp;
	private long itsAddedEventsCount = 0;
	
	private long itsDroppedEvents = 0;
	private long itsUnorderedEvents = 0;
	
	private EventReorderingBuffer itsReorderingBuffer = new EventReorderingBuffer(this);
	
	/**
	 * Creates a new database using the specified file.
	 */
	public EventDatabase(IStructureDatabase aStructureDatabase, int aNodeId) 
	{
		Monitor.getInstance().register(this);
		itsStructureDatabase = aStructureDatabase;
	}

	public IStructureDatabase getStructureDatabase()
	{
		return itsStructureDatabase;
	}
	
	/**
	 * Recursively disposes this database.
	 * Amongst other things, unregister from the monitor.
	 */
	public void dispose()
	{
		Monitor.getInstance().unregister(this);
	}

	/**
	 * Creates an iterator over matching events of this node, starting at the specified timestamp.
	 */
	public abstract IBidiIterator<GridEvent> evaluate(IGridEventFilter aCondition, long aTimestamp);

	public RINodeEventIterator getIterator(IGridEventFilter aCondition) throws RemoteException
	{
		return new NodeEventIterator(this, aCondition);
	}

	public abstract long[] getEventCounts(
			IGridEventFilter aCondition,
			long aT1, 
			long aT2,
			int aSlotsCount, 
			boolean aForceMergeCounts);

	/**
	 * Pushes a single message to this node.
	 * Messages can be events or parent/child
	 * relations.
	 */
	public void push(GridEvent aEvent)
	{
		if (DebugFlags.SKIP_EVENTS) return;
		
//		System.out.println("AddEvent ts: "+aEvent.getTimestamp());
		long theTimestamp = aEvent.getTimestamp();
		if (theTimestamp < itsLastAddedTimestamp)
		{
//			System.out.println(String.format(
//					"Out of order event: %s(%02d)/%s(%02d) (#%d)",
//					AgentUtils.formatTimestampU(theTimestamp),
//					aEvent.getThread(),
//					AgentUtils.formatTimestampU(itsLastAddedTimestamp),
//					itsLastAddedEvent.getThread(),
//					itsAddedEventsCount));
//			
			itsUnorderedEvents++;
		}
		else
		{
			itsLastAddedTimestamp = theTimestamp;
		}
		
		itsAddedEventsCount++;
		
		if (DebugFlags.DISABLE_REORDER)
		{
			processEvent(aEvent);
		}
		else
		{
			while (itsReorderingBuffer.isFull()) processEvent(itsReorderingBuffer.pop());
			itsReorderingBuffer.push(aEvent);
		}
	}
	
	/**
	 * Flushes the event buffer. Events should not be added
	 * after this method is called.
	 */
	public int flush()
	{
		int theCount = 0;
		System.out.println("[EventDatabase] Flushing...");
		while (! itsReorderingBuffer.isEmpty())
		{
			processEvent(itsReorderingBuffer.pop());
			theCount++;
		}
		System.out.println("[EventDatabase] Flushed "+theCount+" events.");
		return theCount;
	}
	
	/**
	 * Flushes the oldest available event.
	 * Returns 0 if the Buffer was empty, otherwise returns 1 if an event was indeed flushed.
	 */
	public int flushOldestEvent()
	{
		int theCount = 0;
		if (!itsReorderingBuffer.isEmpty())
		{
			processEvent(itsReorderingBuffer.pop());
			theCount++;
		}
		return theCount;
	}
	
	
	@Probe(key = "Out of order events", aggr = AggregationType.SUM)
	public long getUnorderedEvents()
	{
		return itsUnorderedEvents;
	}

	@Probe(key = "DROPPED EVENTS", aggr = AggregationType.SUM)
	public long getDroppedEvents()
	{
		return itsDroppedEvents;
	}

	
	public void eventDropped(long aLastRetrieved, long aNewEvent)
	{
		long theDelta = aLastRetrieved-aNewEvent;
		itsDroppedEvents++;
		
		System.err.println(String.format(
				"WARNING: out of order event - dropped (last: %d, new: %d, delta: %d, #%d)",
				aLastRetrieved, 
				aNewEvent,
				theDelta,
				itsDroppedEvents));
	}
	
	/**
	 * define if the difference between the oldest event of the buffer
	 *  and the newest is more than aDelay (in nanosecond)
	 * @param aDelay
	 * @return
	 */
	public boolean isNextEventFlushable(long aDelay)
	{
		return itsReorderingBuffer.isNextEventFlushable(aDelay) ;
	}
	
	
	
	private void processEvent(GridEvent aEvent)
	{
		long theTimestamp = aEvent.getTimestamp();
		if (theTimestamp < itsLastProcessedTimestamp)
		{
			eventDropped(itsLastProcessedTimestamp, theTimestamp);
			return;
		}
		
		itsLastProcessedTimestamp = theTimestamp;
		itsProcessedEventsCount++;
		
		processEvent0(aEvent);
	}

	protected abstract void processEvent0(GridEvent aEvent);
	
	/**
	 * Returns the amount of disk storage used by this node.
	 */
	public abstract long getStorageSpace();
	
	public abstract long getEventsCount();
}
