/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier

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

Contact: gpothier -at- dcc . uchile . cl
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
