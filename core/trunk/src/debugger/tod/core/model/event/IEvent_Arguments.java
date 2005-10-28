/*
 * Created on Nov 9, 2004
 */
package tod.core.model.event;

import tod.core.model.structure.BehaviorInfo;

/**
 * Interface for events that correpond to a behaviour enter.
 * @author gpothier
 */
public interface IEvent_Arguments extends ILogEvent
{
	public Object[] getArguments();
}
