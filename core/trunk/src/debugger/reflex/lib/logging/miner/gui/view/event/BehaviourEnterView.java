/*
 * Created on Nov 11, 2004
 */
package reflex.lib.logging.miner.gui.view.event;

import reflex.lib.logging.miner.gui.IGUIManager;
import tod.core.model.event.IBehaviorEnterEvent;
import tod.core.model.event.IEvent_Arguments;
import tod.core.model.event.IEvent_Behaviour;
import tod.core.model.trace.IEventTrace;

/**
 * View for a {@link tod.core.model.event.MethodEnter} event.
 * @author gpothier
 */
public class BehaviourEnterView extends BehaviourEventView
{
	private IBehaviorEnterEvent itsEvent;
	
	public BehaviourEnterView(
			IGUIManager aManager, 
			IEventTrace aLog, 
			IBehaviorEnterEvent aEvent)
	{
		super(aManager, aLog);
		itsEvent = aEvent;
	}
	
	protected IBehaviorEnterEvent getBehaviourEvent()
	{
		return itsEvent;
	}
}
