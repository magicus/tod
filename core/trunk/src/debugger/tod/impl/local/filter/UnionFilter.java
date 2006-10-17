/*
 * Created on Oct 27, 2004
 */
package tod.impl.local.filter;

import java.util.List;

import tod.core.database.browser.IEventFilter;
import tod.core.database.event.ILogEvent;
import tod.impl.local.LocalBrowser;
import tod.impl.local.LocalBrowser;

/**
 * A compound filter that corresponds to the logical union of
 * all its component filters, ie. accepts an event if any of
 * its components accepts it.
 * @author gpothier
 */
public class UnionFilter extends CompoundFilter
{

	public UnionFilter(LocalBrowser aBrowser)
	{
		super (aBrowser);
	}
	
	public UnionFilter(LocalBrowser aBrowser, List<IEventFilter> aFilters)
	{
		super(aBrowser, aFilters);
	}

	public UnionFilter(LocalBrowser aBrowser, IEventFilter... aFilters)
	{
		super(aBrowser, aFilters);
	}
	
	public boolean accept(ILogEvent aEvent)
	{
		for (IEventFilter theFilter : getFilters()) 
		{
			if (((AbstractFilter) theFilter).accept(aEvent)) return true;
		}
		
		return false;
	}

}
