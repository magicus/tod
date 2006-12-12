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

import java.util.List;

import tod.core.database.browser.ICFlowBrowser;
import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.IEventFilter;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IThreadInfo;
import zz.utils.tree.AbstractTree;

/**
 * Permits to determine control flow information of a given event:
 * <li>The call stack of the event
 * <li>The sibling events
 * @author gpothier
 */
public class CFlowBrowser extends AbstractTree<ILogEvent, ILogEvent> 
implements ICFlowBrowser
{
	private ILogBrowser itsLog;
	
	private final IThreadInfo itsThread;
	
	private IBehaviorCallEvent itsRoot; 
	
	
	public CFlowBrowser(
			ILogBrowser aLog,
			IThreadInfo aThread, 
			IBehaviorCallEvent aRoot)
	{
		itsLog = aLog;
		itsThread = aThread;
		itsRoot = aRoot;
	}
	
	/**
	 * Creates a cflow browser root at the specified thread's root event.
	 */
	public CFlowBrowser(ILogBrowser aBrowser, IThreadInfo aThread)
	{
		this(aBrowser, aThread, getRootEvent(aBrowser, aThread));
	}
	
	public static IBehaviorCallEvent getRootEvent(ILogBrowser aBrowser, IThreadInfo aThread)
	{
		IEventFilter theFilter = aBrowser.createThreadFilter(aThread);
		IEventBrowser theBrowser = aBrowser.createBrowser(theFilter);
		return (IBehaviorCallEvent) theBrowser.next();
	}
	


	public ILogEvent getChild(ILogEvent aParent, int aIndex)
	{
		IBehaviorCallEvent theEvent = (IBehaviorCallEvent) aParent;
		return theEvent.getChildren().get(aIndex);
	}


	public int getChildCount(ILogEvent aParent)
	{
		if (aParent instanceof IBehaviorCallEvent)
		{
			IBehaviorCallEvent theEvent = (IBehaviorCallEvent) aParent;
			List<ILogEvent> theChildren = theEvent.getChildren();
			return theChildren != null ? theChildren.size() : 0;
		}
		return 0;
	}


	public int getIndexOfChild(ILogEvent aParent, ILogEvent aChild)
	{
		IBehaviorCallEvent theEvent = (IBehaviorCallEvent) aParent;
		return theEvent.getChildren().indexOf(aChild);
	}


	public ILogEvent getParent(ILogEvent aNode)
	{
		return aNode.getParent();
	}


	public ILogEvent getRoot()
	{
		return itsRoot;
	}


	public ILogEvent getValue(ILogEvent aNode)
	{
		return aNode;
	}


	public IThreadInfo getThread()
	{
		return itsThread;
	}
	
	
}
