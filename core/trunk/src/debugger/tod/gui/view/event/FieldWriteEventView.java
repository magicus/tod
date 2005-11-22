/*
 * Created on Nov 17, 2004
 */
package tod.gui.view.event;

import tod.core.model.event.IFieldWriteEvent;
import tod.core.model.event.ILogEvent;
import tod.core.model.trace.IEventTrace;
import tod.gui.IGUIManager;

/**
 * @author gpothier
 */
public class FieldWriteEventView extends EventView
{
	private IFieldWriteEvent itsEvent;
	
	public FieldWriteEventView(IGUIManager aManager, IEventTrace aLog, IFieldWriteEvent aEvent)
	{
		super(aManager, aLog);
		itsEvent = aEvent;
	}
	
	protected ILogEvent getEvent()
	{
		return itsEvent;
	}
	
	public void init()
	{
		super.init();
		
		Object theValue = itsEvent.getValue();
		
		add (createTitledPanel("Value: ", createInspectorLink(theValue)));
	}


}
