/*
 * Created on Nov 10, 2004
 */
package reflex.lib.logging.miner.gui.seed;

import reflex.lib.logging.miner.gui.IGUIManager;
import reflex.lib.logging.miner.gui.view.FilterView;
import reflex.lib.logging.miner.gui.view.LogView;
import tod.core.model.trace.IEventTrace;
import tod.core.model.trace.IEventBrowser;
import tod.core.model.trace.IEventFilter;

/**
 * A seed that is based on a {@link tod.core.model.trace.IEventBrowser}.
 * Its view is simply a sequential view of filtered events.
 * @author gpothier
 */
public class FilterSeed extends Seed/*<FilterView>*/
{
	private IEventFilter itsFilter;
	
	/**
	 * Timestamp of the first event displayed by this view.
	 */
	private long itsTimestamp;
	
	
	public FilterSeed(IGUIManager aGUIManager, IEventTrace aLog, IEventFilter aFilter)
	{
		super(aGUIManager, aLog);
		itsFilter = aFilter;
	}
	
	protected LogView requestComponent()
	{
		FilterView theView = new FilterView (getGUIManager(), getLog(), this);
		theView.init();
		return theView;
	}
	
	

	public long getTimestamp()
	{
		return itsTimestamp;
	}
	
	public void setTimestamp(long aTimestamp)
	{
		itsTimestamp = aTimestamp;
	}
	
	public IEventFilter getFilter()
	{
		return itsFilter;
	}
}
