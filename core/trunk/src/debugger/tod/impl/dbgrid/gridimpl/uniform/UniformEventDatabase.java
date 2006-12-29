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
package tod.impl.dbgrid.gridimpl.uniform;

import static tod.impl.dbgrid.DebuggerGridConfig.DB_PAGE_SIZE;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Comparator;

import tod.agent.DebugFlags;
import tod.impl.dbgrid.BidiIterator;
import tod.impl.dbgrid.db.EventList;
import tod.impl.dbgrid.db.EventReorderingBuffer;
import tod.impl.dbgrid.db.Indexes;
import tod.impl.dbgrid.db.NodeEventIterator;
import tod.impl.dbgrid.db.RINodeEventIterator;
import tod.impl.dbgrid.db.EventReorderingBuffer.ReorderingBufferListener;
import tod.impl.dbgrid.db.file.HardPagedFile;
import tod.impl.dbgrid.gridimpl.AbstractEventDatabase;
import tod.impl.dbgrid.messages.GridEvent;
import tod.impl.dbgrid.messages.GridMessage;
import tod.impl.dbgrid.monitoring.AggregationType;
import tod.impl.dbgrid.monitoring.Monitor;
import tod.impl.dbgrid.monitoring.Probe;
import tod.impl.dbgrid.queries.EventCondition;

/**
 * This class manages an event database for a debugging session.
 * An event database consists in an event list and a number
 * of indexes.
 * @author gpothier
 */
public class UniformEventDatabase extends AbstractEventDatabase 
implements ReorderingBufferListener
{
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
	
	private boolean itsFlushed = false;
	
	/**
	 * Creates a new database using the specified file.
	 */
	public UniformEventDatabase(int aNodeId, File aFile) 
	{
		Monitor.getInstance().register(this);
		try
		{
			itsFile = new HardPagedFile(aFile, DB_PAGE_SIZE);
			
			itsEventList = new EventList(aNodeId, itsFile);
			itsIndexes = new Indexes(itsFile);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public void unregister()
	{
		Monitor.getInstance().unregister(this);
		itsFile.unregister();
		itsEventList.unregister();
		itsIndexes.unregister();
	}

	public Indexes getIndexes()
	{
		return itsIndexes;
	}
	
	@Override
	public BidiIterator<GridEvent> evaluate(EventCondition aCondition, long aTimestamp)
	{
		return aCondition.createIterator(itsEventList, getIndexes(), aTimestamp);
	}

	@Override
	public RINodeEventIterator getIterator(EventCondition aCondition) throws RemoteException
	{
		return new NodeEventIterator(this, aCondition);
	}

	@Override
	public long[] getEventCounts(
			EventCondition aCondition,
			long aT1, 
			long aT2,
			int aSlotsCount, 
			boolean aForceMergeCounts) throws RemoteException
	{
		return aCondition.getEventCounts(getIndexes(), aT1, aT2, aSlotsCount, aForceMergeCounts);
	}

	@Override
	public void push(GridEvent aEvent)
	{
		assert ! itsFlushed;
		
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
	
	@Override
	public int flush()
	{
		int theCount = 0;
		System.out.println("DatabaseNode: flushing...");
		while (! itsReorderingBuffer.isEmpty())
		{
			processEvent(itsReorderingBuffer.pop());
			theCount++;
		}
		itsFlushed = true;
		System.out.println("DatabaseNode: flushed "+theCount+" events...");
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

	
	public void eventDropped()
	{
//		System.err.println("****************** WARNING ********************\n" +
//		"**********************************************\n" +
////throw new RuntimeException(
//		"Out of order event: "+theTimestamp+"/"+itsLastProcessedTimestamp
//		+" (#"+itsProcessedEventsCount+")"
//		+" (buffer size: "+itsEventBuffer.getCapacity()+")");
		
		itsDroppedEvents++;
	}
	
	private void processEvent(GridEvent aEvent)
	{
		long theTimestamp = aEvent.getTimestamp();
		if (theTimestamp < itsLastProcessedTimestamp)
		{
			eventDropped();
			return;
		}
		
		itsLastProcessedTimestamp = theTimestamp;
		itsProcessedEventsCount++;
		
		long theId = itsEventList.add(aEvent);
		if (! DebugFlags.DISABLE_INDEXES) aEvent.index(itsIndexes, theId);		
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
