/*
 * Created on Nov 15, 2004
 */
package tod.impl.local.filter;

import java.util.ArrayList;
import java.util.List;

import tod.core.model.event.ILogEvent;
import tod.core.model.trace.IEventBrowser;
import tod.impl.local.EventBrowser;
import tod.impl.local.LocalCollector;
import tod.impl.local.event.Event;

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