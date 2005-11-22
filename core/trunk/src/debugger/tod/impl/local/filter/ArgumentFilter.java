/*
 * Created on Nov 9, 2004
 */
package tod.impl.local.filter;

import tod.core.model.event.IBehaviorCallEvent;
import tod.core.model.event.IFieldWriteEvent;
import tod.core.model.event.ILogEvent;
import tod.core.model.structure.ObjectId;
import tod.impl.local.LocalCollector;

/**
 * @author gpothier
 */
public class ArgumentFilter extends AbstractStatelessFilter
{
	private ObjectId itsObjectId;
	
	public ArgumentFilter(LocalCollector aCollector, ObjectId aObjectId)
	{
		super (aCollector);
		itsObjectId = aObjectId;
	}
	
	public boolean accept(ILogEvent aEvent)
	{
		if (aEvent instanceof IBehaviorCallEvent)
		{
			IBehaviorCallEvent theEvent = (IBehaviorCallEvent) aEvent;
			for (Object theArgument : theEvent.getArguments())
			{
				if (itsObjectId.equals(theArgument)) return true;
			}
			return false;
		}
		else if (aEvent instanceof IFieldWriteEvent)
		{
			IFieldWriteEvent theEvent = (IFieldWriteEvent) aEvent;
			return itsObjectId.equals(theEvent.getValue());
		}
		else return false;
	}
}
