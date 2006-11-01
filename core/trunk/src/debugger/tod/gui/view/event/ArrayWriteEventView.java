/*
 * Created on Nov 17, 2004
 */
package tod.gui.view.event;

import tod.core.database.browser.ILogBrowser;
import tod.core.database.event.IArrayWriteEvent;
import tod.core.database.event.IFieldWriteEvent;
import tod.core.database.event.ILogEvent;
import tod.gui.IGUIManager;

/**
 * @author gpothier
 */
public class ArrayWriteEventView extends EventView
{
	private IArrayWriteEvent itsEvent;
	
	public ArrayWriteEventView(IGUIManager aManager, ILogBrowser aLog, IArrayWriteEvent aEvent)
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
