/*
 * Created on Nov 15, 2004
 */
package reflex.lib.logging.miner.gui.seed;

import reflex.lib.logging.miner.gui.IGUIManager;
import reflex.lib.logging.miner.gui.view.LogView;
import reflex.lib.logging.miner.gui.view.cflow.CFlowView;
import tod.core.model.event.IEvent_Arguments;
import tod.core.model.event.ILogEvent;
import tod.core.model.structure.ThreadInfo;
import tod.core.model.trace.IEventTrace;
import zz.utils.properties.IRWProperty;
import zz.utils.properties.SimpleRWProperty;

/**
 * This seed permits to display the cflow view of a particualr thread.
 * @author gpothier
 */
public class CFlowSeed extends Seed
{
	private final ThreadInfo itsThread;
	private IRWProperty<ILogEvent> pSelectedEvent = new SimpleRWProperty<ILogEvent>(this)
	{
		@Override
		protected void changed(ILogEvent aOldValue, ILogEvent aNewValue)
		{
			assert aNewValue.getThread() != itsThread;
		}
	};
	
	public CFlowSeed(IGUIManager aGUIManager, IEventTrace aLog, ILogEvent aSelectedEvent)
	{
		super(aGUIManager, aLog);
		itsThread = aSelectedEvent.getThread();
		pSelectedEvent().set(aSelectedEvent);
	}

	
	public CFlowSeed(IGUIManager aGUIManager, IEventTrace aLog, ThreadInfo aThread)
	{
		super(aGUIManager, aLog);
		itsThread = aThread;
	}


	protected LogView requestComponent()
	{
		CFlowView theView = new CFlowView(getGUIManager(), getLog(), this);
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
}
