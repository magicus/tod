/*
 * Created on Nov 9, 2004
 */
package tod.impl.local.filter;

import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.IFieldWriteEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.ObjectId;
import tod.impl.local.LocalBrowser;
import tod.impl.local.LocalBrowser;

/**
 * @author gpothier
 */
public class ArgumentFilter extends AbstractStatelessFilter
{
	private ObjectId itsObjectId;
	
	public ArgumentFilter(LocalBrowser aBrowser, ObjectId aObjectId)
	{
		super (aBrowser);
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
