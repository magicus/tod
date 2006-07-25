/*
 * Created on Jul 20, 2006
 */
package tod.impl.dbgrid.dbnode;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Comparator;
import java.util.List;

import tod.impl.dbgrid.DebuggerGridConfig;
import tod.impl.dbgrid.messages.AddChildEvent;
import tod.impl.dbgrid.messages.GridEvent;
import tod.impl.dbgrid.messages.GridMessage;
import zz.utils.SortedRingBuffer;

public class DatabaseNode
{
	private final EventList itsEventList;
	private final Indexes itsIndexes;
	
	/**
	 * Timestamp of the last processed event
	 */
	private long itsLastProcessedTimestamp;
	private SortedRingBuffer<GridEvent> itsEventBuffer = 
		new SortedRingBuffer<GridEvent>(16, new EventTimestampComparator());
	
	public DatabaseNode()
	{
		try
		{
			itsEventList = new EventList(new PagedFile(
					new File("events.bin"), 
					DebuggerGridConfig.DB_EVENT_PAGE_SIZE));
			
			itsIndexes = new Indexes(new PagedFile(
					new File("indexes.bin"),
					DebuggerGridConfig.DB_INDEX_PAGE_SIZE));
		}
		catch (FileNotFoundException e)
		{
			throw new RuntimeException(e);
		}
	}

	public void push(List<GridMessage> aMessagesList)
	{
		for (GridMessage theMessage : aMessagesList)
		{
			if (theMessage instanceof GridEvent)
			{
				GridEvent theEvent = (GridEvent) theMessage;
				addEvent(theEvent);
			}
			else if (theMessage instanceof AddChildEvent)
			{
				AddChildEvent theEvent = (AddChildEvent) theMessage;
				processAddChildEvent(theEvent);
			}
			else throw new RuntimeException("Not handled: "+theMessage);
		}
	}
	
	private void addEvent(GridEvent aEvent)
	{
		if (itsEventBuffer.isFull()) processEvent(itsEventBuffer.remove());
		itsEventBuffer.add(aEvent);
	}
	
	private void processEvent(GridEvent aEvent)
	{
		if (aEvent.getTimestamp() < itsLastProcessedTimestamp)
		{
			throw new RuntimeException("Out of order events");
		}
		itsLastProcessedTimestamp = aEvent.getTimestamp();
		
		long theId = itsEventList.add(aEvent);
		aEvent.index(itsIndexes, theId);		
	}
	
	private void processAddChildEvent(AddChildEvent aMessage)
	{
		
	}
	
	private static class EventTimestampComparator implements Comparator<GridEvent>
	{
		public int compare(GridEvent aEvent1, GridEvent aEvent2)
		{
			long theDelta = aEvent1.getTimestamp() - aEvent2.getTimestamp();
			if (theDelta == 0) return 0;
			else if (theDelta > 0) return 1;
			else return -1;
		}
	}
}
