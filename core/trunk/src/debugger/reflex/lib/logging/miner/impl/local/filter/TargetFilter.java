/*
 * Created on Nov 9, 2004
 */
package reflex.lib.logging.miner.impl.local.filter;

import reflex.lib.logging.miner.impl.local.LocalCollector;
import tod.core.model.event.IBehaviorCallEvent;
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
		Object theTarget;
		
		if (aEvent instanceof IBehaviorCallEvent)
		{
			IBehaviorCallEvent theEvent = (IBehaviorCallEvent) aEvent;
			theTarget = theEvent.getTarget();
		}
		else
		{
			IBehaviorCallEvent theParent = aEvent.getParent();
			theTarget = theParent != null ? theParent.getTarget() : null;
		}
		
		return itsTarget == null || itsTarget.equals(theTarget);
		
	}
}
