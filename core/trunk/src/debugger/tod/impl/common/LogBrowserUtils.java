/*
TOD - Trace Oriented Debugger.
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
	 * Indicates if the specified event is a member of the specified browser's 
	 * result set.
	 * This method might move the browser's event pointer.
	 */
	public static boolean hasEvent(IEventBrowser aBrowser, ILogEvent aEvent)
	{
		aBrowser.setNextEvent(aEvent);
		if (aBrowser.hasNext())
		{
			ILogEvent theNext = aBrowser.next();
			if (aEvent.equals(theNext)) return true;
		}
		return false;
	}

	/**
	 * Implementation of {@link ILogBrowser#getCFlowRoot(IThreadInfo)} 
	 */
	public static IParentEvent createCFlowRoot(ILogBrowser aBrowser, IThreadInfo aThread)
	{
		RootEvent theRoot = new RootEvent(aBrowser);
		theRoot.setTimestamp(aBrowser.getFirstTimestamp());
		
		IEventFilter theFilter = aBrowser.createIntersectionFilter(
				aBrowser.createThreadFilter(aThread),
				aBrowser.createDepthFilter(1));
		
		IEventBrowser theBrowser = aBrowser.createBrowser(theFilter);
		
		while (theBrowser.hasNext())
		{
			ILogEvent theEvent = theBrowser.next();
			theRoot.addChild((Event) theEvent);
			
//			if (! ((theEvent instanceof IBehaviorCallEvent) 
//					|| (theEvent instanceof IExceptionGeneratedEvent)))
//			{
//				System.err.println("[LogBrowserUtils] Warning: bad event at level 1: "+theEvent);
//			}
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
