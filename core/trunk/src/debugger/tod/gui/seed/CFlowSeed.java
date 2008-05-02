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
package tod.gui.seed;

import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.event.IBehaviorExitEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.event.IParentEvent;
import tod.core.database.structure.IThreadInfo;
import tod.gui.IGUIManager;
import tod.gui.view.LogView;
import tod.gui.view.controlflow.CFlowView;
import zz.utils.properties.IRWProperty;
import zz.utils.properties.SimpleRWProperty;

/**
 * This seed permits to display the cflow view of a particualr thread.
 * @author gpothier
 */
public class CFlowSeed extends LogViewSeed
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
	
	public CFlowSeed(
			IGUIManager aGUIManager, 
			ILogBrowser aLog, 
			ILogEvent aSelectedEvent)
	{
		this(aGUIManager, aLog, aSelectedEvent.getThread());
		pSelectedEvent().set(aSelectedEvent);
	}

	
	public CFlowSeed(IGUIManager aGUIManager, ILogBrowser aLog, IThreadInfo aThread)
	{
		super(aGUIManager, aLog);
		itsThread = aThread;

		IParentEvent theRoot = aLog.getCFlowRoot(aThread);
		pRootEvent().set(theRoot);
//		pParentEvent().set(theRoot);
		IEventBrowser theChildrenBrowser = theRoot.getChildrenBrowser();
		if (theChildrenBrowser.hasNext())
		{
			pSelectedEvent().set(theChildrenBrowser.next());
		}
	}


	protected LogView requestComponent()
	{
		CFlowView theView = new CFlowView(getGUIManager(), getLogBrowser(), this);
		theView.init();
		return theView;
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
}
