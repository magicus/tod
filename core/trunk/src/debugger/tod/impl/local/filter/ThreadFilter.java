/*
 * Created on Oct 27, 2004
 */
package tod.impl.local.filter;

import tod.core.database.event.ILogEvent;
import tod.impl.local.LocalCollector;

/**
 * Accepts only events of a given thread.
 * @author gpothier
 */
public class ThreadFilter extends AbstractStatelessFilter
{
	private int itsThreadId;
	
	public ThreadFilter(LocalCollector aCollector, int aThreadId)
	{
		super (aCollector);
		itsThreadId = aThreadId;
	}
	
	public boolean accept(ILogEvent aEvent)
	{
		return aEvent.getThread().getId() == itsThreadId;
	}
}
