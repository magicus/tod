/*
 * Created on Nov 16, 2004
 */
package tod.core.model.event;

/**
 * Interface for events that have a target.
 * @author gpothier
 */
public interface IEvent_Target extends ILogEvent
{
	public Object getTarget();

}
