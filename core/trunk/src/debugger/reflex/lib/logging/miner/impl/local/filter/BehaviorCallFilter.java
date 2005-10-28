/*
 * Created on Nov 8, 2004
 */
package reflex.lib.logging.miner.impl.local.filter;

import reflex.lib.logging.miner.impl.local.LocalCollector;
import tod.core.model.event.IAfterMethodCallEvent;
import tod.core.model.event.IBeforeMethodCallEvent;
import tod.core.model.event.IEvent_Behaviour;
import tod.core.model.event.ILogEvent;
import tod.core.model.structure.BehaviorInfo;

/**
 * A filter that accepts only behavior calls (before and after).
 * @author gpothier
 */
public class BehaviorCallFilter extends AbstractStatelessFilter
{
	private BehaviorInfo itsBehaviourInfo;
	
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
	public BehaviorCallFilter(LocalCollector aCollector, BehaviorInfo aBehaviourInfo)
	{
		super(aCollector);
		itsBehaviourInfo = aBehaviourInfo;
	}
	
	public boolean accept(ILogEvent aEvent)
	{
		if (aEvent instanceof IBeforeMethodCallEvent)
		{
			IBeforeMethodCallEvent theEvent = (IBeforeMethodCallEvent) aEvent;
			return itsBehaviourInfo == null 
				|| theEvent.getBehavior() == itsBehaviourInfo;
		}
		else if (aEvent instanceof IAfterMethodCallEvent)
		{
			IAfterMethodCallEvent theEvent = (IAfterMethodCallEvent) aEvent;
			return itsBehaviourInfo == null 
			|| theEvent.getBehavior() == itsBehaviourInfo;
		}
		else return false;
	}

}
