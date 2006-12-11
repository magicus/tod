/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier (gpothier -at- dcc . uchile . cl)

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
package tod.impl.dbgrid.dbnode;

import java.util.ArrayList;
import java.util.List;

import tod.impl.dbgrid.DebuggerGridConfig;
import tod.impl.dbgrid.messages.GridEvent;
import zz.utils.RingBuffer;

/**
 * A buffer that permits to reorder incoming events.
 * @author gpothier
 */
public class EventReorderingBuffer
{
	private long itsLastPushed;
	private RingBuffer<GridEvent> itsBuffer = new RingBuffer<GridEvent>(DebuggerGridConfig.DB_EVENT_BUFFER_SIZE);
	private OutOfOrderBuffer itsOutOfOrderBuffer = new OutOfOrderBuffer();
	
	private EventDatabase itsDatabase;
	
	public EventReorderingBuffer(EventDatabase aDatabase)
	{
		itsDatabase = aDatabase;
	}

	/**
	 * Pushes an incoming event into this buffer.
	 */
	public void push(GridEvent aEvent)
	{
		long theTimestamp = aEvent.getTimestamp();
		if (theTimestamp < itsLastPushed)
		{
			// Out of order event.
			itsOutOfOrderBuffer.add(aEvent);
		}
		else
		{
			itsLastPushed = theTimestamp;
			itsBuffer.add(aEvent);
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
	public GridEvent pop()
	{
		if (itsBuffer.isEmpty())
		{
			return itsOutOfOrderBuffer.next();
		}
		else
		{
			GridEvent theInOrderEvent = itsBuffer.peek();
			long theNextOutOfOrder = itsOutOfOrderBuffer.getNextAvailable();
			if (theNextOutOfOrder < theInOrderEvent.getTimestamp())
			{
				return itsOutOfOrderBuffer.next();
			}
			else
			{
				GridEvent theEvent = itsBuffer.remove();
				assert theEvent == theInOrderEvent;
				return theEvent;
			}
		}
	}
	
	/**
	 * Buffer for events that arrived late
	 * @author gpothier
	 */
	private class OutOfOrderBuffer
	{
		private List<PerThreadBuffer> itsBuffers = new ArrayList<PerThreadBuffer>();
		
		private GridEvent itsNextAvailable;
		
		public void add(GridEvent aEvent)
		{
			int theThread = aEvent.getThread();
			
			PerThreadBuffer theBuffer = null; 
			while (itsBuffers.size() < theThread+1)
			{
				theBuffer = new PerThreadBuffer();
				itsBuffers.add(theBuffer);
			}
			if (theBuffer == null) theBuffer = itsBuffers.get(theThread);
			
			theBuffer.add(aEvent);
			
			if (itsNextAvailable == null) itsNextAvailable = aEvent;
		}
		
		public boolean isEmpty()
		{
			return itsNextAvailable == null;
		}
		
		public long getNextAvailable()
		{
			return itsNextAvailable != null
					? itsNextAvailable.getTimestamp() 
					: Long.MAX_VALUE;
		}
		
		public GridEvent next()
		{
			// Advance the buffer that contained the next event
			PerThreadBuffer theNextBuffer = itsBuffers.get(itsNextAvailable.getThread());
			GridEvent theNextEvent = theNextBuffer.remove();
			assert theNextEvent == itsNextAvailable;

			// Search next event
			itsNextAvailable = null;
			long theNextTimestamp = Long.MAX_VALUE;
			for (PerThreadBuffer theBuffer : itsBuffers)
			{
				if (theBuffer == null || theBuffer.isEmpty()) continue;
				
				GridEvent theEvent = theBuffer.peek();
				long theTimestamp = theEvent.getTimestamp();
				if (theTimestamp < theNextTimestamp)
				{
					theNextTimestamp = theTimestamp;
					itsNextAvailable = theEvent;
				}
			}
			
			return theNextEvent;
		}
	}
	
	private class PerThreadBuffer extends RingBuffer<GridEvent>
	{
		private long itsLastAdded;
		
		public PerThreadBuffer()
		{
			super(DebuggerGridConfig.DB_EVENT_BUFFER_SIZE);
		}
		
		public void add(GridEvent aEvent)
		{
			long theTimestamp = aEvent.getTimestamp();
			if (theTimestamp < itsLastAdded) 
			{
				itsDatabase.eventDropped();
				return;
			}
			
			itsLastAdded = theTimestamp;
			super.add(aEvent);
		}
	}
}
