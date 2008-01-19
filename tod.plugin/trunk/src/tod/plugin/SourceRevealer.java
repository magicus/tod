package tod.plugin;

import org.eclipse.debug.core.ILaunch;

import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.ICallerSideEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.SourceRange;
import tod.utils.TODUtils;

/**
 * An interface for source code lookups.
 * @author gpothier
 */
public abstract class SourceRevealer
{
	private ILaunch itsLaunch;
	
	public SourceRevealer(ILaunch aLaunch)
	{
		itsLaunch = aLaunch;
	}

	protected ILaunch getLaunch()
	{
		return itsLaunch;
	}

	public final void gotoSource (ILogEvent aEvent)
	{
		TODUtils.log(2,"[SourceRevealer.gotoSource(ILogEvent)]: "+aEvent);
		if (aEvent instanceof ICallerSideEvent)
		{
			ICallerSideEvent theEvent = (ICallerSideEvent) aEvent;
			gotoSource(theEvent);
		}
		else if (aEvent instanceof IBehaviorCallEvent)
		{
			IBehaviorCallEvent theEvent = (IBehaviorCallEvent) aEvent;
			gotoSource(theEvent.getExecutedBehavior());
		}
	}
	
	protected abstract void gotoSource(SourceRange aSourceRange);
	protected abstract void gotoSource (IBehaviorInfo aBehavior);

}