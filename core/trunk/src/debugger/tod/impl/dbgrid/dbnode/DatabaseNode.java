/*
 * Created on Jul 20, 2006
 */
package tod.impl.dbgrid.dbnode;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import static tod.impl.dbgrid.DebuggerGridConfig.*;
import tod.impl.dbgrid.messages.AddChildEvent;
import tod.impl.dbgrid.messages.GridEvent;
import tod.impl.dbgrid.messages.GridMessage;
import tod.impl.dbgrid.monitoring.Monitor;
import tod.impl.dbgrid.queries.EventCondition;
import zz.utils.SortedRingBuffer;

public class DatabaseNode
{
	/**
	 * Id of this node in the system
	 */
	private final int itsNodeId;
	
	private final PagedFile itsEventsFile;
	private final PagedFile itsIndexesFile;
	private final PagedFile itsCFlowDataFile;
	
	private final EventList itsEventList;
	private final Indexes itsIndexes;
	private final CFlowMap itsCFlowMap;
	
	/**
	 * Timestamp of the last processed event
	 */
	private long itsLastProcessedTimestamp;
	
	private SortedRingBuffer<GridEvent> itsEventBuffer = 
		new SortedRingBuffer<GridEvent>(DB_EVENT_BUFFER_SIZE, new EventTimestampComparator());
	
	private boolean itsFlushed = false;
	
	public DatabaseNode(int aNodeId)
	{
		Monitor.getInstance().register(this);
		itsNodeId = aNodeId;
		try
		{
			itsEventsFile = new PagedFile(new File("events.bin"), DB_EVENT_PAGE_SIZE);
			itsIndexesFile = new PagedFile(new File("indexes.bin"), DB_INDEX_PAGE_SIZE);
			itsCFlowDataFile = new PagedFile(new File("cflow.bin"), DB_CFLOW_PAGE_SIZE);
			
			itsEventList = new EventList(itsEventsFile);
			itsIndexes = new Indexes(itsIndexesFile);
			itsCFlowMap = new CFlowMap(this, itsIndexesFile, itsCFlowDataFile);
		}
		catch (FileNotFoundException e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns the id of this node
	 */
	public int getNodeId()
	{
		return itsNodeId;
	}

	public Indexes getIndexes()
	{
		return itsIndexes;
	}
	
	/**
	 * Creates an iterator over matching events of this node, starting at the specified timestamp.
	 */
	public Iterator<GridEvent> evaluate(EventCondition aCondition, long aTimestamp)
	{
		return aCondition.createIterator(itsEventList, getIndexes(), aTimestamp);
	}

	/**
	 * Pushes a list of messages to this node.
	 * @see #push(GridMessage) 
	 */
	public void push(List<GridMessage> aMessagesList)
	{
		for (GridMessage theMessage : aMessagesList) push(theMessage);
	}

	/**
	 * Pushes a single message to this node.
	 * Messages can be events or parent/child
	 * relations.
	 */
	public void push(GridMessage aMessage)
	{
		assert ! itsFlushed;
		
		if (aMessage instanceof GridEvent)
		{
			GridEvent theEvent = (GridEvent) aMessage;
			addEvent(theEvent);
		}
		else if (aMessage instanceof AddChildEvent)
		{
			AddChildEvent theEvent = (AddChildEvent) aMessage;
			processAddChildEvent(theEvent);
		}
		else throw new RuntimeException("Not handled: "+aMessage);
	}
	
	/**
	 * Flushes the event buffer. Events should not be added
	 * after this method is called.
	 */
	public void flush()
	{
		while (! itsEventBuffer.isEmpty()) processEvent(itsEventBuffer.remove());
		itsFlushed = true;
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
		itsCFlowMap.add(aMessage.getParentPointer(), aMessage.getChildPointer());
	}
	
	/**
	 * Returns the amount of disk storage used by this node.
	 */
	public long getStorageSpace()
	{
		return itsEventsFile.getStorageSpace() + itsIndexesFile.getStorageSpace();
	}
	
	public long getEventsCount()
	{
		return itsEventList.getEventsCount();
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
