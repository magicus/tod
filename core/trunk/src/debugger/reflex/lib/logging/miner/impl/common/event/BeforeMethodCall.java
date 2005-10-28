/*
 * Created on Aug 30, 2005
 */
package reflex.lib.logging.miner.impl.common.event;

import tod.core.model.event.IBeforeMethodCallEvent;
import tod.core.model.event.IEvent_Arguments;

public class BeforeMethodCall extends BehaviorEvent implements IBeforeMethodCallEvent
{
	private Object itsTarget;
	private Object[] itsArguments;
	
	public Object getTarget()
	{
		return itsTarget;
	}

	public void setTarget(Object aTarget)
	{
		itsTarget = aTarget;
	}

	public Object[] getArguments()
	{
		return itsArguments;
	}
	
	public void setArguments(Object[] aArguments)
	{
		itsArguments = aArguments;
	}

}
