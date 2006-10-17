/*
 * Created on Oct 27, 2004
 */
package tod.impl.local.filter;

import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.IEventFilter;
import tod.core.database.event.ILogEvent;
import tod.impl.local.LocalBrowser;
import tod.impl.local.LocalBrowser;

/**
 * Abstract base class for stateless filters. They only implement 
 * the reset method, which does nothing.
 * @author gpothier
 */
public abstract class AbstractFilter implements IEventFilter
{
	private LocalBrowser itsBrowser;
	
	public AbstractFilter(LocalBrowser aBrowser)
	{
		itsBrowser = aBrowser;
	}
	
	
	protected LocalBrowser getBrowser()
	{
		return itsBrowser;
	}

	/**
	 * Whether the specified event is accepted by this filter.
	 */
	public abstract boolean accept (ILogEvent aEvent);
	
	public abstract IEventBrowser createBrowser ();

}
