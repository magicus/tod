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

import java.io.File;

import tod.core.DebugFlags;
import tod.impl.dbgrid.db.ObjectsReorderingBuffer.Entry;
import tod.impl.dbgrid.db.ObjectsReorderingBuffer.ReorderingBufferListener;
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

	public ReorderedObjectsDatabase(File aFile)
	{
		super(aFile);
	}

	public void store(long aId, Object aObject, long aTimestamp)
	{
		if (aId < itsLastAddedId) itsUnorderedObjects++;
		else itsLastAddedId = aId;
		
		Entry theEntry = new Entry(aId, aObject, aTimestamp);
		
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

	/**
	 * return 0 if the Buffer is empty else return 1
	 * @return
	 */
	public synchronized  int flushOldestEvent(){
		int theCount = 0;
		if (!itsReorderingBuffer.isEmpty())
		{
			doStore(itsReorderingBuffer.pop());
			theCount++;
		}
		return theCount;
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
