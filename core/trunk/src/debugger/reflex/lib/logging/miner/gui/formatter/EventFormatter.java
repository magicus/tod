/*
 * Created on Nov 10, 2004
 */
package reflex.lib.logging.miner.gui.formatter;

import tod.core.model.event.IAfterMethodCallEvent;
import tod.core.model.event.IBeforeMethodCallEvent;
import tod.core.model.event.IBehaviorEnterEvent;
import tod.core.model.event.IBehaviorExitEvent;
import tod.core.model.event.IEvent_Arguments;
import tod.core.model.event.IEvent_ReturnValue;
import tod.core.model.event.IFieldWriteEvent;
import tod.core.model.event.IInstantiationEvent;
import tod.core.model.event.ILocalVariableWriteEvent;
import tod.core.model.event.ILogEvent;
import tod.core.model.event.IOutputEvent;
import tod.core.model.structure.LocationInfo;
import zz.utils.AbstractFormatter;

/**
 * Formatter for {@link tod.core.model.event.Event}
 * @author gpothier
 */
public class EventFormatter extends AbstractFormatter<ILogEvent>
{
	private static EventFormatter INSTANCE = new EventFormatter();

	public static EventFormatter getInstance()
	{
		return INSTANCE;
	}

	private EventFormatter()
	{
	}
	
	protected String getText(ILogEvent aEvent, boolean aHtml)
	{
		if (aEvent instanceof IBeforeMethodCallEvent)
		{
			IBeforeMethodCallEvent theEvent = (IBeforeMethodCallEvent) aEvent;
			return "Calling "+formatLocation(theEvent.getBehavior())
				+ " with "+formatArgs(theEvent.getArguments());
		}
		else if (aEvent instanceof IAfterMethodCallEvent)
		{
			IAfterMethodCallEvent theEvent = (IAfterMethodCallEvent) aEvent;
			return "Called "+formatLocation(theEvent.getBehavior());
		}
		if (aEvent instanceof IBehaviorEnterEvent)
		{
			IBehaviorEnterEvent theEvent = (IBehaviorEnterEvent) aEvent;
			return "Entered "+formatLocation(theEvent.getBehavior());
		}
		else if (aEvent instanceof IBehaviorExitEvent)
		{
			IBehaviorExitEvent theEvent = (IBehaviorExitEvent) aEvent;
			return "Exited "+formatLocation(theEvent.getBehavior());
		}
		else if (aEvent instanceof IFieldWriteEvent)
		{
			IFieldWriteEvent theEvent = (IFieldWriteEvent) aEvent;
			
			return "Field written: "+formatLocation(theEvent.getField())
				+" on "+formatObject(theEvent.getTarget())
				+" value: "+formatObject(theEvent.getValue());
		}
        else if (aEvent instanceof ILocalVariableWriteEvent)
		{
			ILocalVariableWriteEvent theEvent = (ILocalVariableWriteEvent) aEvent;
			
            return "Variable written: "+theEvent.getVariable().getVariableName()
            	+" on "+formatObject(theEvent.getTarget())
            	+" value: "+formatObject(theEvent.getValue());
		}
		else if (aEvent instanceof IInstantiationEvent)
		{
			IInstantiationEvent theEvent = (IInstantiationEvent) aEvent;
			return "Object instantiated: "+formatLocation(theEvent.getType());
		}
		else if (aEvent instanceof IOutputEvent)
		{
			IOutputEvent theEvent = (IOutputEvent) aEvent;
			return "Output ("+theEvent.getOutput()+"): "+theEvent.getData();
		}
		else return ""+aEvent;
	}
	
	private String formatObject (Object aObject)
	{
		return ObjectFormatter.getInstance().getPlainText(aObject);
	}
	
	private String formatLocation (LocationInfo aLocationInfo)
	{
		return LocationFormatter.getInstance().getPlainText(aLocationInfo);
	}
	
	private String formatArgs (Object[] aArguments)
	{
		StringBuffer theBuffer = new StringBuffer();
		theBuffer.append("[");
		
		if (aArguments != null) for (Object theArgument : aArguments)
		{
			theBuffer.append(formatObject(theArgument));
			theBuffer.append(" ");
		}
		
		theBuffer.append("]");
		
		return theBuffer.toString();
	}

}
