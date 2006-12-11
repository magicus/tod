/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

Parts of this work rely on the MD5 algorithm "derived from the 
RSA Data Security, Inc. MD5 Message-Digest Algorithm".

Contact: gpothier -at- dcc . uchile . cl
*/
package tod.gui.formatter;

import java.util.Arrays;

import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.IFieldWriteEvent;
import tod.core.database.event.ILocalVariableWriteEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.event.IOutputEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.ILocationInfo;
import zz.utils.AbstractFormatter;

/**
 * Formatter for {@link tod.core.database.event.Event}
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
			
			IBehaviorInfo theBehavior = theEvent.getExecutedBehavior();
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
	
	private String formatLocation (ILocationInfo aLocationInfo)
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
