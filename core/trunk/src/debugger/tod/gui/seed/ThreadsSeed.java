/*
 * Created on Sep 22, 2005
 */
package tod.gui.seed;

import tod.core.database.browser.ILogBrowser;
import tod.gui.IGUIManager;
import tod.gui.view.LogView;
import tod.gui.view.ThreadsView;

/**
 * This seed provides a view that lets the user browse all events that occured in
 * each thread.
 * @author gpothier
 */
public class ThreadsSeed extends Seed
{

	public ThreadsSeed(IGUIManager aGUIManager, ILogBrowser aLog)
	{
		super(aGUIManager, aLog);
	}

	@Override
	protected LogView requestComponent()
	{
		ThreadsView theView = new ThreadsView(getGUIManager(), getEventTrace(), this);
		theView.init();
		return theView;
	}

}
