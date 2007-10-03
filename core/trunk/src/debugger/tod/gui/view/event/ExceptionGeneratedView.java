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
import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.IExceptionGeneratedEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.ITypeInfo;
import tod.gui.IGUIManager;
import tod.gui.seed.LogViewSeed;
import tod.gui.seed.LogViewSeedFactory;

public class ExceptionGeneratedView extends EventView
{
	private IExceptionGeneratedEvent itsEvent;
	
	public ExceptionGeneratedView(
			IGUIManager aManager, 
			ILogBrowser aLog, 
			IExceptionGeneratedEvent aEvent)
	{
		super(aManager, aLog);
		itsEvent = aEvent;
	}
	
	protected IExceptionGeneratedEvent getEvent()
	{
		return itsEvent;
	}
	
	@Override
	public void init()
	{
		super.init();
		
		IExceptionGeneratedEvent theEvent = getEvent();
		
		// Target
		add (createTitledPanel("Exception: ", createInspectorLink(theEvent.getException())));
		
		// Behaviour
		IBehaviorInfo theBehavior = theEvent.getOperationBehavior();
		String theBehaviorName = theBehavior != null ? theBehavior.getName() : "<unknown>";
		
		LogViewSeed theSeed = LogViewSeedFactory.getDefaultSeed(
				getGUIManager(), 
				getLogBrowser(), 
				theBehavior);
		
		add (createTitledLink("Occured in: ", theBehaviorName, theSeed));
	}

}
