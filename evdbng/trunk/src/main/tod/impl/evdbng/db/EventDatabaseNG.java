/*
evdbng - Fast database for TOD
Copyright (C) 2007 Sylapse
Proprietary and confidential
*/
package tod.impl.evdbng.db;

import tod.core.DebugFlags;
import tod.core.database.structure.IStructureDatabase;
import tod.impl.database.IBidiIterator;
import tod.impl.dbgrid.IGridEventFilter;
import tod.impl.dbgrid.db.EventDatabase;
import tod.impl.dbgrid.messages.GridEvent;
import tod.impl.evdbng.db.file.PagedFile;
import tod.impl.evdbng.db.file.SequenceTree;
import tod.impl.evdbng.db.file.TupleFinder.NoMatch;
import tod.impl.evdbng.messages.GridEventNG;
import tod.impl.evdbng.queries.EventCondition;

/**
 * This class manages an event database for a debugging session.
 * An event database consists in an event list and a number
 * of indexes.
 * @author gpothier
 */
public class EventDatabaseNG extends EventDatabase
{
	private final PagedFile itsIndexesFile;
	
	private final EventList itsEventList;
	private final Indexes itsIndexes;
	
	/**
	 * This tree permits to retrieve the id of the event for a specified timestamp
	 */
	private final SequenceTree itsTimestampTree;
	
	/**
	 * Creates a new database using the specified files.
	 */
	public EventDatabaseNG(IStructureDatabase aStructureDatabase, int aNodeId, PagedFile aIndexesFile, PagedFile aEventsFile) 
	{
		super(aStructureDatabase, aNodeId);
		System.out.println("Using evdbng");
		
		itsIndexesFile = aIndexesFile;
		itsEventList = new EventList(getStructureDatabase(), aNodeId, itsIndexesFile, aEventsFile);
		itsIndexes = new Indexes(itsIndexesFile);
		itsTimestampTree = new SequenceTree("[EventDatabase] timestamp tree", itsIndexesFile);
	}

	@Override
	public void dispose()
	{
		super.dispose();
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
	@Override
	public IBidiIterator<GridEvent> evaluate(IGridEventFilter aCondition, long aTimestamp)
	{
		long theEventId = itsTimestampTree.getTuplePosition(aTimestamp, NoMatch.AFTER);
		return ((EventCondition<?>) aCondition).createIterator(itsEventList, getIndexes(), theEventId);
	}

	@Override
	public long[] getEventCounts(
			IGridEventFilter aCondition,
			long aT1, 
			long aT2,
			int aSlotsCount, 
			boolean aForceMergeCounts)
	{
		return ((EventCondition<?>) aCondition).getEventCounts(
				itsEventList, 
				getIndexes(), 
				aT1, 
				aT2, 
				aSlotsCount, 
				aForceMergeCounts);
	}

	
	@Override
	protected void processEvent0(GridEvent aEvent)
	{
		GridEventNG theEvent = (GridEventNG) aEvent;

		int theId = itsEventList.add(theEvent);
		itsTimestampTree.add(aEvent.getTimestamp());
		if (! DebugFlags.DISABLE_INDEXES) theEvent.index(itsIndexes, theId);		
	}
	
	/**
	 * Returns the amount of disk storage used by this node.
	 */
	@Override
	public long getStorageSpace()
	{
		return itsIndexesFile.getStorageSpace();
	}
	
	@Override
	public long getEventsCount()
	{
		return itsEventList.getEventsCount();
	}
}
