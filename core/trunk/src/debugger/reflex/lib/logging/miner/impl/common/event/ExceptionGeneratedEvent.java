/*
 * Created on Oct 25, 2004
 */
package reflex.lib.logging.miner.impl.common.event;

import tod.core.model.event.IExceptionGeneratedEvent;
import tod.core.model.structure.BehaviorInfo;

/**
 * @author gpothier
 */
public class ExceptionGeneratedEvent extends Event implements IExceptionGeneratedEvent
{
	private Object itsException;
	private BehaviorInfo itsThrowingBehavior;

	public Object getException()
	{
		return itsException;
	}

	public void setException(Object aException)
	{
		itsException = aException;
	}

	public BehaviorInfo getThrowingBehavior()
	{
		return itsThrowingBehavior;
	}

	public void setThrowingBehavior(BehaviorInfo aThrowingBehavior)
	{
		itsThrowingBehavior = aThrowingBehavior;
	}
}
