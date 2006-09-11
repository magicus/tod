/*
 * Created on Nov 10, 2004
 */
package tod.gui;

import javax.swing.AbstractListModel;

import tod.core.database.browser.IEventBrowser;

/**
 * A swing list model of an {@link tod.core.database.browser.IEventBrowser}
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
		throw new UnsupportedOperationException();
//		return itsBrowser.getEvent(aIndex);
	}
}
