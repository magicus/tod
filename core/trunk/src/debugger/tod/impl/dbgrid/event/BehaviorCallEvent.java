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

import java.io.Serializable;

import tod.core.database.browser.ICompoundFilter;
import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.IEventFilter;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.event.ExternalPointer;
import tod.core.database.event.IBehaviorExitEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IThreadInfo;
import tod.impl.dbgrid.GridLogBrowser;
import tod.impl.dbgrid.aggregator.GridEventBrowser;
import zz.utils.ITask;

public abstract class BehaviorCallEvent extends tod.impl.common.event.BehaviorCallEvent
{
	private CallInfo itsCallInfo = null;
	
	/**
	 * The first (direct) child event of the call
	 */
	private ILogEvent itsFirstChild;
	
	/**
	 * The last (direct) child event of the call
	 */
	private ILogEvent itsLastChild;
	
	/**
	 * The behavior exit event corresponding to the call
	 */
	private IBehaviorExitEvent itsExitEvent;
	

	public BehaviorCallEvent(GridLogBrowser aLogBrowser)
	{
		super(aLogBrowser);
	}

	/**
	 * Initialize exit event and hasRealChildren flag.
	 */
	private void initChildren()
	{
		itsCallInfo = getLogBrowser().exec(new CallInfoBuilder(getPointer()));		
	}

	public boolean hasRealChildren()
	{
		if (itsCallInfo == null) initChildren();
		return itsCallInfo.hasRealChildren;
	}
	
	public IEventBrowser getChildrenBrowser()
	{
		if (itsFirstChild == null || itsLastChild == null)
		{
			if (itsCallInfo == null) initChildren();
			
			itsFirstChild = itsCallInfo.firstChild != null ?
					getLogBrowser().getEvent(itsCallInfo.firstChild)
					: null;
					
			itsLastChild = itsCallInfo.lastChild != null ? 
					getLogBrowser().getEvent(itsCallInfo.lastChild)
					: null;
		}
		
		GridLogBrowser theLogBrowser = (GridLogBrowser) getLogBrowser();
		
		ICompoundFilter theFilter = theLogBrowser.createIntersectionFilter(
				theLogBrowser.createThreadFilter(getThread()),
				theLogBrowser.createDepthFilter(getDepth()+1));
		
		GridEventBrowser theBrowser = theLogBrowser.createBrowser(theFilter);
		theBrowser.setBounds(itsFirstChild, itsLastChild);
		
		return theBrowser;
	}

	public IBehaviorExitEvent getExitEvent()
	{		
		if (itsExitEvent == null)
		{
			if (itsCallInfo == null) initChildren();

			if (itsCallInfo.exitEvent == null)
			{
				itsExitEvent = null;
			}
			else if (itsCallInfo.lastChild != null 
					&& itsLastChild != null
					&& itsCallInfo.lastChild.equals(itsCallInfo.exitEvent))
			{
				itsExitEvent = (IBehaviorExitEvent) itsLastChild;
			}
			else
			{
				itsExitEvent = (IBehaviorExitEvent) getLogBrowser().getEvent(itsCallInfo.exitEvent);
			}
		}

		return itsExitEvent;
	}
	
	
	private static class CallInfoBuilder 
	implements ITask<ILogBrowser, CallInfo>, Serializable
	{
		private static final long serialVersionUID = -4193913344574735748L;
		
		private ExternalPointer itsEvent;

		public CallInfoBuilder(ExternalPointer aEvent)
		{
			itsEvent = aEvent;
		}

		public CallInfo run(ILogBrowser aLogBrowser)
		{
			ILogEvent theCallEvent = aLogBrowser.getEvent(itsEvent);
			long theTimestamp = theCallEvent.getTimestamp();
			int theDepth = theCallEvent.getDepth();
			IThreadInfo theThread = theCallEvent.getThread();
			
			ILogEvent theFirstChild = null;
			ILogEvent theLastChild = null;
			IBehaviorExitEvent theExitEvent = null;
			boolean theHasRealChildren;
			
			// Find the behavior exit event.
			// First, find next event at the same depth
			IEventFilter theFilter = aLogBrowser.createIntersectionFilter(
					aLogBrowser.createThreadFilter(theThread),
					aLogBrowser.createDepthFilter(theDepth));
			
			IEventBrowser theBrowser = aLogBrowser.createBrowser(theFilter);
			boolean theFound = theBrowser.setPreviousEvent(theCallEvent);
			assert theFound;
			
			if (theBrowser.hasNext())
			{
				// We found the next event at the same depth, so the previous
				// event (at any depth) should be the exit event.
				ILogEvent theNextEvent = theBrowser.next();
				theFilter = aLogBrowser.createThreadFilter(theThread);
				
				theBrowser = aLogBrowser.createBrowser(theFilter);
				theFound = theBrowser.setNextEvent(theNextEvent);
				assert theFound;
				
				ILogEvent theEvent = theBrowser.previous();
				assert theEvent.getDepth() == theDepth+1;
				assert theEvent.getTimestamp() >= theTimestamp;
				assert theEvent.getParentPointer().timestamp == theTimestamp;
				
				theLastChild = theEvent;
				if (theEvent instanceof IBehaviorExitEvent)
				{
					theExitEvent = (IBehaviorExitEvent) theEvent;
				}
			}
			else
			{
				// This event was the last event at this depth, so we must
				// find the last event of the thread.
				theFilter = aLogBrowser.createThreadFilter(theThread);
				
				theBrowser = aLogBrowser.createBrowser(theFilter);
				theBrowser.setPreviousTimestamp(Long.MAX_VALUE);
				ILogEvent theEvent = theBrowser.previous();
				if (theEvent.getDepth() == theDepth+1)
				{
					assert theEvent.getParentPointer().timestamp == theTimestamp;
					theLastChild = theEvent;
					if (theEvent instanceof IBehaviorExitEvent)
					{
						theExitEvent = (IBehaviorExitEvent) theEvent;
					}
				}
			}
			
			// Find out if we have real children
			theFilter = aLogBrowser.createIntersectionFilter(
					aLogBrowser.createThreadFilter(theThread),
					aLogBrowser.createDepthFilter(theDepth+1));
			
			theBrowser = aLogBrowser.createBrowser(theFilter);
			theBrowser.setPreviousEvent(theCallEvent);
			if (theBrowser.hasNext())
			{
				ILogEvent theEvent = theBrowser.next();
				assert theEvent.getParentPointer().timestamp == theTimestamp;
				
				theFirstChild = theEvent;
				theHasRealChildren = ! theFirstChild.equals(theExitEvent);
			}
			else theHasRealChildren = false;
			
			return new CallInfo(
					theFirstChild != null ? theFirstChild.getPointer() : null,
					theLastChild != null ? theLastChild.getPointer() : null,
					theExitEvent != null ? theExitEvent.getPointer() : null,
					theHasRealChildren);
		}
		
	}

	private static class CallInfo implements Serializable
	{
		private static final long serialVersionUID = 642849421884431178L;

		public final ExternalPointer firstChild;
		public final ExternalPointer lastChild;
		public final ExternalPointer exitEvent;
		public final boolean hasRealChildren;

		public CallInfo(
				ExternalPointer aFirstChild, 
				ExternalPointer aLastChild, 
				ExternalPointer aExitEvent, 
				boolean aHasRealChildren)
		{
			firstChild = aFirstChild;
			lastChild = aLastChild;
			exitEvent = aExitEvent;
			hasRealChildren = aHasRealChildren;
		}
	}
}
