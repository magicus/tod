/*
 * Created on Nov 15, 2004
 */
package reflex.lib.logging.miner.gui.seed;

import reflex.lib.logging.miner.gui.IGUIManager;
import reflex.lib.logging.miner.gui.view.LogView;
import tod.core.model.event.IEvent_Arguments;
import tod.core.model.event.ILogEvent;
import tod.core.model.structure.ThreadInfo;
import tod.core.model.trace.ICFlowBrowser;
import tod.core.model.trace.IEventTrace;
import tod.gui.controlflow.CFlowView;
import zz.utils.properties.IRWProperty;
import zz.utils.properties.SimpleRWProperty;

/**
 * This seed permits to display the cflow view of a particualr thread.
 * @author gpothier
 */
public class CFlowSeed extends Seed
{
	private final ThreadInfo itsThread;
	
	private IRWProperty<ILogEvent> pSelectedEvent = new SimpleRWProperty<ILogEvent>(this);
	private IRWProperty<ILogEvent> pRootEvent = new SimpleRWProperty<ILogEvent>(this);
	
	public CFlowSeed(IGUIManager aGUIManager, IEventTrace aLog, ILogEvent aSelectedEvent)
	{
		this(aGUIManager, aLog, aSelectedEvent.getThread());
		pSelectedEvent().set(aSelectedEvent);
	}

	
	public CFlowSeed(IGUIManager aGUIManager, IEventTrace aLog, ThreadInfo aThread)
	{
		super(aGUIManager, aLog);
		itsThread = aThread;

		ICFlowBrowser theBrowser = getEventTrace().createCFlowBrowser(getThread());
		pRootEvent().set(theBrowser.getRoot());
	}


	protected LogView requestComponent()
	{
		CFlowView theView = new CFlowView(getGUIManager(), getEventTrace(), this);
		theView.init();
		return theView;
	}
	
	public ThreadInfo getThread()
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
	public IRWProperty<ILogEvent> pRootEvent()
	{
		return pRootEvent;
	}
}
