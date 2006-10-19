/*
 * Created on Oct 27, 2004
 */
package tod.impl.local;

import java.util.ArrayList;
import java.util.List;

import tod.core.database.browser.IEventBrowser;
import tod.core.database.event.EventComparator;
import tod.core.database.event.ILogEvent;
import tod.impl.local.filter.AbstractFilter;

/**
 * @author gpothier
 */
public class EventBrowser implements IEventBrowser
{
	private List<ILogEvent> itsEvents = new ArrayList<ILogEvent>();
	
	private int itsIndex;
	
	public EventBrowser(List<ILogEvent> aEvents)
	{
		itsEvents = aEvents;
	}
	
	public EventBrowser(EventList aEventList, AbstractFilter aFilter)
	{
		for (int i=0;i<aEventList.size();i++)
		{
			ILogEvent theEvent = aEventList.get(i);
			if (aFilter.accept(theEvent)) 
			{
				theEvent = getEvent(theEvent);
				itsEvents.add (theEvent);
			}
		}
	}
	
	/**
	 * Returns the event that should be included in the list
	 * given a source event.
	 */
	protected ILogEvent getEvent (ILogEvent aSourceEvent)
	{
		return aSourceEvent;
	}
	
	public boolean hasNext()
	{
		return itsIndex < itsEvents.size();
	}
	
	public boolean hasPrevious()
	{
		return itsIndex > 0;
	}
	
	public ILogEvent next()
	{
		return getEvent(itsIndex++);
	}
	
	public ILogEvent previous()
	{
		return getEvent(--itsIndex);
	}
	
	/**
	 * Returns the event at a specified index.
	 */
	public ILogEvent getEvent (int aIndex)
	{
		return itsEvents.get (aIndex);
	}
	
	public void setNextTimestamp(long aTimestamp)
	{
		itsIndex = EventComparator.indexOf(aTimestamp, itsEvents);
		while (hasPrevious() && itsEvents.get (itsIndex-1).getTimestamp() == aTimestamp) itsIndex--;
	}
	
	public void setPreviousTimestamp(long aTimestamp)
	{
		itsIndex = EventComparator.indexOf(aTimestamp, itsEvents);
		while (hasNext() && itsEvents.get(itsIndex).getTimestamp() == aTimestamp)
		{
			itsIndex++;
		} 
	}
	
	
	
	public void setNextEvent(ILogEvent aEvent)
	{
		itsIndex = EventComparator.indexOf(aEvent, itsEvents);
	}

	public void setPreviousEvent(ILogEvent aEvent)
	{
		itsIndex = EventComparator.indexOf(aEvent, itsEvents)+1;
	}

	public long getEventCount()
	{
		return itsEvents.size();
	}

	public long getEventCount(long aT1, long aT2, boolean aForceMergeCounts)
	{
		long theCount = 0;
		
		for (ILogEvent theEvent : itsEvents)
		{
			long theTimestamp = theEvent.getTimestamp();
			if (theTimestamp < aT1) continue;
			if (theTimestamp > aT2) break;
			theCount++;
		}
		
		return theCount;
	}

	public long[] getEventCounts(long aT1, long aT2, int aSlotsCount, boolean aForceMergeCounts)
	{
		long[] theCounts = new long[aSlotsCount];
		
		for (ILogEvent theEvent : itsEvents)
		{
			long theTimestamp = theEvent.getTimestamp();
			if (theTimestamp < aT1) continue;
			if (theTimestamp >= aT2) break;

			int theSlot = (int)(((theTimestamp - aT1) * aSlotsCount) / (aT2 - aT1));
			theCounts[theSlot]++;
		}
		
		return theCounts;
	}
	
	
	public List<ILogEvent> getEvents(long aT1, long aT2)
	{
		List<ILogEvent> theResult = new ArrayList<ILogEvent>();
		
		for (ILogEvent theEvent : itsEvents)
		{
			long theTimestamp = theEvent.getTimestamp();
			if (theTimestamp < aT1) continue;
			if (theTimestamp > aT2) break;

			theResult.add (theEvent);
		}
		
		return theResult;
	}
	
	
	
}
