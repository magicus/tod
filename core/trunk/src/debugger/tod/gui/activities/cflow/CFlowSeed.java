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
package tod.gui.activities.cflow;

import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.event.IBehaviorExitEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.event.IParentEvent;
import tod.core.database.structure.IThreadInfo;
import tod.core.database.structure.ObjectId;
import tod.gui.activities.ActivityPanel;
import tod.gui.activities.ActivitySeed;
import tod.gui.activities.IEventListSeed;
import tod.gui.formatter.EventFormatter;
import zz.utils.properties.IRWProperty;
import zz.utils.properties.SimpleRWProperty;

/**
 * This seed permits to display the cflow view of a particualr thread.
 * @author gpothier
 */
public class CFlowSeed extends ActivitySeed
implements IEventListSeed
{
	private IThreadInfo itsThread;
	
	private IRWProperty<ILogEvent> pSelectedEvent = new SimpleRWProperty<ILogEvent>(this)
	{
		@Override
		protected void changed(ILogEvent aOldValue, ILogEvent aNewValue)
		{
			if (aNewValue instanceof IBehaviorExitEvent) aNewValue = aNewValue.getParent();
			itsThread = aNewValue.getThread();
		}
	};
	
	private IRWProperty<IParentEvent> pRootEvent = new SimpleRWProperty<IParentEvent>(this);
	private IRWProperty<ILogEvent> pLeafEvent = new SimpleRWProperty<ILogEvent>(this);
	
	private IRWProperty<ObjectId> pInspectedObject = new SimpleRWProperty<ObjectId>(this);
	
	public CFlowSeed(ILogBrowser aLog, ILogEvent aSelectedEvent)
	{
		super(aLog);
		itsThread = aSelectedEvent.getThread();
		pSelectedEvent().set(aSelectedEvent);
		pLeafEvent().set(aSelectedEvent);
	}
	
	public static CFlowSeed forThread(ILogBrowser aLog, IThreadInfo aThread)
	{
		IParentEvent theRoot = aLog.getCFlowRoot(aThread);
		ILogEvent theSelectedEvent = null;
		IEventBrowser theChildrenBrowser = theRoot.getChildrenBrowser();
		if (theChildrenBrowser.hasNext())
		{
			theSelectedEvent = theChildrenBrowser.next();
		}
		
		CFlowSeed theSeed = new CFlowSeed(aLog, theSelectedEvent);
		theSeed.pRootEvent().set(theRoot);
//		theSeed.pParentEvent().set(theRoot);
		return theSeed;
	}

	@Override
	public Class< ? extends ActivityPanel> getComponentClass()
	{
		return CFlowActivityPanel.class;
	}
	
	public IThreadInfo getThread()
	{
		return itsThread;
	}

	/**
	 * The currently selected event in the tree.
	 */
	public IRWProperty<ILogEvent> pSelectedEvent()
	{
		return pSelectedEvent;
	}

	/**
	 * The event at the root of the CFlow tree. Ancestors of the root event
	 * are displayed in the call stack.  
	 */
	public IRWProperty<IParentEvent> pRootEvent()
	{
		return pRootEvent;
	}

	/**
	 * The bottommost event in the control flow (for call stack display).
	 */
	public IRWProperty<ILogEvent> pLeafEvent()
	{
		return pLeafEvent;
	}
	
	/**
	 * The currently inspected object, if any.
	 */
	public IRWProperty<ObjectId> pInspectedObject()
	{
		return pInspectedObject;
	}

	public IRWProperty<ILogEvent> pEvent()
	{
		return pSelectedEvent;
	}

	@Override
	public String getKindDescription()
	{
		return "Control flow view";
	}

	@Override
	public String getShortDescription()
	{
		return EventFormatter.formatEvent(getLogBrowser(), pSelectedEvent.get());
	}
	
	public IEventBrowser getEventBrowser()
	{
		ILogEvent theSelectedEvent = pSelectedEvent().get();
		IParentEvent theParent = theSelectedEvent.getParent();
		return theParent.getChildrenBrowser();
	}
}
