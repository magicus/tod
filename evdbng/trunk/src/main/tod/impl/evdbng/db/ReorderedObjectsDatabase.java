/*
evdbng - Fast database for TOD
Copyright (C) 2007 Sylapse
Proprietary and confidential
*/
package tod.impl.evdbng.db;

import tod.core.DebugFlags;
import tod.impl.evdbng.db.ObjectsReorderingBuffer.Entry;
import tod.impl.evdbng.db.ObjectsReorderingBuffer.ReorderingBufferListener;
import tod.impl.evdbng.db.file.PagedFile;
import zz.utils.monitoring.AggregationType;
import zz.utils.monitoring.Probe;

/**
 * An object database that uses a reordering buffer prior to
 * storing objects.
 * @author gpothier
 */
public class ReorderedObjectsDatabase extends ObjectsDatabase
implements ReorderingBufferListener

{
	private ObjectsReorderingBuffer itsReorderingBuffer = 
		new ObjectsReorderingBuffer(this);

	private long itsDroppedObjects = 0;
	private long itsUnorderedObjects = 0;
	private long itsProcessedObjects = 0;
	private long itsLastAddedId;
	private long itsLastProcessedId;	

	public ReorderedObjectsDatabase(PagedFile aIndexFile, PagedFile aObjectsFile)
	{
		super(aIndexFile, aObjectsFile);
	}

	public void store(long aId, Object aObject)
	{
		if (aId < itsLastAddedId) itsUnorderedObjects++;
		else itsLastAddedId = aId;
		
		Entry theEntry = new Entry(aId, aObject);
		
		if (DebugFlags.DISABLE_REORDER)
		{
			doStore(theEntry);
		}
		else
		{
			while (itsReorderingBuffer.isFull()) doStore(itsReorderingBuffer.pop());
			itsReorderingBuffer.push(theEntry);
		}
	}
	
	private void doStore(Entry aEntry)
	{
		long theId = aEntry.id;
		if (theId < itsLastProcessedId)
		{
			objectDropped();
			return;
		}
		
		itsLastProcessedId = theId;
		
		itsProcessedObjects++;
		
		super.store(theId, aEntry.object);
	}
	
	public synchronized int flush()
	{
		int theCount = 0;
		System.out.println("[ReorderedObjectsDatabase] Flushing...");
		while (! itsReorderingBuffer.isEmpty())
		{
			doStore(itsReorderingBuffer.pop());
			theCount++;
		}
		System.out.println("[ReorderedObjectsDatabase] Flushed "+theCount+" objects.");

		return theCount;
	}

	@Probe(key = "Out of order objects", aggr = AggregationType.SUM)
	public long getUnorderedEvents()
	{
		return itsUnorderedObjects;
	}

	@Probe(key = "DROPPED OBJECTS", aggr = AggregationType.SUM)
	public long getDroppedEvents()
	{
		return itsDroppedObjects;
	}
	
	public void objectDropped()
	{
		itsDroppedObjects++;
	}
	

}
