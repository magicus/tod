/*
 * Created on Oct 25, 2004
 */
package reflex.lib.logging.miner.impl.common.event;

import java.util.ArrayList;
import java.util.List;

import tod.core.model.event.IBehaviorEnterEvent;
import tod.core.model.event.IEvent_Arguments;

/**
 * The behavior enter event is the parent of the events that occur during the execution
 * of the behavior. 
 * @author gpothier
 */
public class BehaviorEnter extends BehaviorEvent implements IBehaviorEnterEvent
{
	private List<Event> itsChildren;

	public List<Event> getChildren()
	{
		return itsChildren;
	}

	public void addChild (Event aEvent)
	{
		if (itsChildren == null) itsChildren = new ArrayList<Event>();
		itsChildren.add(aEvent);
	}
}
