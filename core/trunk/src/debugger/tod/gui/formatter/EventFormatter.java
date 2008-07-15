/*
TOD - Trace Oriented Debugger.
Copyright (c) 2006-2008, Guillaume Pothier
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this 
      list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, 
      this list of conditions and the following disclaimer in the documentation 
      and/or other materials provided with the distribution.
    * Neither the name of the University of Chile nor the names of its contributors 
      may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
POSSIBILITY OF SUCH DAMAGE.

Parts of this work rely on the MD5 algorithm "derived from the RSA Data Security, 
Inc. MD5 Message-Digest Algorithm".
*/
package tod.gui.formatter;

import tod.Util;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.event.IArrayWriteEvent;
import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.IBehaviorExitEvent;
import tod.core.database.event.IExceptionGeneratedEvent;
import tod.core.database.event.IFieldWriteEvent;
import tod.core.database.event.IInstantiationEvent;
import tod.core.database.event.ILocalVariableWriteEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.event.IOutputEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.ILocationInfo;
import zz.utils.AbstractFormatter;

/**
 * Formatter for {@link ILogEvent}s
 * @author gpothier
 */
public class EventFormatter extends AbstractFormatter<ILogEvent>
{
	private ILogBrowser itsLogBrowser;
	private ObjectFormatter itsObjectFormatter;
	
	public EventFormatter(ILogBrowser aLogBrowser)
	{
		itsLogBrowser = aLogBrowser;
		itsObjectFormatter = new ObjectFormatter(itsLogBrowser);
	}

	protected String getText(ILogEvent aEvent, boolean aHtml)
	{
		if (aEvent instanceof IInstantiationEvent)
		{
			IInstantiationEvent theEvent = (IInstantiationEvent) aEvent;
			
			return String.format(
					"%s (%s)",
					Util.getPrettyName(theEvent.getType().getName()),
	                formatArgs(theEvent.getArguments()));
		}
		else if (aEvent instanceof IBehaviorCallEvent)
		{
			IBehaviorCallEvent theEvent = (IBehaviorCallEvent) aEvent;
			
			IBehaviorInfo theBehavior = theEvent.getExecutedBehavior();
			if (theBehavior == null) theBehavior = theEvent.getCalledBehavior();
			
			return String.format(
					"%s.%s (%s)",
					Util.getPrettyName(theBehavior.getType().getName()),
	                theBehavior.getName(),
	                formatArgs(theEvent.getArguments()));
		}
		else if (aEvent instanceof IBehaviorExitEvent)
		{
			IBehaviorExitEvent theEvent = (IBehaviorExitEvent) aEvent;
			IBehaviorCallEvent theParent = theEvent.getParent();
			
			if (theParent != null)
			{
				IBehaviorInfo theBehavior = theParent.getExecutedBehavior();
				if (theBehavior == null) theBehavior = theParent.getCalledBehavior();

				return String.format(
						"Return from %s.%s -> %s",
						Util.getPrettyName(theBehavior.getType().getName()),
		                theBehavior.getName(),
		                theEvent.getResult());
			}
			else
			{
				return String.format(
						"Return from ? -> %s",
		                theEvent.getResult());
			}
		}
		else if (aEvent instanceof IFieldWriteEvent)
		{
			IFieldWriteEvent theEvent = (IFieldWriteEvent) aEvent;

			return String.format(
					"%s.%s = %s",
					Util.getPrettyName(theEvent.getField().getType().getName()),
					theEvent.getField().getName(),
					formatObject(theEvent.getValue()));
		}
        else if (aEvent instanceof ILocalVariableWriteEvent)
		{
			ILocalVariableWriteEvent theEvent = (ILocalVariableWriteEvent) aEvent;
			
			return String.format(
					"%s = %s",
					theEvent.getVariable().getVariableName(),
					formatObject(theEvent.getValue()));
		}
		else if (aEvent instanceof IOutputEvent)
		{
			IOutputEvent theEvent = (IOutputEvent) aEvent;
			return "Output ("+theEvent.getOutput()+"): "+theEvent.getData();
		}
		else if (aEvent instanceof IExceptionGeneratedEvent)
		{
			IExceptionGeneratedEvent theEvent = (IExceptionGeneratedEvent) aEvent;
			IBehaviorInfo theBehavior = theEvent.getOperationBehavior();
			String theBehaviorName = theBehavior != null ? 
					Util.getSimpleName(theBehavior.getType().getName()) + "." + theBehavior.getName() 
					: "<unknown>"; 
			return "Exception thrown in "+theBehaviorName+": "+formatObject(theEvent.getException());
		}
		else if (aEvent instanceof IArrayWriteEvent)
		{
			IArrayWriteEvent theEvent = (IArrayWriteEvent) aEvent;
			
			return String.format(
					"%s[%d] = %s",
					formatObject(theEvent.getTarget()),
					theEvent.getIndex(),
					formatObject(theEvent.getValue()));
		}
		else return ""+aEvent;
	}
	
	public String formatObject (Object aObject)
	{
		return itsObjectFormatter.getPlainText(aObject);
	}
	
	private String formatLocation (ILocationInfo aLocationInfo)
	{
		return LocationFormatter.getInstance().getPlainText(aLocationInfo);
	}
	
	private String formatArgs (Object[] aArguments)
	{
		StringBuffer theBuffer = new StringBuffer();
		
		boolean theFirst = true;
		if (aArguments != null) for (Object theArgument : aArguments)
		{
			if (! theFirst) theBuffer.append(", ");
			else theFirst = false;
			
			theBuffer.append(formatObject(theArgument));
		}
		
		return theBuffer.toString();
	}

	/**
	 * Formats the given event.
	 */
	public static String formatEvent(ILogBrowser aLogBrowser, ILogEvent aEvent)
	{
		return new EventFormatter(aLogBrowser).getText(aEvent, false);
	}
}
