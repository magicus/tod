/*
 * Created on Sep 11, 2006
 */
package tod.impl.local.filter;

import tod.core.database.event.ILogEvent;
import tod.impl.local.LocalCollector;

public class HostFilter extends AbstractStatelessFilter
{
	private int itsHostId;
	
	public HostFilter(LocalCollector aCollector, int aHostId)
	{
		super (aCollector);
		itsHostId = aHostId;
	}
	
	public boolean accept(ILogEvent aEvent)
	{
		return aEvent.getHost().getId() == itsHostId;
	}
}
