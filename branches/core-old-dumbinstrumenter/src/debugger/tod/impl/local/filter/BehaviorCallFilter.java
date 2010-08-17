/*
 * Created on Nov 8, 2004
 */
package tod.impl.local.filter;

import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.impl.local.LocalCollector;

/**
 * A filter that accepts only behavior calls (before and after).
 * @author gpothier
 */
public class BehaviorCallFilter extends AbstractStatelessFilter
{
	private IBehaviorInfo itsBehaviour;
	
	/**
	 * Creates a filter that accepts any behaviou-related event.
	 */
	public BehaviorCallFilter(LocalCollector aCollector)
	{
		this (aCollector, null);
	}

	/**
	 * Creates a filter that accepts only the events related 
	 * to a particular behaviour (method/constructor).
	 */
	public BehaviorCallFilter(LocalCollector aCollector, IBehaviorInfo aBehavior)
	{
		super(aCollector);
		itsBehaviour = aBehavior;
	}
	
	public boolean accept(ILogEvent aEvent)
	{
		if (aEvent instanceof IBehaviorCallEvent)
		{
			IBehaviorCallEvent theEvent = (IBehaviorCallEvent) aEvent;
			IBehaviorInfo theExecutedBehavior = theEvent.getExecutedBehavior();
			IBehaviorInfo theCalledBehavior = theEvent.getCalledBehavior();
			
			return (theExecutedBehavior != null 
					&& theExecutedBehavior.equals(itsBehaviour))
				|| (theCalledBehavior != null
					&& theCalledBehavior.equals(itsBehaviour));
		}
		else return false;
	}

}