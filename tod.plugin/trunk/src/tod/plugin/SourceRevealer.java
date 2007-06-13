package tod.plugin;

import org.eclipse.debug.core.ILaunch;

import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.ICallerSideEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.ITypeInfo;

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
	
	protected final void gotoSource (ICallerSideEvent aEvent)
	{
		IBehaviorCallEvent theParent = aEvent.getParent();
	    if (theParent == null) return;
	    
	    int theBytecodeIndex = aEvent.getOperationBytecodeIndex();
	    IBehaviorInfo theBehavior = theParent.getExecutedBehavior();
	    if (theBehavior == null) return;
	    
	    int theLineNumber = theBehavior.getLineNumber(theBytecodeIndex);
	    ITypeInfo theType = theBehavior.getType();
	    
	    String theTypeName = theType.getName();
	    gotoSource(theTypeName, theLineNumber);
	}
	
	protected abstract void gotoSource(String aTypeName, int aLineNumber);
	protected abstract void gotoSource (IBehaviorInfo aBehavior);

}