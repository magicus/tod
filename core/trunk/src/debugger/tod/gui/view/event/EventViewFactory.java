/*
 * Created on Nov 13, 2004
 */
package tod.gui.view.event;

import tod.core.database.browser.ILogBrowser;
import tod.core.database.event.IArrayWriteEvent;
import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.IFieldWriteEvent;
import tod.core.database.event.IInstantiationEvent;
import tod.core.database.event.ILogEvent;
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
			ILogBrowser aLog,
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
		else if (aEvent instanceof IArrayWriteEvent)
		{
			IArrayWriteEvent theEvent = (IArrayWriteEvent) aEvent;
			theView = new ArrayWriteEventView(aGUIManager, aLog, theEvent);
		}
		
		if (theView != null) theView.init();
		return theView;
	}
}
