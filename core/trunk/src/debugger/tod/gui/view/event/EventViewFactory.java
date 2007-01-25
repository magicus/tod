/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier (gpothier@dcc.uchile.cl)

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
*/
package tod.gui.view.event;

import tod.core.database.browser.ILogBrowser;
import tod.core.database.event.IArrayWriteEvent;
import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.IBehaviorExitEvent;
import tod.core.database.event.IExceptionGeneratedEvent;
import tod.core.database.event.IFieldWriteEvent;
import tod.core.database.event.IInstantiationEvent;
import tod.core.database.event.ILogEvent;
import tod.gui.IGUIManager;

/**
 * This factory permits to obtain an event view given a
 * log event.
 * @author gpothier
 */
public class EventViewFactory
{
	/**
	 * Creates a viewer for the given event.
	 */
	public static EventView createView (
			IGUIManager aGUIManager, 
			ILogBrowser aLog,
			ILogEvent aEvent)
	{
		EventView theView = null;
		
		if (aEvent instanceof IBehaviorCallEvent)
		{
			IBehaviorCallEvent theEvent = (IBehaviorCallEvent) aEvent;
			theView = new BehaviorCallView (aGUIManager, aLog, theEvent);
		}
		else if (aEvent instanceof IBehaviorExitEvent)
		{
			IBehaviorExitEvent theEvent = (IBehaviorExitEvent) aEvent;
			theView = new BehaviorExitView(aGUIManager, aLog, theEvent);
		}
		else if (aEvent instanceof IInstantiationEvent)
		{
			IInstantiationEvent theEvent = (IInstantiationEvent) aEvent;
			theView = new InstantiationView (aGUIManager, aLog, theEvent);
		}
		else if (aEvent instanceof IFieldWriteEvent)
		{
			IFieldWriteEvent theEvent = (IFieldWriteEvent) aEvent;
			theView = new FieldWriteEventView (aGUIManager, aLog, theEvent);
		}
		else if (aEvent instanceof IArrayWriteEvent)
		{
			IArrayWriteEvent theEvent = (IArrayWriteEvent) aEvent;
			theView = new ArrayWriteEventView(aGUIManager, aLog, theEvent);
		}
		else if (aEvent instanceof IExceptionGeneratedEvent)
		{
			IExceptionGeneratedEvent theEvent = (IExceptionGeneratedEvent) aEvent;
			theView = new ExceptionGeneratedView(aGUIManager, aLog, theEvent);
		}
		
		if (theView != null) theView.init();
		return theView;
	}
}
