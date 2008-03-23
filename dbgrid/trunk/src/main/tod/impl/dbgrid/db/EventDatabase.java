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

import static tod.impl.dbgrid.DebuggerGridConfig.DB_PAGE_SIZE;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Comparator;

import tod.core.DebugFlags;
import tod.core.database.structure.IStructureDatabase;
import tod.impl.database.IBidiIterator;
import tod.impl.dbgrid.db.EventReorderingBuffer.ReorderingBufferListener;
import tod.impl.dbgrid.db.file.HardPagedFile;
import tod.impl.dbgrid.messages.BitGridEvent;
import tod.impl.dbgrid.messages.GridEvent;
import tod.impl.dbgrid.queries.EventCondition;
import zz.utils.monitoring.AggregationType;
import zz.utils.monitoring.Monitor;
import zz.utils.monitoring.Probe;

/**
 * This class manages an event database for a debugging session.
 * An event database consists in an event list and a number
 * of indexes.
 * @author gpothier
 */
public class EventDatabase implements ReorderingBufferListener
{
	private final IStructureDatabase itsStructureDatabase;
	private final HardPagedFile itsFile;
	
	private final EventList itsEventList;
	private final Indexes itsIndexes;
	
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
	public EventDatabase(IStructureDatabase aStructureDatabase, int aNodeId, File aFile) 
	{
		Monitor.getInstance().register(this);
		itsStructureDatabase = aStructureDatabase;
		try
		{
			itsFile = new HardPagedFile(aFile, DB_PAGE_SIZE);
			itsEventList = new EventList(itsStructureDatabase, aNodeId, itsFile);
			itsIndexes = new Indexes(itsFile);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Recursively disposes this database.
	 * Amongst other things, unregister from the monitor.
	 */
	public void dispose()
	{
		Monitor.getInstance().unregister(this);
		itsEventList.dispose();
		itsIndexes.dispose();
		itsFile.dispose();
	}

	public Indexes getIndexes()
	{
		return itsIndexes;
	}
	
	/**
	 * Creates an iterator over matching events of this node, starting at the specified timestamp.
	 */
	public IBidiIterator<GridEvent> evaluate(EventCondition aCondition, long aTimestamp)
	{
		return aCondition.createIterator(itsEventList, getIndexes(), aTimestamp);
	}

	public RINodeEventIterator getIterator(EventCondition aCondition) throws RemoteException
	{
		return new NodeEventIterator(this, aCondition);
	}

	public long[] getEventCounts(
			EventCondition aCondition,
			long aT1, 
			long aT2,
			int aSlotsCount, 
			boolean aForceMergeCounts)
	{
		return aCondition.getEventCounts(getIndexes(), aT1, aT2, aSlotsCount, aForceMergeCounts);
	}

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
	public int flushOldestEvent(){
		int theCount = 0;
		if (!itsReorderingBuffer.isEmpty()){
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

	
	public void eventDropped(long aDelta)
	{
		System.err.println("WARNING: out of order event - dropped ("+aDelta+")");
		itsDroppedEvents++;
	}
	
	/**
	 * define if the difference between the oldest event of the buffer
	 *  and the newest is more than aDelay (in nanosecond)
	 * @param aDelay
	 * @return
	 */
	public boolean isNextEventFlushable(long aDelay){
		return itsReorderingBuffer.isNextEventFlushable(aDelay) ;
	}
	
	
	
	private void processEvent(GridEvent aEvent)
	{
		BitGridEvent theEvent = (BitGridEvent) aEvent;
		
		long theTimestamp = aEvent.getTimestamp();
		if (theTimestamp < itsLastProcessedTimestamp)
		{
			eventDropped(itsLastProcessedTimestamp-theTimestamp);
			return;
		}
		
		itsLastProcessedTimestamp = theTimestamp;
		itsProcessedEventsCount++;
		
		long theId = itsEventList.add(aEvent);
		if (! DebugFlags.DISABLE_INDEXES) theEvent.index(itsIndexes, theId);		
	}
	
	/**
	 * Returns the amount of disk storage used by this node.
	 */
	public long getStorageSpace()
	{
		return itsFile.getStorageSpace();
	}
	
	public long getEventsCount()
	{
		return itsEventList.getEventsCount();
	}

	
	private static class EventTimestampComparator implements Comparator<GridEvent>
	{
		public int compare(GridEvent aEvent1, GridEvent aEvent2)
		{
			long theDelta = aEvent1.getTimestamp() - aEvent2.getTimestamp();
			if (theDelta == 0) return 0;
			else if (theDelta > 0) return 1;
			else return -1;
		}
	}
	
}
