/*
evdbng - Fast database for TOD
Copyright (C) 2007 Sylapse
Proprietary and confidential
*/
package tod.impl.evdbng.db;

import java.rmi.RemoteException;
import java.util.Comparator;

import tod.core.DebugFlags;
import tod.core.database.structure.IStructureDatabase;
import tod.impl.database.IBidiIterator;
import tod.impl.evdbng.db.EventReorderingBuffer.ReorderingBufferListener;
import tod.impl.evdbng.db.file.PagedFile;
import tod.impl.evdbng.db.file.SequenceTree;
import tod.impl.evdbng.db.file.TupleFinder.NoMatch;
import tod.impl.evdbng.messages.GridEventNG;
import tod.impl.evdbng.queries.EventCondition;
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
	
	private final PagedFile itsIndexesFile;
	
	private final EventList itsEventList;
	private final Indexes itsIndexes;
	
	/**
	 * This tree permits to retrieve the id of the event for a specified timestamp
	 */
	private final SequenceTree itsTimestampTree;
	
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
	 * Creates a new database using the specified files.
	 */
	public EventDatabase(IStructureDatabase aStructureDatabase, int aNodeId, PagedFile aIndexesFile, PagedFile aEventsFile) 
	{
		Monitor.getInstance().register(this);
		itsStructureDatabase = aStructureDatabase;
		itsIndexesFile = aIndexesFile;
		itsEventList = new EventList(itsStructureDatabase, aNodeId, itsIndexesFile, aEventsFile);
		itsIndexes = new Indexes(itsIndexesFile);
		itsTimestampTree = new SequenceTree("[EventDatabase] timestamp tree", itsIndexesFile);
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
		itsIndexesFile.dispose();
	}

	public Indexes getIndexes()
	{
		return itsIndexes;
	}
	
	/**
	 * Creates an iterator over matching events of this node, starting at the specified timestamp.
	 */
	public IBidiIterator<GridEventNG> evaluate(EventCondition<?> aCondition, long aTimestamp)
	{
		long theEventId = itsTimestampTree.getTuplePosition(aTimestamp, NoMatch.AFTER);
		return aCondition.createIterator(itsEventList, getIndexes(), theEventId);
	}

	public RINodeEventIterator getIterator(EventCondition<?> aCondition) throws RemoteException
	{
		return new NodeEventIterator(this, aCondition);
	}

	public long[] getEventCounts(
			EventCondition<?> aCondition,
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
	public void push(GridEventNG aEvent)
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
		System.err.println("WARNING: out of order event - dropped");
		itsDroppedEvents++;
	}
	
	private void processEvent(GridEventNG aEvent)
	{
		long theTimestamp = aEvent.getTimestamp();
		if (theTimestamp < itsLastProcessedTimestamp)
		{
			eventDropped();
			return;
		}
		
		itsLastProcessedTimestamp = theTimestamp;
		itsProcessedEventsCount++;
		
		int theId = itsEventList.add(aEvent);
		itsTimestampTree.add(aEvent.getTimestamp());
		if (! DebugFlags.DISABLE_INDEXES) aEvent.index(itsIndexes, theId);		
	}
	
	/**
	 * Returns the amount of disk storage used by this node.
	 */
	public long getStorageSpace()
	{
		return itsIndexesFile.getStorageSpace();
	}
	
	public long getEventsCount()
	{
		return itsEventList.getEventsCount();
	}

	
	private static class EventTimestampComparator implements Comparator<GridEventNG>
	{
		public int compare(GridEventNG aEvent1, GridEventNG aEvent2)
		{
			long theDelta = aEvent1.getTimestamp() - aEvent2.getTimestamp();
			if (theDelta == 0) return 0;
			else if (theDelta > 0) return 1;
			else return -1;
		}
	}
	
}
