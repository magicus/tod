/*
 * Created on Sep 11, 2006
 */
package tod.impl.local.filter;

import tod.core.database.event.ILogEvent;
import tod.impl.local.LocalBrowser;
import tod.impl.local.LocalBrowser;

public class HostFilter extends AbstractStatelessFilter
{
	private int itsHostId;
	
	public HostFilter(LocalBrowser aBrowser, int aHostId)
	{
		super (aBrowser);
		itsHostId = aHostId;
	}
	
	public boolean accept(ILogEvent aEvent)
	{
		return aEvent.getHost().getId() == itsHostId;
	}
}
