/*
 * Created on Nov 15, 2004
 */
package reflex.lib.logging.miner.impl.local.filter;

import reflex.lib.logging.miner.impl.local.EventBrowser;
import reflex.lib.logging.miner.impl.local.LocalCollector;
import tod.core.model.trace.IEventTrace;
import tod.core.model.trace.IEventBrowser;

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
