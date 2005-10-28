/*
 * Created on Nov 8, 2004
 */
package reflex.lib.logging.miner.impl.local.filter;

import reflex.lib.logging.miner.impl.local.LocalCollector;
import tod.core.model.event.IEvent_Behaviour;
import tod.core.model.event.ILogEvent;
import tod.core.model.structure.BehaviorInfo;

/**
 * Behaviour-related filter.
 * @author gpothier
 */
public class BehaviourFilter extends AbstractStatelessFilter
{
	private BehaviorInfo itsBehaviourInfo;
	
	/**
	 * Creates a filter that accepts any behaviou-related event.
	 */
	public BehaviourFilter(LocalCollector aCollector)
	{
		this (aCollector, null);
	}

	/**
	 * Creates a filter that accepts only the events related 
	 * to a particular behaviour (method/constructor).
	 */
	public BehaviourFilter(LocalCollector aCollector, BehaviorInfo aBehaviourInfo)
	{
		super(aCollector);
		itsBehaviourInfo = aBehaviourInfo;
	}
	
	public boolean accept(ILogEvent aEvent)
	{
		if (aEvent instanceof IEvent_Behaviour)
		{
			IEvent_Behaviour theEvent = (IEvent_Behaviour) aEvent;
			return itsBehaviourInfo == null 
				|| theEvent.getBehavior() == itsBehaviourInfo;
		}
		else return false;
	}

}
