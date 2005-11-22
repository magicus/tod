/*
 * Created on Oct 25, 2004
 */
package tod.impl.local;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import tod.core.model.event.EventComparator;
import tod.impl.local.event.Event;

/**
 * A list of events, ordered by timestamps.
 * @author gpothier
 */
public class EventList
{
	private List<Event> itsEvents = new ArrayList<Event>();
	private long itsFirstTimestamp = Long.MAX_VALUE;
	private long itsLastTimestamp = 0;
	
	public void clear()
	{
		itsEvents.clear();
	}
	
	/**
	 * Adds the specified event to this list.
	 * @return The index at which the event is inserted
	 */
	public int add (Event aEvent)
	{
		int theIndex;
		
		long theTimestamp = aEvent.getTimestamp();
		
		if (size() == 0
			|| getLast().getTimestamp() <= theTimestamp)
		{
			theIndex = size();
		}
		else
		{
			theIndex = Collections.binarySearch(
					itsEvents, 
					aEvent, 
					EventComparator.getInstance());
			
			if (theIndex < 0) theIndex = -theIndex-1;
		}
		
		itsEvents.add (theIndex, aEvent);
		
		if (theTimestamp > 0)
		{
			itsFirstTimestamp = Math.min(itsFirstTimestamp, theTimestamp);
			itsLastTimestamp = Math.max(itsLastTimestamp, theTimestamp);
		}
		
		return theIndex;
	}
	
	public Event getLast()
	{
		return itsEvents.get (itsEvents.size()-1);
	}
	
	public int size()
	{
		return itsEvents.size();
	}

	public Event get (int aIndex)
	{
		return itsEvents.get(aIndex);
	}

	public long getFirstTimestamp()
	{
		return itsFirstTimestamp;
	}

	public long getLastTimestamp()
	{
		return itsLastTimestamp;
	}
	
}
