/*
 * Created on Aug 30, 2005
 */
package reflex.lib.logging.miner.impl.common.event;

import tod.core.model.event.IAfterMethodCallEvent;
import tod.core.model.event.IEvent_ReturnValue;

public class AfterMethodCall extends BehaviorEvent implements IAfterMethodCallEvent
{
	private Object itsTarget;
	private Object itsReturnValue;
	
	public Object getTarget()
	{
		return itsTarget;
	}

	public void setTarget(Object aTarget)
	{
		itsTarget = aTarget;
	}

	public Object getReturnValue()
	{
		return itsReturnValue;
	}
	
	public void setReturnValue(Object aReturnValue)
	{
		itsReturnValue = aReturnValue;
	}

}
