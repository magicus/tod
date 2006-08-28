/*
 * Created on Nov 11, 2004
 */
package tod.gui.view.event;

import tod.core.database.browser.ILogBrowser;
import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.ITypeInfo;
import tod.gui.IGUIManager;
import tod.gui.seed.SeedFactory;

/**
 * View for a {@link tod.core.database.event.MethodEnter} event.
 * @author gpothier
 */
public class BehaviorCallView extends EventView
{
	private IBehaviorCallEvent itsEvent;
	
	public BehaviorCallView(
			IGUIManager aManager, 
			ILogBrowser aLog, 
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
		ITypeInfo theTypeInfo = null;
		IBehaviorInfo theExecutedBehavior = theEvent.getExecutedBehavior();
		if (theExecutedBehavior != null) theTypeInfo = theExecutedBehavior.getType();
		else
		{
			IBehaviorInfo theCalledBehavior = theEvent.getCalledBehavior();
			if (theCalledBehavior != null) theTypeInfo = theCalledBehavior.getType();
		}
		
		String theTypeName = theTypeInfo.getName();
		
		add (createTitledLink(
				"Type: ", 
				theTypeName, 
				SeedFactory.getDefaultSeed(getGUIManager(), getLogBrowser(), theTypeInfo)));
		
		// Target
		add (createTitledPanel("Target: ", createInspectorLink(theEvent.getTarget())));
	}
}
