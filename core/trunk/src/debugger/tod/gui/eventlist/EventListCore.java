/*
Copyright (c) 2006-2008, Guillaume Pothier
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this 
      list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, 
      this list of conditions and the following disclaimer in the documentation 
      and/or other materials provided with the distribution.
    * Neither the name of the University of Chile nor the names of its contributors 
      may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
POSSIBILITY OF SUCH DAMAGE.

Parts of this work rely on the MD5 algorithm "derived from the RSA Data Security, 
Inc. MD5 Message-Digest Algorithm".
*/
package tod.gui.eventlist;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import tod.core.database.browser.IEventBrowser;
import tod.core.database.event.ILogEvent;
import zz.utils.Utils;

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
	 * The returned list is a copy.
	 */
	public synchronized List<ILogEvent> getDisplayedEvents()
	{
		List<ILogEvent> theEvents = new ArrayList<ILogEvent>(itsDisplayedEvents.size());
		Utils.fillCollection(theEvents, itsDisplayedEvents);
		return theEvents;
	}

	/**
	 * Resets this list core with the given browser and first timestamp.
	 */
	public synchronized void setBrowser(IEventBrowser aBrowser)
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
	public synchronized void setTimestamp(long aTimestamp)
	{
		itsDisplayedEvents.clear();
		
		itsBrowser.setNextTimestamp(aTimestamp);
		itsCurrentDelta = 0;
		
		for(int i=0;i<itsVisibleEvents && itsBrowser.hasNext();i++)
		{
			ILogEvent theEvent = itsBrowser.next();
			itsDisplayedEvents.add(theEvent);
			itsCurrentDelta++;
		}
	}
	
	private synchronized void backward()
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
	
	private synchronized void forward()
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
	public synchronized void forward(int aCount)
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
	public synchronized void backward(int aCount)
	{
		for (int i=0;i<aCount;i++)
		{
			backward();
		}		
	}
	
	/**
	 * Augments the number of visible events by one, and returns the last visible event.
	 */
	public synchronized ILogEvent incVisibleEvents()
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
