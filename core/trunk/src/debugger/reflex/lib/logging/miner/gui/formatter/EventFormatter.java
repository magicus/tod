/*
 * Created on Nov 10, 2004
 */
package reflex.lib.logging.miner.gui.formatter;

import java.util.Arrays;

import tod.core.model.event.IBehaviorCallEvent;
import tod.core.model.event.IFieldWriteEvent;
import tod.core.model.event.ILocalVariableWriteEvent;
import tod.core.model.event.ILogEvent;
import tod.core.model.event.IOutputEvent;
import tod.core.model.structure.BehaviorInfo;
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
		if (aEvent instanceof IBehaviorCallEvent)
		{
			IBehaviorCallEvent theEvent = (IBehaviorCallEvent) aEvent;
			
			BehaviorInfo theBehavior = theEvent.getExecutedBehavior();
			if (theBehavior == null) theBehavior = theEvent.getCalledBehavior();
			
			return String.format(
					"%s.%s (%s)",
					theBehavior.getType().getName(),
	                theBehavior.getName(),
	                Arrays.asList(theEvent.getArguments()));
		}
		else if (aEvent instanceof IFieldWriteEvent)
		{
			IFieldWriteEvent theEvent = (IFieldWriteEvent) aEvent;

			return String.format(
					"%s.%s = %s",
					theEvent.getField().getType().getName(),
					theEvent.getField().getName(),
					formatObject(theEvent.getValue()));
		}
        else if (aEvent instanceof ILocalVariableWriteEvent)
		{
			ILocalVariableWriteEvent theEvent = (ILocalVariableWriteEvent) aEvent;
			
            return "Variable written: "+theEvent.getVariable().getVariableName()
            	+" value: "+formatObject(theEvent.getValue());
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
