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
 * A compound filter that corresponds to the logical intersection of
 * all its component filters, ie. accepts an event if all of
 * its components accept it.
 * @author gpothier
 */
public class IntersectionFilter extends CompoundFilter
{

	public IntersectionFilter(LocalBrowser aBrowser)
	{
		super (aBrowser);
	}
	
	public IntersectionFilter(LocalBrowser aBrowser, List<IEventFilter> aFilters)
	{
		super(aBrowser, aFilters);
	}

	public IntersectionFilter(LocalBrowser aBrowser, IEventFilter... aFilters)
	{
		super(aBrowser, aFilters);
	}
	
	public boolean accept(ILogEvent aEvent)
	{
		for (IEventFilter theFilter : getFilters()) 
		{
			if (! ((AbstractFilter) theFilter).accept(aEvent)) return false;
		}
		
		return true;
	}

}
