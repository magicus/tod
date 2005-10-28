/*
 * Created on Nov 15, 2004
 */
package tod.core.model.event;

import tod.core.model.structure.BehaviorInfo;

/**
 * An event that is related to a behaviour
 * @author gpothier
 */
public interface IEvent_Behaviour extends ILogEvent
{
	public BehaviorInfo getBehavior();
}
