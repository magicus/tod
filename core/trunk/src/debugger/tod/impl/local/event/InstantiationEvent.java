/*
 * Created on Oct 25, 2004
 */
package tod.impl.local.event;

import tod.core.model.event.IInstantiationEvent;
import tod.core.model.structure.ITypeInfo;

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
	
	public ITypeInfo getType()
	{
		return getExecutedBehavior().getType();
	}
}
