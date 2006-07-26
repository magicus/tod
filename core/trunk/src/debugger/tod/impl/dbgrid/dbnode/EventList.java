/*
 * Created on Jul 23, 2006
 */
package tod.impl.dbgrid.dbnode;

import tod.impl.dbgrid.DebuggerGridConfig;
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
	private PageBitStruct itsCurrentBitStruct;
	
	/**
	 * Index of the last record in the current page.
	 */
	private int itsRecordIndex = 0;
	
	public EventList(PagedFile aFile) 
	{
		itsFile = aFile;
		itsCurrentBitStruct = itsFile.createPage().asBitStruct();
	}

	/**
	 * Adds an event to the events list and returns its internal pointer.
	 */
	public long add(GridEvent aEvent)
	{
		int theRecordLength = DebuggerGridConfig.DB_EVENT_SIZE_BITS + aEvent.getBitCount();
		
		// Check available space in current page (we must leave space for the next-page pointer)
		int theTailSize = DebuggerGridConfig.DB_EVENT_SIZE_BITS + 64;
		if (itsCurrentBitStruct.getRemainingBits() - theTailSize < theRecordLength)
		{
			PageBitStruct theOldBitStruct = itsCurrentBitStruct;
			itsCurrentBitStruct = itsFile.createPage().asBitStruct();
			
			theOldBitStruct.writeInt(0, DebuggerGridConfig.DB_EVENT_SIZE_BITS); // End-of-page marker 
			theOldBitStruct.writeLong(itsCurrentBitStruct.getPage().getPageId(), 64);
			
			itsFile.writePage(theOldBitStruct.getPage());
//			itsFile.freePage(theOldBitStruct.getPage());
			itsRecordIndex = 0;
		}
		
		// Construct event pointer
		long thePageId = itsCurrentBitStruct.getPage().getPageId();
		long thePageMask = BitUtils.pow2(DebuggerGridConfig.DB_EVENTID_PAGE_BITS)-1;
		if ((thePageId & thePageMask) != 0) throw new RuntimeException("Page Id overflow");
		
		long theIndexMask = BitUtils.pow2(DebuggerGridConfig.DB_EVENTID_INDEX_BITS)-1;
		if ((itsRecordIndex & theIndexMask) != 0) throw new RuntimeException("Record index overflow");
		
		long theEventPointer = thePageId << DebuggerGridConfig.DB_EVENTID_INDEX_BITS;
		theEventPointer |= itsRecordIndex;
		
		// Write data
		itsEventsCount++;
		itsEventsSize += theRecordLength;
		
		itsCurrentBitStruct.writeInt(theRecordLength, DebuggerGridConfig.DB_EVENT_SIZE_BITS);
		aEvent.writeTo(itsCurrentBitStruct);
		
		itsRecordIndex++;
		return theEventPointer;
	}
	
	/**
	 * Returns the event corresponding to the specified internal pointer.
	 */
	public GridEvent getEvent(long aPointer)
	{
		long thePageId = aPointer >>> DebuggerGridConfig.DB_EVENTID_INDEX_BITS;
		int theRecordIndex = (int) (aPointer & (BitUtils.pow2(DebuggerGridConfig.DB_EVENTID_INDEX_BITS)-1));
		
		PagedFile.Page thePage = itsFile.getPage(thePageId);
		PageBitStruct theBitStruct = thePage.asBitStruct();
		
		do 
		{
			int theRecordLength = theBitStruct.readInt(DebuggerGridConfig.DB_EVENT_SIZE_BITS);
			if (theRecordLength == 0) break; // End-of-page marker.
			
			if (theRecordIndex == 0) return GridEvent.create(theBitStruct);
			
			theBitStruct.skip(theRecordLength-DebuggerGridConfig.DB_EVENT_SIZE_BITS);
			theRecordIndex--;
			
		} while (theRecordIndex > 0);
		
		throw new RuntimeException("Event not found: "+aPointer);
	}
}
