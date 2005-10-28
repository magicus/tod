/*
 * Created on Oct 27, 2004
 */
package reflex.lib.logging.miner.impl.local.filter;

import reflex.lib.logging.miner.impl.local.LocalCollector;
import tod.core.model.event.ILogEvent;
import tod.core.model.trace.IEventTrace;
import tod.core.model.trace.IEventBrowser;
import tod.core.model.trace.IEventFilter;

/**
 * Abstract base class for stateless filters. They only implement 
 * the reset method, which does nothing.
 * @author gpothier
 */
public abstract class AbstractFilter implements IEventFilter
{
	private LocalCollector itsCollector;
	
	public AbstractFilter(LocalCollector aCollector)
	{
		itsCollector = aCollector;
	}
	
	
	protected LocalCollector getCollector()
	{
		return itsCollector;
	}

	/**
	 * Whether the specified event is accepted by this filter.
	 */
	public abstract boolean accept (ILogEvent aEvent);
	
	public abstract IEventBrowser createBrowser ();

}
