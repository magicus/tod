/*
 * Created on Oct 15, 2006
 */
package tod.impl.local.filter;

import tod.core.database.event.ILogEvent;
import tod.impl.local.LocalBrowser;

public class DepthFilter extends AbstractStatelessFilter
{
	private int itsDepth;
	
	public DepthFilter(LocalBrowser aBrowser, int aDepth)
	{
		super (aBrowser);
		itsDepth = aDepth;
	}
	
	public boolean accept(ILogEvent aEvent)
	{
		return aEvent.getDepth() == itsDepth;
	}
}
