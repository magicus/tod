/*
 * Created on Oct 27, 2004
 */
package tod.impl.local.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import tod.core.database.browser.ICompoundFilter;
import tod.core.database.browser.IEventFilter;
import tod.impl.local.LocalBrowser;
import tod.impl.local.LocalBrowser;

/**
 * Base class for filters that are compound of other filters.
 * @author gpothier
 */
public abstract class CompoundFilter extends AbstractStatelessFilter implements ICompoundFilter
{
	private List<IEventFilter> itsFilters;
	
	public CompoundFilter(LocalBrowser aBrowser)
	{
		this (aBrowser, new ArrayList<IEventFilter>());
	}
	
	public CompoundFilter(LocalBrowser aBrowser, List<IEventFilter> aFilters)
	{
		super (aBrowser);
		itsFilters = aFilters;
	}
	
	public CompoundFilter(LocalBrowser aBrowser, IEventFilter... aFilters)
	{
		super (aBrowser);
		itsFilters = new ArrayList<IEventFilter>(Arrays.asList(aFilters));
	}
	
	public List<IEventFilter> getFilters()
	{
		return itsFilters;
	}
	
	public void add (IEventFilter aFilter)
	{
		itsFilters.add(aFilter);
	}
	
	public void remove (IEventFilter aFilter)
	{
		itsFilters.remove(aFilter);
	}
	
}
