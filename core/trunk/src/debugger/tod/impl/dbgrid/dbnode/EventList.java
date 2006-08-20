/*
 * Created on Jul 23, 2006
 */
package tod.impl.dbgrid.dbnode;

import static tod.impl.dbgrid.DebuggerGridConfig.*;

import java.util.Iterator;

import tod.impl.dbgrid.dbnode.file.HardPagedFile;
import tod.impl.dbgrid.dbnode.file.HardPagedFile.PageBitStruct;
import tod.impl.dbgrid.messages.GridEvent;
import tod.impl.dbgrid.monitoring.AggregationType;
import tod.impl.dbgrid.monitoring.Monitor;
import tod.impl.dbgrid.monitoring.Probe;
import zz.utils.bit.BitUtils;

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
	
	private HardPagedFile itsFile;
	private long itsFirstPageId;
	private PageBitStruct itsCurrentBitStruct;
	
	/**
	 * Index of the last record in the current page.
	 */
	private int itsRecordIndex = 0;
	
	public EventList(HardPagedFile aFile) 
	{
		Monitor.getInstance().register(this);
		itsFile = aFile;
		itsCurrentBitStruct = itsFile.create().asBitStruct();
		itsFirstPageId = itsCurrentBitStruct.getPage().getPageId();
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
			itsPageCount++;
			
			theOldBitStruct.writeInt(0, DB_EVENT_SIZE_BITS); // End-of-page marker 
			theOldBitStruct.writeLong(itsCurrentBitStruct.getPage().getPageId()+1, DB_PAGE_POINTER_BITS);
			
//			itsFile.store(theOldBitStruct.getPage());
//			itsFile.free(theOldBitStruct.getPage());
			itsRecordIndex = 0;
		}
		
		// Construct event pointer
		long theEventPointer = makeInternalPointer(
				itsCurrentBitStruct.getPage().getPageId(), 
				itsRecordIndex);
		
//		System.out.println(String.format(
//				"Add event %d (%d, %d)",
//				theEventPointer,
//				itsCurrentBitStruct.getPage().getPageId(),
//				itsRecordIndex));
		
		// Write data
		itsEventsCount++;
		itsEventsSize += theRecordLength;
		
		itsCurrentBitStruct.writeInt(theRecordLength, DB_EVENT_SIZE_BITS);
		aEvent.writeTo(itsCurrentBitStruct);
		
		itsRecordIndex++;
		return theEventPointer;
	}
	
	private static long makeInternalPointer(long aPageId, int aRecordIndex)
	{
		long thePageMask = BitUtils.pow2(DB_EVENTID_PAGE_BITS)-1;
		if ((aPageId & ~thePageMask) != 0) throw new RuntimeException("Page Id overflow");
		
		long theIndexMask = BitUtils.pow2(DB_EVENTID_INDEX_BITS)-1;
		if ((aRecordIndex & ~theIndexMask) != 0) throw new RuntimeException("Record index overflow: "+aRecordIndex);
		
		long thePointer = aPageId << DB_EVENTID_INDEX_BITS;
		thePointer |= aRecordIndex;
		
		return thePointer;
	}
	
	/**
	 * Returns the event corresponding to the specified internal pointer.
	 */
	public GridEvent getEvent(long aPointer)
	{
		long thePageId = aPointer >>> DB_EVENTID_INDEX_BITS;
		int theRecordIndex = (int) (aPointer & (BitUtils.pow2(DB_EVENTID_INDEX_BITS)-1));
		int theCount = theRecordIndex;
		
		HardPagedFile.Page thePage = itsFile.get(thePageId);
		PageBitStruct theBitStruct = thePage.asBitStruct();
		
		do 
		{
			int theRecordLength = theBitStruct.readInt(DB_EVENT_SIZE_BITS);
			if (theRecordLength == 0) break; // End-of-page marker.
			
			if (theCount == 0) return GridEvent.create(theBitStruct);
			
			theBitStruct.skip(theRecordLength-DB_EVENT_SIZE_BITS);
			theCount--;
			
		} while (theCount >= 0);
		
		throw new RuntimeException(String.format(
				"Event not found: %d (%d, %d)", 
				aPointer,
				thePageId,
				theRecordIndex));
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
			
			GridEvent theEvent = GridEvent.create(itsPage);
			
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
