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

import static tod.impl.dbgrid.DebuggerGridConfig.DB_EVENT_SIZE_BITS;
import static tod.impl.dbgrid.DebuggerGridConfig.DB_PAGE_POINTER_BITS;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import tod.impl.dbgrid.InternalPointer;
import tod.impl.dbgrid.db.file.HardPagedFile;
import tod.impl.dbgrid.db.file.HardPagedFile.PageBitStruct;
import tod.impl.dbgrid.messages.GridEvent;
import tod.impl.dbgrid.messages.GridMessage;
import tod.impl.dbgrid.monitoring.AggregationType;
import tod.impl.dbgrid.monitoring.Monitor;
import tod.impl.dbgrid.monitoring.Probe;

public class EventList
{
	/**
	 * Number of events stored
	 */
	private long itsEventsCount = 0;
	
	/**
	 * Size of storage (in bits) occupied by events.
	 */
	private long itsEventsSize = 0;
	
	/**
	 * Number of used pages.
	 */
	private long itsPageCount = 0;
	
	/**
	 * Id of the node that contains this event list.
	 * Serves to construct internal pointers.
	 */
	private int itsNodeId;
	
	private HardPagedFile itsFile;
	private long itsFirstPageId;
	private PageBitStruct itsCurrentBitStruct;
	
	/**
	 * Index of the last record in the current page.
	 */
	private int itsRecordIndex = 0;
	
	public EventList(int aNodeId, HardPagedFile aFile) 
	{
		Monitor.getInstance().register(this);
		itsNodeId = aNodeId;
		itsFile = aFile;
		itsCurrentBitStruct = itsFile.create().asBitStruct();
		itsFirstPageId = itsCurrentBitStruct.getPage().getPageId();
	}
	
	public void unregister()
	{
		Monitor.getInstance().unregister(this);
	}

	/**
	 * Adds an event to the events list and returns its internal pointer.
	 */
	public long add(GridEvent aEvent)
	{
		int theRecordLength = DB_EVENT_SIZE_BITS + aEvent.getBitCount();
		
		// Check available space in current page (we must leave space for the next-page pointer)
		int theTailSize = DB_EVENT_SIZE_BITS + DB_PAGE_POINTER_BITS;
		if (itsCurrentBitStruct.getRemainingBits() - theTailSize < theRecordLength)
		{
			PageBitStruct theOldBitStruct = itsCurrentBitStruct;
			itsCurrentBitStruct = itsFile.create().asBitStruct();
			long thePageId = itsCurrentBitStruct.getPage().getPageId();
			itsPageCount++;
			
			theOldBitStruct.writeInt(0, DB_EVENT_SIZE_BITS); // End-of-page marker 
			theOldBitStruct.writeLong(
					thePageId+1, 
					DB_PAGE_POINTER_BITS);
			
			itsFile.free(theOldBitStruct.getPage());
			itsRecordIndex = 0;
		}
		
		itsCurrentBitStruct.getPage().use();
		
		// Construct event pointer
		long theEventPointer = InternalPointer.create(
				itsRecordIndex,
				itsCurrentBitStruct.getPage().getPageId(), 
				itsNodeId);
		
//		System.out.println(String.format(
//				"Add event %d (%d, %d)",
//				theEventPointer,
//				itsCurrentBitStruct.getPage().getPageId(),
//				itsRecordIndex));
		
		// Write data
		itsEventsCount++;
		itsEventsSize += theRecordLength;
		
		int p0 = itsCurrentBitStruct.getPos();
		itsCurrentBitStruct.writeInt(theRecordLength, DB_EVENT_SIZE_BITS);
		aEvent.writeTo(itsCurrentBitStruct);
		int p1 = itsCurrentBitStruct.getPos();
		assert p1-p0 == theRecordLength;
		
		itsRecordIndex++;
		return theEventPointer;
	}
	
	/**
	 * Returns the event corresponding to the specified internal pointer.
	 */
	public GridEvent getEvent(long aPointer)
	{
		InternalPointer thePointer = new InternalPointer(aPointer);
		assert thePointer.getNode() == itsNodeId;
		long thePageId = thePointer.getPage();
		int theRecordIndex = thePointer.getIndex();
		return getEvent(thePageId, theRecordIndex);
	}
	
	/**
	 * Returns the event corresponding to the specified internal pointer.
	 */
	public GridEvent getEvent(long aPageId, int aRecordIndex)
	{
		int theCount = aRecordIndex;
		
		HardPagedFile.Page thePage = itsFile.get(aPageId);
		PageBitStruct theBitStruct = thePage.asBitStruct();
		
		do 
		{
			int theRecordLength = theBitStruct.readInt(DB_EVENT_SIZE_BITS);
			if (theRecordLength == 0) break; // End-of-page marker.
			
			if (theCount == 0) return (GridEvent) GridMessage.read(theBitStruct);
			
			theBitStruct.skip(theRecordLength-DB_EVENT_SIZE_BITS);
			theCount--;
			
		} while (theCount >= 0);
		
		throw new RuntimeException(String.format(
				"Event not found: (pageId: %d, recordIndex: %d)", 
				aPageId,
				aRecordIndex));
	}
	
	/**
	 * Returns an iterator on all the events of this list
	 */
	public Iterator<GridEvent> getEventIterator()
	{
		return new EventIterator(itsFile.get(itsFirstPageId).asBitStruct());
	}
	
	public int getPageSize()
	{
		return itsFile.getPageSize();
	}
	
	@Probe(key = "event pages", aggr = AggregationType.SUM)
	public long getPageCount()
	{
		return itsPageCount;
	}
	
	@Probe(key = "event storage", aggr = AggregationType.SUM)
	public long getStorageSpace()
	{
		return getPageCount() * getPageSize();
	}
	
	@Probe(key = "event count", aggr = AggregationType.SUM)
	public long getEventsCount()
	{
		return itsEventsCount;
	}
		
	@Probe(key = "event size", aggr = AggregationType.AVG)
	public float getAverageEventSize()
	{
		if (itsEventsCount == 0) return -1;
		return itsEventsSize / itsEventsCount / 8f;
	}
	
	private class EventIterator implements Iterator<GridEvent>
	{
		private PageBitStruct itsPage;
		
		private GridEvent itsNext;

		public EventIterator(PageBitStruct aPage)
		{
			itsPage = aPage;
			itsNext = readNext();
		}

		private GridEvent readNext()
		{
			int theRecordLength;
			
			do
			{
				theRecordLength = itsPage.readInt(DB_EVENT_SIZE_BITS);
				
				if (theRecordLength == 0)
				{
					// We reached the end of the page, we must read the next-page pointer
					long theNextPage = itsPage.readLong(DB_PAGE_POINTER_BITS);
					if (theNextPage == 0) return null;
					
//					itsFile.freePage(itsPage.getPage());
					itsPage = itsFile.get(theNextPage-1).asBitStruct();
				}
			} while (theRecordLength == 0); // We really should not loop more than once
			
			GridEvent theEvent = (GridEvent) GridMessage.read(itsPage);
			
			return theEvent;
		}
		
		public boolean hasNext()
		{
			return itsNext != null;
		}

		public GridEvent next()
		{
			GridEvent theResult = itsNext;
			itsNext = readNext();
			return theResult;
		}

		public void remove()
		{
			throw new UnsupportedOperationException();
		}
		
		
	}
}
