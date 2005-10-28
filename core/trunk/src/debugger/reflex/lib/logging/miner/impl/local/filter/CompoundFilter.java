/*
 * Created on Oct 27, 2004
 */
package reflex.lib.logging.miner.impl.local.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import reflex.lib.logging.miner.impl.local.LocalCollector;
import tod.core.model.trace.ICompoundFilter;
import tod.core.model.trace.IEventFilter;

/**
 * Base class for filters that are compound of other filters.
 * @author gpothier
 */
public abstract class CompoundFilter extends AbstractStatelessFilter implements ICompoundFilter
{
	private List<IEventFilter> itsFilters;
	
	public CompoundFilter(LocalCollector aCollector)
	{
		this (aCollector, new ArrayList<IEventFilter>());
	}
	
	public CompoundFilter(LocalCollector aCollector, List<IEventFilter> aFilters)
	{
		super (aCollector);
		itsFilters = aFilters;
	}
	
	public CompoundFilter(LocalCollector aCollector, IEventFilter... aFilters)
	{
		super (aCollector);
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
