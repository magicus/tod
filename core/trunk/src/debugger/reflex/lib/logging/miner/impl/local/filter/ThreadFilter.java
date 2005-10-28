/*
 * Created on Oct 27, 2004
 */
package reflex.lib.logging.miner.impl.local.filter;

import reflex.lib.logging.miner.impl.local.LocalCollector;
import tod.core.model.event.ILogEvent;

/**
 * Accepts only events of a given thread.
 * @author gpothier
 */
public class ThreadFilter extends AbstractStatelessFilter
{
	private long itsThreadId;
	
	public ThreadFilter(LocalCollector aCollector, long aThreadId)
	{
		super (aCollector);
		itsThreadId = aThreadId;
	}
	
	public boolean accept(ILogEvent aEvent)
	{
		return aEvent.getThread().getId() == itsThreadId;
	}
}
