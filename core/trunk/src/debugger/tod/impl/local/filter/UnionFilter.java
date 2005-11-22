/*
 * Created on Oct 27, 2004
 */
package tod.impl.local.filter;

import java.util.List;

import tod.core.model.event.ILogEvent;
import tod.core.model.trace.IEventFilter;
import tod.impl.local.LocalCollector;

/**
 * A compound filter that corresponds to the logical union of
 * all its component filters, ie. accepts an event if any of
 * its components accepts it.
 * @author gpothier
 */
public class UnionFilter extends CompoundFilter
{

	public UnionFilter(LocalCollector aCollector)
	{
		super (aCollector);
	}
	
	public UnionFilter(LocalCollector aCollector, List<IEventFilter> aFilters)
	{
		super(aCollector, aFilters);
	}

	public UnionFilter(LocalCollector aCollector, IEventFilter... aFilters)
	{
		super(aCollector, aFilters);
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
