/*
 * Created on Nov 15, 2004
 */
package tod.impl.local.filter;

import tod.core.model.browser.IEventBrowser;
import tod.impl.local.EventBrowser;
import tod.impl.local.LocalCollector;

/**
 * A stateless filter is determined only by a condition.
 * @author gpothier
 */
public abstract class AbstractStatelessFilter extends AbstractFilter
{
	public AbstractStatelessFilter(LocalCollector aCollector)
	{
		super(aCollector);
	}

	public IEventBrowser createBrowser ()
	{
		return new EventBrowser (getCollector().getEvents(), this);
	}
}
