/*
 * Created on Nov 9, 2004
 */
package reflex.lib.logging.miner.impl.local.filter;

import reflex.lib.logging.miner.impl.local.LocalCollector;
import tod.core.model.event.IEvent_Target;
import tod.core.model.event.ILogEvent;
import tod.core.model.structure.ObjectId;

/**
 * Filter for all events that have a specific target.
 * @author gpothier
 */
public class TargetFilter extends AbstractStatelessFilter
{
	private ObjectId itsTarget;
	
	public TargetFilter(LocalCollector aCollector, ObjectId aTarget)
	{
		super (aCollector);
		itsTarget = aTarget;
	}
	
	public boolean accept(ILogEvent aEvent)
	{
		if (aEvent instanceof IEvent_Target)
		{
			IEvent_Target theTargetEvent = (IEvent_Target) aEvent;
			return itsTarget.equals(theTargetEvent.getTarget());
		}
		else return false;
	}
}
