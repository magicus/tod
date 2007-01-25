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
package tod.impl.dbgrid.event;

import tod.core.database.browser.ICompoundFilter;
import tod.core.database.browser.IEventBrowser;
import tod.core.database.event.IBehaviorExitEvent;
import tod.core.database.event.ILogEvent;
import tod.impl.dbgrid.GridLogBrowser;
import tod.impl.dbgrid.aggregator.GridEventBrowser;

public abstract class BehaviorCallEvent extends tod.impl.common.event.BehaviorCallEvent
{
	private IBehaviorExitEvent itsExitEvent;
	private boolean itsHasRealChildren;
	private ILogEvent itsFirstChild;
	private ILogEvent itsLastChild;
	
	private boolean itsChildrenInitialized = false;

	public BehaviorCallEvent(GridLogBrowser aLogBrowser)
	{
		super(aLogBrowser);
	}

	/**
	 * Initialize exit event and hasRealChildren flag.
	 */
	private void initChildren()
	{
		// Find the behavior exit event.
		// First, find next event at the same depth
		ICompoundFilter theFilter = getLogBrowser().createIntersectionFilter(
				getLogBrowser().createHostFilter(getHost()),
				getLogBrowser().createThreadFilter(getThread()),
				getLogBrowser().createDepthFilter(getDepth()));
		
		IEventBrowser theBrowser = getLogBrowser().createBrowser(theFilter);
		boolean theFound = theBrowser.setPreviousEvent(this);
		assert theFound;
		
		if (theBrowser.hasNext())
		{
			// We found the next event at the same depth, so the previous
			// event (at any depth) should be the exit event.
			ILogEvent theNextEvent = theBrowser.next();
			theFilter = getLogBrowser().createIntersectionFilter(
					getLogBrowser().createHostFilter(getHost()),
					getLogBrowser().createThreadFilter(getThread()));
			
			theBrowser = getLogBrowser().createBrowser(theFilter);
			theFound = theBrowser.setNextEvent(theNextEvent);
			assert theFound;
			
			ILogEvent theEvent = theBrowser.previous();
			assert theEvent.getDepth() == getDepth()+1;
			assert theEvent.getTimestamp() >= getTimestamp();
			assert theEvent.getParentPointer().timestamp == getTimestamp();
			
			itsLastChild = theEvent;
			if (theEvent instanceof IBehaviorExitEvent)
			{
				itsExitEvent = (IBehaviorExitEvent) theEvent;
			}
		}
		else
		{
			// This event was the last event at this depth, so we must
			// find the last event of the thread.
			theFilter = getLogBrowser().createIntersectionFilter(
					getLogBrowser().createHostFilter(getHost()),
					getLogBrowser().createThreadFilter(getThread()));
			
			theBrowser = getLogBrowser().createBrowser(theFilter);
			theBrowser.setPreviousTimestamp(Long.MAX_VALUE);
			ILogEvent theEvent = theBrowser.previous();
			if (theEvent.getDepth() == getDepth()+1)
			{
				assert theEvent.getParentPointer().timestamp == getTimestamp();
				itsLastChild = theEvent;
				if (theEvent instanceof IBehaviorExitEvent)
				{
					itsExitEvent = (IBehaviorExitEvent) theEvent;
				}
			}
		}
		
		// Find out if we have real children
		theFilter = getLogBrowser().createIntersectionFilter(
				getLogBrowser().createHostFilter(getHost()),
				getLogBrowser().createThreadFilter(getThread()),
				getLogBrowser().createDepthFilter(getDepth()+1));
		
		theBrowser = getLogBrowser().createBrowser(theFilter);
		theBrowser.setPreviousEvent(this);
		if (theBrowser.hasNext())
		{
			ILogEvent theEvent = theBrowser.next();
			assert theEvent.getParentPointer().timestamp == getTimestamp();
			
			itsFirstChild = theEvent;
			itsHasRealChildren = ! itsFirstChild.equals(itsExitEvent);
		}
		else itsHasRealChildren = false;
		
		itsChildrenInitialized = true;
	}

	public boolean hasRealChildren()
	{
		if (! itsChildrenInitialized) initChildren();
		return itsHasRealChildren;
	}

	public IEventBrowser getChildrenBrowser()
	{
		if (! itsChildrenInitialized) initChildren();
		GridLogBrowser theLogBrowser = (GridLogBrowser) getLogBrowser();
		
		ICompoundFilter theFilter = theLogBrowser.createIntersectionFilter(
				theLogBrowser.createHostFilter(getHost()),
				theLogBrowser.createThreadFilter(getThread()),
				theLogBrowser.createDepthFilter(getDepth()+1));
		
		GridEventBrowser theBrowser = theLogBrowser.createBrowser(theFilter);
		theBrowser.setBounds(itsFirstChild, itsLastChild);
		
		return theBrowser;
	}

	public IBehaviorExitEvent getExitEvent()
	{
		if (! itsChildrenInitialized) initChildren();
		return itsExitEvent;
	}
	
	


}
