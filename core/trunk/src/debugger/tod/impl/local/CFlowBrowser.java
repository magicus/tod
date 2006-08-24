/*
 * Created on Nov 16, 2004
 */
package tod.impl.local;

import java.util.List;

import tod.core.model.browser.ICFlowBrowser;
import tod.core.model.browser.ILogBrowser;
import tod.core.model.event.IBehaviorCallEvent;
import tod.core.model.event.ILogEvent;
import tod.core.model.structure.IThreadInfo;
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
	
	
	public CFlowBrowser(ILogBrowser aLog, IThreadInfo aThread, IBehaviorCallEvent aRoot)
	{
		itsLog = aLog;
		itsThread = aThread;
		itsRoot = aRoot;
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
