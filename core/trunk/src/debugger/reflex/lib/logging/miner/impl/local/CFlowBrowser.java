/*
 * Created on Nov 16, 2004
 */
package reflex.lib.logging.miner.impl.local;

import java.util.List;

import reflex.lib.logging.miner.impl.common.event.Event;
import tod.core.model.event.IBehaviorEnterEvent;
import tod.core.model.event.ILogEvent;
import tod.core.model.structure.ThreadInfo;
import tod.core.model.trace.IEventTrace;
import tod.core.model.trace.ICFlowBrowser;
import tod.core.model.trace.ICompoundFilter;
import tod.core.model.trace.IEventBrowser;
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
	private IEventTrace itsLog;
	
	private final ThreadInfo itsThread;
	
	private IBehaviorEnterEvent itsRoot; 
	
	
	public CFlowBrowser(IEventTrace aLog, ThreadInfo aThread)
	{
		itsLog = aLog;
		itsThread = aThread;
		
		ICompoundFilter theFilter = itsLog.createIntersectionFilter(
				itsLog.createBehaviorFilter(),
				itsLog.createThreadFilter(itsThread));
		
		IEventBrowser theBrowser = itsLog.createBrowser(theFilter);
		
		while (theBrowser.hasNext())
		{
			ILogEvent theEvent = theBrowser.getNext();
			if (theEvent instanceof IBehaviorEnterEvent) 
			{
				itsRoot = (IBehaviorEnterEvent) theEvent;
				break;
			}
		}
	}


	public ILogEvent getChild(ILogEvent aParent, int aIndex)
	{
		IBehaviorEnterEvent theEvent = (IBehaviorEnterEvent) aParent;
		return theEvent.getChildren().get(aIndex);
	}


	public int getChildCount(ILogEvent aParent)
	{
		if (aParent instanceof IBehaviorEnterEvent)
		{
			IBehaviorEnterEvent theEvent = (IBehaviorEnterEvent) aParent;
			List<Event> theChildren = theEvent.getChildren();
			return theChildren != null ? theChildren.size() : 0;
		}
		return 0;
	}


	public int getIndexOfChild(ILogEvent aParent, ILogEvent aChild)
	{
		IBehaviorEnterEvent theEvent = (IBehaviorEnterEvent) aParent;
		return theEvent.getChildren().indexOf(aChild);
	}


	public ILogEvent getParent(ILogEvent aNode)
	{
		return aNode.getFather();
	}


	public ILogEvent getRoot()
	{
		return itsRoot;
	}


	public ILogEvent getValue(ILogEvent aNode)
	{
		return aNode;
	}


	public ThreadInfo getThread()
	{
		return itsThread;
	}
	
	
}
