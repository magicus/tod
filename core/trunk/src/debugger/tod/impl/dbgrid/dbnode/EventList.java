/*
 * Created on Jul 23, 2006
 */
package tod.impl.dbgrid.dbnode;

import static tod.impl.dbgrid.DebuggerGridConfig.*;

import java.util.Iterator;

import tod.impl.dbgrid.dbnode.PagedFile.PageBitStruct;
import tod.impl.dbgrid.messages.GridEvent;
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
	
	private PagedFile itsFile;
	private long itsFirstPageId;
	private PageBitStruct itsCurrentBitStruct;
	
	/**
	 * Index of the last record in the current page.
	 */
	private int itsRecordIndex = 0;
	
	public EventList(PagedFile aFile) 
	{
		itsFile = aFile;
		itsCurrentBitStruct = itsFile.createPage().asBitStruct();
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
			itsCurrentBitStruct = itsFile.createPage().asBitStruct();
			
			theOldBitStruct.writeInt(0, DB_EVENT_SIZE_BITS); // End-of-page marker 
			theOldBitStruct.writeLong(itsCurrentBitStruct.getPage().getPageId()+1, DB_PAGE_POINTER_BITS);
			
			itsFile.writePage(theOldBitStruct.getPage());
//			itsFile.freePage(theOldBitStruct.getPage());
			itsRecordIndex = 0;
		}
		
		// Construct event pointer
		long theEventPointer = makeInternalPointer(itsCurrentBitStruct.getPage().getPageId(), itsRecordIndex);
		
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
		
		PagedFile.Page thePage = itsFile.getPage(thePageId);
		PageBitStruct theBitStruct = thePage.asBitStruct();
		
		do 
		{
			int theRecordLength = theBitStruct.readInt(DB_EVENT_SIZE_BITS);
			if (theRecordLength == 0) break; // End-of-page marker.
			
			if (theRecordIndex == 0) return GridEvent.create(theBitStruct);
			
			theBitStruct.skip(theRecordLength-DB_EVENT_SIZE_BITS);
			theRecordIndex--;
			
		} while (theRecordIndex > 0);
		
		throw new RuntimeException("Event not found: "+aPointer);
	}
	
	/**
	 * Returns an iterator on all the events of this list
	 */
	public Iterator<GridEvent> getEventIterator()
	{
		return new EventIterator(itsFile.getPage(itsFirstPageId).asBitStruct());
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
					itsPage = itsFile.getPage(theNextPage-1).asBitStruct();
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
