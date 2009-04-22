/*
 * Created on Jul 20, 2006
 */
package tod.core.database.event;

/**
 * This event is produced whenever a behavior exits, either normally
 * or because of an exception.
 * It is always the last event of a {@link IBehaviorCallEvent}.
 * @author gpothier
 */
public interface IBehaviorExitEvent extends ILogEvent
{
	/**
	 * Whether the behavior returned normally or with an
	 * exception.
	 * <br/>
	 * This information is always available.
	 */
	public boolean hasThrown();

	/**
	 * Value returned by the behavior, or exception thrown by the 
	 * behavior, according to the value of {@link #hasThrown()}.
	 */
	public Object getResult();
	

}
