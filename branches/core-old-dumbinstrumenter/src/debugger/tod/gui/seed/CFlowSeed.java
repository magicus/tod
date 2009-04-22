/*
 * Created on Nov 15, 2004
 */
package tod.gui.seed;

import tod.core.database.browser.ICFlowBrowser;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.event.ILogEvent;
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
public class CFlowSeed extends Seed
{
	private final IThreadInfo itsThread;
	
	private IRWProperty<ILogEvent> pSelectedEvent = new SimpleRWProperty<ILogEvent>(this);
	private IRWProperty<ILogEvent> pRootEvent = new SimpleRWProperty<ILogEvent>(this);
	
	public CFlowSeed(IGUIManager aGUIManager, ILogBrowser aLog, ILogEvent aSelectedEvent)
	{
		this(aGUIManager, aLog, aSelectedEvent.getThread());
		pSelectedEvent().set(aSelectedEvent);
	}

	
	public CFlowSeed(IGUIManager aGUIManager, ILogBrowser aLog, IThreadInfo aThread)
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
	public IRWProperty<ILogEvent> pRootEvent()
	{
		return pRootEvent;
	}
}
