/*
 * Created on Nov 9, 2004
 */
package tod.impl.local.filter;

import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.ObjectId;
import tod.impl.local.LocalBrowser;
import tod.impl.local.LocalBrowser;

/**
 * Filter for all events that have a specific target.
 * @author gpothier
 */
public class TargetFilter extends AbstractStatelessFilter
{
	private ObjectId itsTarget;
	
	public TargetFilter(LocalBrowser aBrowser, ObjectId aTarget)
	{
		super (aBrowser);
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
