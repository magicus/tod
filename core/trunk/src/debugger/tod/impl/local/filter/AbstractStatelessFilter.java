/*
 * Created on Nov 15, 2004
 */
package tod.impl.local.filter;

import tod.core.database.browser.IEventBrowser;
import tod.impl.local.EventBrowser;
import tod.impl.local.LocalBrowser;
import tod.impl.local.LocalBrowser;

/**
 * A stateless filter is determined only by a condition.
 * @author gpothier
 */
public abstract class AbstractStatelessFilter extends AbstractFilter
{
	public AbstractStatelessFilter(LocalBrowser aBrowser)
	{
		super(aBrowser);
	}

	public IEventBrowser createBrowser ()
	{
		return new EventBrowser (getBrowser().getEvents(), this);
	}
}
