/*
 * Created on Nov 8, 2004
 */
package reflex.lib.logging.miner.impl.common.event;

import tod.core.model.event.IEvent_Behaviour;
import tod.core.model.structure.BehaviorInfo;

/**
 * @author gpothier
 */
public abstract class BehaviorEvent extends Event implements IEvent_Behaviour
{
	private BehaviorInfo itsBehaviourInfo;
	
	public void setBehavior(BehaviorInfo aBehaviourInfo)
	{
		itsBehaviourInfo = aBehaviourInfo;
	}
	
	public BehaviorInfo getBehavior()
	{
		return itsBehaviourInfo;
	}
}
