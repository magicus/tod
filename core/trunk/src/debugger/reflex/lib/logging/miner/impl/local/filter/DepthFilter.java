/*
 * Created on Oct 27, 2004
 */
package reflex.lib.logging.miner.impl.local.filter;

import reflex.lib.logging.miner.impl.common.event.Event;
import reflex.lib.logging.miner.impl.local.LocalCollector;
import tod.core.model.event.ILogEvent;

/**
 * Accepts only events of a given depth.
 * @author gpothier
 */
public class DepthFilter extends AbstractStatelessFilter
{
	private int itsDepth;
	
	
	public DepthFilter(LocalCollector aCollector, int aDepth)
	{
		super(aCollector);
		itsDepth = aDepth;
	}
	
	public boolean accept(ILogEvent aEvent)
	{
		if (aEvent instanceof Event)
		{
			Event theEvent = (Event) aEvent;
			return theEvent.getDepth() == itsDepth;
		}
		else return false;
	}
}
