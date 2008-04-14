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

import java.util.Comparator;
import java.util.PriorityQueue;

import tod.impl.dbgrid.DebuggerGridConfig;
import zz.utils.RingBuffer;

/**
 * A buffer that permits to reoder slightly out-of-order objects.
 * 
 * @see ObjectsDatabase
 * @author gpothier
 */
public class ObjectsReorderingBuffer
{
	private long itsLastPushed;

	private RingBuffer<Entry> itsBuffer = new RingBuffer<Entry>(DebuggerGridConfig.DB_OBJECTS_BUFFER_SIZE);

	private PriorityQueue<Entry> itsOutOfOrderBuffer = new PriorityQueue<Entry>(100, EntryComparator.getInstance());

	private ReorderingBufferListener itsListener;

	public ObjectsReorderingBuffer(ReorderingBufferListener aListener)
	{
		itsListener = aListener;
	}

	/**
	 * Pushes an incoming event into this buffer.
	 */
	public void push(Entry aEntry)
	{
		long theId = aEntry.id;
		if (theId < itsLastPushed)
		{
			// Out of order event.
			itsOutOfOrderBuffer.offer(aEntry);
		}
		else
		{
			itsLastPushed = theId;
			itsBuffer.add(aEntry);
		}
	}

	/**
	 * define if the difference between the oldest event of the buffer and the
	 * newest is more than aDelay (in nanosecond)
	 * 
	 * @param aDelay
	 * @return
	 */
	public boolean isNextEventFlushable(long aDelay)
	{
		long theNextAvailableTimestamp = getNextAvailableTimestamp();
		if (theNextAvailableTimestamp == -1) return false;
		return (itsLastPushed - theNextAvailableTimestamp) > aDelay;
	}

	/**
	 * return the timestamp of the oldest (next ordered) event in the buffer
	 * return -1 if no more event are available
	 * 
	 * @return
	 */
	public long getNextAvailableTimestamp()
	{
		long theInOrderEvent;
		long theNextOutOfOrder;

		if (!itsBuffer.isEmpty()) theInOrderEvent = itsBuffer.peek().itsTimestamp;
		else theInOrderEvent = -1;
		if (!itsOutOfOrderBuffer.isEmpty()) theNextOutOfOrder = itsOutOfOrderBuffer.peek().itsTimestamp;
		else theNextOutOfOrder = -1;

		if (theNextOutOfOrder == -1) return theInOrderEvent;
		if (theInOrderEvent == -1) return theNextOutOfOrder;
		return Math.min(theNextOutOfOrder, theInOrderEvent);
	}

	/**
	 * Returns true if an event is available on output. if an event is available
	 * it should be immediately retrieved, before a new event is pushed.
	 */
	public boolean isFull()
	{
		return itsBuffer.isFull();
	}

	public boolean isEmpty()
	{
		return itsBuffer.isEmpty() && itsOutOfOrderBuffer.isEmpty();
	}

	/**
	 * Retrieves the next ordered event.
	 */
	public Entry pop()
	{
		if (itsBuffer.isEmpty())
		{
			return itsOutOfOrderBuffer.poll();
		}
		else
		{
			Entry theInOrder = itsBuffer.peek();
			Entry theNextOutOfOrder = itsOutOfOrderBuffer.peek();
			if (theNextOutOfOrder != null && theNextOutOfOrder.id < theInOrder.id)
			{
				return itsOutOfOrderBuffer.poll();
			}
			else
			{
				Entry theEntry = itsBuffer.remove();
				assert theEntry == theInOrder;
				return theEntry;
			}
		}
	}

	public static class Entry
	{
		public final long id;

		public final byte[] data;

		public final long itsTimestamp;

		public Entry(long aId, byte[] aData, long aTimestamp)
		{
			id = aId;
			data = aData;
			itsTimestamp = aTimestamp;
		}
	}

	private static class EntryComparator implements Comparator<Entry>
	{
		private static EntryComparator INSTANCE = new EntryComparator();

		public static EntryComparator getInstance()
		{
			return INSTANCE;
		}

		private EntryComparator()
		{
		}

		public int compare(Entry aO1, Entry aO2)
		{
			long theId1 = aO1.id;
			long theId2 = aO2.id;

			if (theId1 < theId2) return -1;
			else if (theId1 == theId2) return 0;
			else return 1;
		}
	}

	public interface ReorderingBufferListener
	{
		/**
		 * Called when an object could not be reordered and had to be dropped.
		 */
		public void objectDropped();
	}

}
