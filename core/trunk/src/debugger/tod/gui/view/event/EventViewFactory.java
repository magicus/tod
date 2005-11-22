/*
 * Created on Nov 13, 2004
 */
package tod.gui.view.event;

import tod.core.model.event.IBehaviorCallEvent;
import tod.core.model.event.IFieldWriteEvent;
import tod.core.model.event.IInstantiationEvent;
import tod.core.model.event.ILogEvent;
import tod.core.model.trace.IEventTrace;
import tod.gui.IGUIManager;

/**
 * This factory permits to obtain an event view given a
 * log event.
 * @author gpothier
 */
public class EventViewFactory
{
	/**
	 * Creates a viewer for the given event.
	 */
	public static EventView createView (
			IGUIManager aGUIManager, 
			IEventTrace aLog,
			ILogEvent aEvent)
	{
		EventView theView = null;
		
		if (aEvent instanceof IBehaviorCallEvent)
		{
			IBehaviorCallEvent theEvent = (IBehaviorCallEvent) aEvent;
			theView = new BehaviorCallView (aGUIManager, aLog, theEvent);
		}
		else if (aEvent instanceof IInstantiationEvent)
		{
			IInstantiationEvent theEvent = (IInstantiationEvent) aEvent;
			theView = new InstantiationView (aGUIManager, aLog, theEvent);
		}
		else if (aEvent instanceof IFieldWriteEvent)
		{
			IFieldWriteEvent theEvent = (IFieldWriteEvent) aEvent;
			theView = new FieldWriteEventView (aGUIManager, aLog, theEvent);
		}
		
		if (theView != null) theView.init();
		return theView;
	}
}
