/*
 * Created on Oct 25, 2004
 */
package tod.impl.common.event;

import tod.core.model.event.IExceptionGeneratedEvent;
import tod.core.model.structure.IBehaviorInfo;

/**
 * @author gpothier
 */
public class ExceptionGeneratedEvent extends Event implements IExceptionGeneratedEvent
{
	private Object itsException;
	private IBehaviorInfo itsThrowingBehavior;

	public Object getException()
	{
		return itsException;
	}

	public void setException(Object aException)
	{
		itsException = aException;
	}

	public IBehaviorInfo getThrowingBehavior()
	{
		return itsThrowingBehavior;
	}

	public void setThrowingBehavior(IBehaviorInfo aThrowingBehavior)
	{
		itsThrowingBehavior = aThrowingBehavior;
	}
}
