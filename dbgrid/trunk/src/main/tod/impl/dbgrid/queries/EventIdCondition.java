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
package tod.impl.dbgrid.queries;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import tod.core.database.browser.ICompoundFilter;
import tod.core.database.browser.IEventFilter;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.event.ILogEvent;
import tod.impl.dbgrid.aggregator.IGridEventBrowser;
import tod.impl.local.EventBrowser;

/**
 * A "pseudo-condition" that accepts only one event,
 * or 0 event if the given event is null.
 * @author gpothier
 */
public class EventIdCondition implements IEventFilter, ICompoundFilter
{
	private final ILogBrowser itsLogBrowser;
	private final ILogEvent itsEvent;

	public EventIdCondition(ILogBrowser aLogBrowser, ILogEvent aEvent)
	{
		itsLogBrowser = aLogBrowser;
		itsEvent = aEvent;
	}
	
	public IGridEventBrowser createBrowser()
	{
		List<ILogEvent> theEvents = new ArrayList<ILogEvent>();
		if (itsEvent != null) theEvents.add(itsEvent);
		return new MyBrowser(itsLogBrowser, theEvents);
	}
	
	public ILogEvent getEvent()
	{
		return itsEvent;
	}
	
	public void add(IEventFilter aFilter) throws IllegalStateException
	{
		throw new UnsupportedOperationException();
	}

	public List<IEventFilter> getFilters()
	{
		throw new UnsupportedOperationException();
	}

	public void remove(IEventFilter aFilter) throws IllegalStateException
	{
		throw new UnsupportedOperationException();
	}

	private static class MyBrowser extends EventBrowser implements IGridEventBrowser
	{
		/**
		 * To simplify the implementation we only allow bounds to be set once.
		 */
		private boolean itsBoundsSet = false;
		
		public MyBrowser(ILogBrowser aLogBrowser, List<ILogEvent> aEvents)
		{
			super(aLogBrowser, aEvents);
		}

		public void setBounds(ILogEvent aFirstEvent, ILogEvent aLastEvent)
		{
			if (itsBoundsSet) throw new UnsupportedOperationException();
			itsBoundsSet = true;
			
			List<ILogEvent> theEvents = getEvents();
			Iterator<ILogEvent> theIterator = theEvents.iterator();
			while (theIterator.hasNext())
			{
				ILogEvent theEvent = theIterator.next();
				if ((aFirstEvent != null && theEvent.getTimestamp() < aFirstEvent.getTimestamp())
						|| (aLastEvent != null && theEvent.getTimestamp() > aLastEvent.getTimestamp())) 
				{
					theIterator.remove();
				}
			}
		}
	}
}