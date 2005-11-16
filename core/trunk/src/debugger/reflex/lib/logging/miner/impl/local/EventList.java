/*
 * Created on Oct 25, 2004
 */
package reflex.lib.logging.miner.impl.local;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import reflex.lib.logging.miner.impl.common.event.Event;
import tod.core.model.event.EventComparator;

/**
 * A list of events, ordered by timestamps.
 * @author gpothier
 */
public class EventList
{
	
	private List<Event> itsEvents = new ArrayList<Event>();
	
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
		
		if (size() == 0
			|| getLast().getTimestamp() <= aEvent.getTimestamp())
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
	
}
