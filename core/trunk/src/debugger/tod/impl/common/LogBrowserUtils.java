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
package tod.impl.common;

import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.IEventFilter;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.event.ExternalPointer;
import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.IConstructorChainingEvent;
import tod.core.database.event.IExceptionGeneratedEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.event.IParentEvent;
import tod.core.database.event.IConstructorChainingEvent.CallType;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IThreadInfo;
import tod.impl.common.event.Event;
import tod.impl.local.event.RootEvent;

/**
 * Utility methods for implementing log browsers.
 * @author gpothier
 */
public class LogBrowserUtils 
{
	/**
	 * Retrieves the event corresponding to the given pointer from a log browser.
	 */
	public static ILogEvent getEvent(ILogBrowser aLogBrowser, ExternalPointer aPointer)
	{
		IEventFilter theFilter = aLogBrowser.createThreadFilter(aPointer.thread);
		
		IEventBrowser theBrowser = aLogBrowser.createBrowser(theFilter);
		theBrowser.setNextTimestamp(aPointer.timestamp);
		if (theBrowser.hasNext())
		{
			ILogEvent theEvent = theBrowser.next();
			
			assert theEvent.getThread().equals(aPointer.thread);
			if (theEvent.getTimestamp() == aPointer.timestamp) return theEvent;
		}

		return null;
	}

	/**
	 * Iplementation of {@link ILogBrowser#getCFlowRoot(IThreadInfo)} 
	 */
	public static IParentEvent createCFlowRoot(ILogBrowser aBrowser, IThreadInfo aThread)
	{
		RootEvent theRoot = new RootEvent(aBrowser);
		
		IEventFilter theFilter = aBrowser.createIntersectionFilter(
				aBrowser.createThreadFilter(aThread),
				aBrowser.createDepthFilter(1));
		
		IEventBrowser theBrowser = aBrowser.createBrowser(theFilter);
		
		while (theBrowser.hasNext())
		{
			ILogEvent theEvent = theBrowser.next();
			theRoot.addChild((Event) theEvent);
			
			if (! ((theEvent instanceof IBehaviorCallEvent) 
					|| (theEvent instanceof IExceptionGeneratedEvent)))
			{
				System.err.println("[LogBrowserUtils] Warning: bad event at level 1: "+theEvent);
			}
		}
		return theRoot;
	}
	
	public static CallType isSuperCall(IConstructorChainingEvent aEvent)
	{
		IBehaviorInfo theExecutedBehavior = aEvent.getExecutedBehavior();
		IBehaviorInfo theCallingBehavior = aEvent.getCallingBehavior();
		if (theCallingBehavior == null
				|| theExecutedBehavior == null) 
		{
			return CallType.UNKNOWN;
		}
		else if (theExecutedBehavior.getType().equals(theCallingBehavior.getType()))
		{
			return CallType.THIS;
		}
		else 
		{
			return CallType.SUPER;
		}
	}

}
