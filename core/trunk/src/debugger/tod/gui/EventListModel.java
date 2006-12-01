/*
 * Created on Nov 10, 2004
 */
package tod.gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;

import tod.core.database.browser.IEventBrowser;

/**
 * A swing list model of an {@link tod.core.database.browser.IEventBrowser}
 * @author gpothier
 */
public class EventListModel extends AbstractListModel
{
	private IEventBrowser itsBrowser;
	
	// TODO: this is a hack, UI should be changed so that we don't use lists
	private List itsList = new ArrayList();
	
	public EventListModel (IEventBrowser aBrowser)
	{
		itsBrowser = aBrowser;
		while (aBrowser.hasNext()) itsList.add(aBrowser.next());
	}

	public int getSize()
	{
		return itsList.size();
	}

	public Object getElementAt(int aIndex)
	{
		return itsList.get(aIndex);
	}
}
