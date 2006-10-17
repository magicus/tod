/*
 * Created on Oct 25, 2004
 */
package tod.impl.common.event;

import tod.core.database.event.IInstantiationEvent;
import tod.core.database.structure.ITypeInfo;

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
		return getExecutedBehavior() != null ? 
				getExecutedBehavior().getType()
				: getCalledBehavior().getType();
	}
}
