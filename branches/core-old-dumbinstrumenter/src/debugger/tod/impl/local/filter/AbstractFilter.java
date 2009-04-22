/*
 * Created on Oct 27, 2004
 */
package tod.impl.local.filter;

import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.IEventFilter;
import tod.core.database.event.ILogEvent;
import tod.impl.local.LocalCollector;

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
