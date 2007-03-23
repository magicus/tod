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
package tod.gui.eventlist;

import java.util.LinkedList;

import tod.core.database.browser.IEventBrowser;
import tod.core.database.event.ILogEvent;

/**
 * Implements the logic of {@link IEventBrowser}-based event lists.
 * Supports backward and forward unit scrolling and absolute
 * tracking.
 * @author gpothier
 */
public class EventListCore
{
	private IEventBrowser itsBrowser;
	
	private long itsFirstTimestamp;
	private long itsLastTimestamp;
	
	/**
	 * Delta between the first displayed event and the browser's
	 * current event.
	 */
	private int itsCurrentDelta = 0;
	
	private LinkedList<ILogEvent> itsDisplayedEvents = new LinkedList<ILogEvent>();
	
	/**
	 * Number of visible events (according ie. to the window's size). 
	 */
	private int itsVisibleEvents;
	
	/**
	 * This permits to go "beyond the end" of the list so that the last
	 * event can be displayed fully in a GUI.
	 */
	private int itsAllowedOverflow = 3;
	
	/**
	 * Constructs an {@link EventListCore} taking the first and last event available
	 * in the specified browser as bounds.
	 */
	public EventListCore(IEventBrowser aBrowser, int aVisibleEvents)
	{
		itsVisibleEvents = aVisibleEvents;
		setBrowser(aBrowser);		
	}
	
	public long getFirstTimestamp()
	{
		return itsFirstTimestamp;
	}

	public long getLastTimestamp()
	{
		return itsLastTimestamp;
	}

	public int getVisibleEvents()
	{
		return itsVisibleEvents;
	}

	/**
	 * Returns the list of events that should be displayed.
	 */
	public LinkedList<ILogEvent> getDisplayedEvents()
	{
		return itsDisplayedEvents;
	}

	/**
	 * Resets this list core with the given browser and first timestamp.
	 */
	public void setBrowser(IEventBrowser aBrowser)
	{
		itsBrowser = aBrowser;
		
		itsFirstTimestamp = itsBrowser.getFirstTimestamp();
		itsLastTimestamp = itsBrowser.getLastTimestamp();

		setTimestamp(itsFirstTimestamp);
	}
	
	/**
	 * Updates the displayed events so that the first displayed
	 * event is at or immediately after the specified timestamp. 
	 */
	public void setTimestamp(long aTimestamp)
	{
		itsDisplayedEvents.clear();
		
		itsBrowser.setNextTimestamp(aTimestamp);
		itsCurrentDelta = 0;
		
		for(int i=0;i<itsVisibleEvents && itsBrowser.hasNext();i++)
		{
			itsDisplayedEvents.add(itsBrowser.next());
			itsCurrentDelta++;
		}
	}
	
	private void backward()
	{
		while (itsCurrentDelta > 0)
		{
			ILogEvent theEvent = itsBrowser.previous();
			itsCurrentDelta--;
			
			// Check consistency
			if (itsDisplayedEvents.size() > itsCurrentDelta)
			{
				ILogEvent theDisplayedEvent = itsDisplayedEvents.get(itsCurrentDelta);
				assert theDisplayedEvent.getPointer().equals(theEvent.getPointer());
			}
		}
		
		if (itsBrowser.hasPrevious())
		{
			assert itsCurrentDelta == 0;
			ILogEvent theEvent = itsBrowser.previous();
			
			itsDisplayedEvents.addFirst(theEvent);
			if (itsDisplayedEvents.size() > itsVisibleEvents) 
				itsDisplayedEvents.removeLast();
		}
	}
	
	private void forward()
	{
		while (itsCurrentDelta < itsVisibleEvents)
		{
			if (! itsBrowser.hasNext()) break;
			
			ILogEvent theEvent = itsBrowser.next();
			
			// Check consistency
			if (itsDisplayedEvents.size() > itsCurrentDelta)
			{
				ILogEvent theDisplayedEvent = itsDisplayedEvents.get(itsCurrentDelta);
				assert theDisplayedEvent.getPointer().equals(theEvent.getPointer());
			}
			
			itsCurrentDelta++;
		}
		
		// This could happen if the number of visible events has reduced
		while (itsCurrentDelta > itsVisibleEvents)
		{
			ILogEvent theEvent = itsBrowser.previous();
			itsCurrentDelta--;
			
			// Check consistency
			if (itsDisplayedEvents.size() > itsCurrentDelta)
			{
				ILogEvent theDisplayedEvent = itsDisplayedEvents.get(itsCurrentDelta);
				assert theDisplayedEvent.getPointer().equals(theEvent.getPointer());
			}
		}
		
		if (itsCurrentDelta == itsVisibleEvents)
		{
			if (itsBrowser.hasNext())
			{
				ILogEvent theEvent = itsBrowser.next();
				itsCurrentDelta++;
				
				itsDisplayedEvents.addLast(theEvent);
			}
		}

		if (itsDisplayedEvents.size() > itsAllowedOverflow)
		{
			itsDisplayedEvents.removeFirst();
			itsCurrentDelta--;
		}
	}
	
	/**
	 * Moves the displayed events list forward by the specified number
	 * of events
	 */
	public void forward(int aCount)
	{
		for (int i=0;i<aCount;i++)
		{
			forward();
		}
	}
	
	/**
	 * Moves the displayed events list backward by the specified number
	 * of events
	 */
	public void backward(int aCount)
	{
		for (int i=0;i<aCount;i++)
		{
			backward();
		}		
	}
	
	/**
	 * Augments the number of visible events by one, and returns the last visible event.
	 */
	public ILogEvent incVisibleEvents()
	{
		while (itsCurrentDelta < itsVisibleEvents)
		{
			if (! itsBrowser.hasNext()) break;
			
			ILogEvent theEvent = itsBrowser.next();
			
			// Check consistency
			if (itsDisplayedEvents.size() > itsCurrentDelta)
			{
				ILogEvent theDisplayedEvent = itsDisplayedEvents.get(itsCurrentDelta);
				assert theDisplayedEvent.getPointer().equals(theEvent.getPointer());
			}
			
			itsCurrentDelta++;
		}

		if (itsCurrentDelta < itsVisibleEvents) return null;
		
		itsVisibleEvents++;
		if (itsBrowser.hasNext())
		{
			ILogEvent theEvent = itsBrowser.next();
			itsCurrentDelta++;
			
			itsDisplayedEvents.addLast(theEvent);
			return theEvent;
		}
		else return null;
	}
}
