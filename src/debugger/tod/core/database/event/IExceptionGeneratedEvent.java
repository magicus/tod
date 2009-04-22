/*
 * Created on Oct 29, 2005
 */
package tod.core.database.event;

import tod.core.database.structure.IBehaviorInfo;

public interface IExceptionGeneratedEvent extends ICallerSideEvent
{
	/**
	 * The generated exception
	 */
	public Object getException();
	
	/**
	 * Returns the behavior that throws the exception.
	 */
	public IBehaviorInfo getThrowingBehavior();
}
