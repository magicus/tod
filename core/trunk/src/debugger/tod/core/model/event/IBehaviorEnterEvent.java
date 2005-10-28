/*
 * Created on Aug 30, 2005
 */
package tod.core.model.event;

import java.util.List;

import reflex.lib.logging.miner.impl.common.event.Event;

public interface IBehaviorEnterEvent extends IEvent_Behaviour
{
	/**
	 * Returns the list of events that occured during the execution of the
	 * behavior corresponding to this event.
	 * @see ILogEvent#getFather()
	 */
	public List<Event> getChildren();

}
