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
package tod.gui.seed;

import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.IBehaviorExitEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.event.IParentEvent;
import tod.core.database.structure.IThreadInfo;
import tod.gui.IGUIManager;
import tod.gui.controlflow.CFlowView;
import tod.gui.view.LogView;
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
		public void set(ILogEvent aEvent)
		{
			if (aEvent instanceof IBehaviorExitEvent) aEvent = aEvent.getParent();
			itsThread = aEvent.getThread();
			super.set(aEvent);
		}
	};
	private IRWProperty<IParentEvent> pParentEvent = new SimpleRWProperty<IParentEvent>(this);
	private IRWProperty<IParentEvent> pRootEvent = new SimpleRWProperty<IParentEvent>(this);
	
	public CFlowSeed(
			IGUIManager aGUIManager, 
			ILogBrowser aLog, 
			ILogEvent aSelectedEvent)
	{
		this(aGUIManager, aLog, aSelectedEvent.getThread());
		pSelectedEvent().set(aSelectedEvent);
		if (aSelectedEvent != null) pParentEvent().set(aSelectedEvent.getParent());
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
	 * The current parent.
	 */
	public IRWProperty<IParentEvent> pParentEvent()
	{
		return pParentEvent;
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
