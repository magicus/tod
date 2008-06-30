/*
evdbng - Fast database for TOD
Copyright (C) 2007 Sylapse
Proprietary and confidential
*/
package tod.impl.evdbng.db;

import java.util.Comparator;
import java.util.PriorityQueue;

import tod.impl.evdbng.DebuggerGridConfig;
import zz.utils.RingBuffer;

/**
 * A buffer that permits to reoder slightly out-of-order objects.
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
	 * Returns true if an event is available on output.
	 * if an event is available it should be immediately retrieved,
	 * before a new event is pushed.
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
		public final Object object;
		
		public Entry(final long aId, final Object aObject)
		{
			id = aId;
			object = aObject;
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
