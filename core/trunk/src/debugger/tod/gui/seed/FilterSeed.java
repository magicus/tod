/*
 * Created on Nov 10, 2004
 */
package tod.gui.seed;

import tod.core.model.trace.IEventFilter;
import tod.core.model.trace.IEventTrace;
import tod.gui.IGUIManager;
import tod.gui.view.FilterView;
import tod.gui.view.LogView;

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
		FilterView theView = new FilterView (getGUIManager(), getEventTrace(), this);
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
