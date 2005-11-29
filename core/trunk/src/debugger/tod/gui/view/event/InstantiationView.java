/*
 * Created on Nov 23, 2004
 */
package tod.gui.view.event;

import tod.core.model.event.IInstantiationEvent;
import tod.core.model.trace.IEventTrace;
import tod.gui.IGUIManager;

/**
 * View for {@link tod.core.model.event.IInstantiationEvent}
 * @author gpothier
 */
public class InstantiationView extends BehaviorCallView
{
	public InstantiationView(IGUIManager aManager, IEventTrace aLog, IInstantiationEvent aEvent)
	{
		super(aManager, aLog, aEvent);
	}
	
	
	protected IInstantiationEvent getEvent()
	{
		return (IInstantiationEvent) super.getEvent();
	}
	
	public void init()
	{
		super.init();
		add (createTitledPanel("Created instance: ", createInspectorLink(getEvent().getInstance())));
	}

}