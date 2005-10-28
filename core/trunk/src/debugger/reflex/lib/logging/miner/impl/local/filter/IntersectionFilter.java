/*
 * Created on Oct 27, 2004
 */
package reflex.lib.logging.miner.impl.local.filter;

import java.util.List;

import reflex.lib.logging.miner.impl.local.LocalCollector;
import tod.core.model.event.ILogEvent;
import tod.core.model.trace.IEventFilter;

/**
 * A compound filter that corresponds to the logical intersection of
 * all its component filters, ie. accepts an event if all of
 * its components accept it.
 * @author gpothier
 */
public class IntersectionFilter extends CompoundFilter
{

	public IntersectionFilter(LocalCollector aCollector)
	{
		super (aCollector);
	}
	
	public IntersectionFilter(LocalCollector aCollector, List<IEventFilter> aFilters)
	{
		super(aCollector, aFilters);
	}

	public IntersectionFilter(LocalCollector aCollector, IEventFilter... aFilters)
	{
		super(aCollector, aFilters);
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
