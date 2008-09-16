/*
TOD - Trace Oriented Debugger.
Copyright (c) 2006-2008, Guillaume Pothier
All rights reserved.

This program is free software; you can redistribute it and/or 
modify it under the terms of the GNU General Public License 
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful, 
but WITHOUT ANY WARRANTY; without even the implied warranty of 
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
General Public License for more details.

You should have received a copy of the GNU General Public License 
along with this program; if not, write to the Free Software 
Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
MA 02111-1307 USA

Parts of this work rely on the MD5 algorithm "derived from the 
RSA Data Security, Inc. MD5 Message-Digest Algorithm".
*/
package tod.impl.dbgrid.db;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import tod.core.DebugFlags;
import tod.impl.dbgrid.db.DatabaseNode.FlushMonitor;
import tod.impl.dbgrid.db.ObjectsReorderingBuffer.Entry;
import tod.impl.dbgrid.db.ObjectsReorderingBuffer.ReorderingBufferListener;
import zz.utils.monitoring.AggregationType;
import zz.utils.monitoring.Monitor;
import zz.utils.monitoring.Probe;

/**
 * Stores registered objects.
 * @author gpothier
 */
public abstract class ObjectsDatabase
implements ReorderingBufferListener
{
	private ObjectsReorderingBuffer itsReorderingBuffer = new ObjectsReorderingBuffer(this);

	private long itsLastRecordedId = 0;
	private long itsDroppedObjects = 0;
	private long itsUnorderedObjects = 0;
	private long itsProcessedObjects = 0;
	private long itsLastAddedId;
	private long itsLastProcessedId;
	private long itsObjectsCount = 0;

	public ObjectsDatabase()
	{
		Monitor.getInstance().register(this);		
	}

	/**
	 * Stores an object into the database. The object is serialized by {@link #encode(Object)}
	 * prior to storage.
	 * @param aId Id of the object to store.
	 * @param aObject The object to store.
	 */
	public void store(long aId, Object aObject)
	{
		store(aId, encode(aObject));
	}

	public void store(long aId, byte[] aData, long aTimestamp)
	{
		if (aId < itsLastAddedId) itsUnorderedObjects++;
		else itsLastAddedId = aId;
		
		Entry theEntry = new Entry(aId, aData, aTimestamp);
		
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
		
		store(theId, aEntry.data);
	}
	
	private void store(long aId, byte[] aData)
	{
		itsObjectsCount++;
		
		assert aData.length > 0;
		if (aId < itsLastRecordedId)
		{
			itsDroppedObjects++;
			return;
		}
		
		itsLastRecordedId = aId;
		store0(aId, aData);
	}
	
	protected abstract void store0(long aId, byte[] aData);
	
	public synchronized int flush(FlushMonitor aFlushMonitor)
	{
		int theCount = 0;
		System.out.println("[ReorderedObjectsDatabase] Flushing...");
		while (! itsReorderingBuffer.isEmpty())
		{
			if (aFlushMonitor != null && aFlushMonitor.isCancelled()) 
			{
				System.out.println("[ObjectsDatabase] Flush cancelled.");
				break;
			}
			
			doStore(itsReorderingBuffer.pop());
			theCount++;
		}
		System.out.println("[ObjectsDatabase] Flushed "+theCount+" objects.");

		return theCount;
	}
	
	public int flushOld(long aOldness, FlushMonitor aFlushMonitor)
	{
		int theCount = 0;
		
		while (isNextEventFlushable(aOldness)) 
		{
			if (aFlushMonitor != null && aFlushMonitor.isCancelled()) 
			{
				System.out.println("[ObjectsDatabase] FlushOld cancelled.");
				break;
			}

			flushOldestEvent();
			theCount++;
		}

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

	public void dispose()
	{
		Monitor.getInstance().unregister(this);
	}
	
	public abstract Object load(long aObjectId);

	/**
	 * Serializes the given object into an array of bytes so that
	 * it can be stored.
	 */
	protected byte[] encode(Object aObject)
	{
		try
		{
			ByteArrayOutputStream theStream = new ByteArrayOutputStream();
			ObjectOutputStream theOOStream = new ObjectOutputStream(theStream);
			theOOStream.writeObject(aObject);
			theOOStream.flush();
			return theStream.toByteArray();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Deserializes an object previously serialized by {@link #encode(Object)}.
	 */
	protected Object decode(long aId, byte[] aData)
	{
		assert aData.length > 0;
		ByteArrayInputStream theStream = new ByteArrayInputStream(aData);
		return decode(aId, theStream);
	}
	
	/**
	 * Deserializes an object previously serialized by {@link #encode(Object)}.
	 */
	protected Object decode(long aId, InputStream aStream)
	{
		try
		{
			ObjectInputStream theOIStream = new ObjectInputStream(aStream);
			return theOIStream.readObject();
		}
		catch (IOException e)
		{
			throw new RuntimeException("Error while decoding object "+aId, e);
		}
		catch (ClassNotFoundException e)
		{
			throw new RuntimeException("Error while decoding object "+aId, e);
		}
	}
	

	/**
	 * define if the difference between the oldest event of the buffer
	 *  and the newest is more than aDelay (in nanosecond)
	 * @param aDelay
	 * @return
	 */
	private boolean isNextEventFlushable(long aDelay)
	{
		return itsReorderingBuffer.isNextEventFlushable(aDelay);
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

	@Probe(key = "objects count", aggr = AggregationType.SUM)
	public long getObjectsCount()
	{
		return itsObjectsCount;
	}
	

	public void objectDropped()
	{
		itsDroppedObjects++;
	}
	

}
