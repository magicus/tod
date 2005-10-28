/*
 * Created on Nov 15, 2004
 */
package reflex.lib.logging.miner.impl.local.filter;

import java.util.ArrayList;
import java.util.List;

import reflex.lib.logging.miner.impl.common.event.Event;
import reflex.lib.logging.miner.impl.local.EventBrowser;
import reflex.lib.logging.miner.impl.local.LocalCollector;
import tod.core.model.event.ILogEvent;
import tod.core.model.trace.IEventBrowser;

/**
 * Base class for filters that are precomputed.
 * @author gpothier
 */
public abstract class AbstractPrecomputedFilter extends AbstractFilter
{
	private List<ILogEvent> itsEvents = new ArrayList<ILogEvent>();

	public AbstractPrecomputedFilter(LocalCollector aCollector)
	{
		super(aCollector);
		
	}
	
	protected void addEvent (Event aEvent)
	{
		itsEvents.add (aEvent);
	}
	
	public IEventBrowser createBrowser()
	{
		return new EventBrowser(itsEvents);
	}
	
	public boolean accept(ILogEvent aEvent)
	{
		return itsEvents.contains(aEvent);
	}
}
