/*
 * Created on Sep 22, 2005
 */
package reflex.lib.logging.miner.gui.seed;

import reflex.lib.logging.miner.gui.IGUIManager;
import reflex.lib.logging.miner.gui.view.LogView;
import reflex.lib.logging.miner.gui.view.ThreadsView;
import tod.core.model.trace.IEventTrace;

/**
 * This seed provides a view that lets the user browse all events that occured in
 * each thread.
 * @author gpothier
 */
public class ThreadsSeed extends Seed
{

	public ThreadsSeed(IGUIManager aGUIManager, IEventTrace aLog)
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
