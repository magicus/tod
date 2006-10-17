/*
 * Created on Oct 27, 2004
 */
package tod.impl.local.filter;

import tod.core.database.event.ILogEvent;
import tod.impl.local.LocalBrowser;
import tod.impl.local.LocalBrowser;

/**
 * Accepts only events of a given thread.
 * @author gpothier
 */
public class ThreadFilter extends AbstractStatelessFilter
{
	private int itsThreadId;
	
	public ThreadFilter(LocalBrowser aBrowser, int aThreadId)
	{
		super (aBrowser);
		itsThreadId = aThreadId;
	}
	
	public boolean accept(ILogEvent aEvent)
	{
		return aEvent.getThread().getId() == itsThreadId;
	}
}
