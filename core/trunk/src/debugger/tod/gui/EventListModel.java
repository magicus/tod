/*
 * Created on Nov 10, 2004
 */
package tod.gui;

import javax.swing.AbstractListModel;

import tod.core.model.trace.IEventBrowser;

/**
 * A swing list model of an {@link tod.core.model.trace.IEventBrowser}
 * @author gpothier
 */
public class EventListModel extends AbstractListModel
{
	private IEventBrowser itsBrowser;
	
	public EventListModel (IEventBrowser aBrowser)
	{
		itsBrowser = aBrowser;
	}

	public int getSize()
	{
		return itsBrowser.getEventCount();
	}

	public Object getElementAt(int aIndex)
	{
		return itsBrowser.getEvent(aIndex);
	}
}