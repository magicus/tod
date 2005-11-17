/*
 * Created on Oct 25, 2004
 */
package reflex.lib.logging.miner.impl.common.event;

import tod.core.model.event.IInstantiationEvent;
import tod.core.model.structure.TypeInfo;

/**
 * @author gpothier
 */
public class InstantiationEvent extends BehaviorCallEvent 
implements IInstantiationEvent
{
	public Object getInstance()
	{
		return getTarget();
	}
	
	public void setInstance(Object aInstance)
	{
		setTarget(aInstance);
	}
	
	public TypeInfo getType()
	{
		return getExecutedBehavior().getType();
	}
}
