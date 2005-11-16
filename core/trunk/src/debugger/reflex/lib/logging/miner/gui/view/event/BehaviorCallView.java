/*
 * Created on Nov 11, 2004
 */
package reflex.lib.logging.miner.gui.view.event;

import reflex.lib.logging.miner.gui.IGUIManager;
import reflex.lib.logging.miner.gui.seed.SeedFactory;
import tod.core.model.event.IBehaviorCallEvent;
import tod.core.model.structure.TypeInfo;
import tod.core.model.trace.IEventTrace;

/**
 * View for a {@link tod.core.model.event.MethodEnter} event.
 * @author gpothier
 */
public class BehaviorCallView extends EventView
{
	private IBehaviorCallEvent itsEvent;
	
	public BehaviorCallView(
			IGUIManager aManager, 
			IEventTrace aLog, 
			IBehaviorCallEvent aEvent)
	{
		super(aManager, aLog);
		itsEvent = aEvent;
	}
	
	protected IBehaviorCallEvent getEvent()
	{
		return itsEvent;
	}
	
	@Override
	public void init()
	{
		super.init();
		
		IBehaviorCallEvent theEvent = getEvent();
		
		// Behaviour
		TypeInfo theTypeInfo = theEvent.getCalledBehavior().getType();
		String theTypeName = theTypeInfo.getName();
		
		add (createTitledLink(
				"Type: ", 
				theTypeName, 
				SeedFactory.getDefaultSeed(getGUIManager(), getEventTrace(), theTypeInfo)));
		
		// Target
		add (createTitledPanel("Target: ", createInspectorLink(theEvent.getTarget())));
	}
}
