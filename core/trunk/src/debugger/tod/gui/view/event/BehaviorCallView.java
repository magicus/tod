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

import tod.Util;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.ITypeInfo;
import tod.gui.IGUIManager;
import tod.gui.seed.LogViewSeedFactory;

/**
 * View for a {@link tod.core.database.event.MethodEnter} event.
 * @author gpothier
 */
public class BehaviorCallView extends EventView
{
	private IBehaviorCallEvent itsEvent;
	
	public BehaviorCallView(
			IGUIManager aManager, 
			ILogBrowser aLog, 
			IBehaviorCallEvent aEvent)
	{
		super(aManager, aLog);
		itsEvent = aEvent;
	}
	
	protected IBehaviorCallEvent getEvent()
	{
		return itsEvent;
	}
	
	@Override
	public void init()
	{
		super.init();
		
		IBehaviorCallEvent theEvent = getEvent();
		
		// Behaviour
		ITypeInfo theTypeInfo = null;
		IBehaviorInfo theExecutedBehavior = theEvent.getExecutedBehavior();
		if (theExecutedBehavior != null) theTypeInfo = theExecutedBehavior.getType();
		else
		{
			IBehaviorInfo theCalledBehavior = theEvent.getCalledBehavior();
			if (theCalledBehavior != null) theTypeInfo = theCalledBehavior.getType();
		}
		
		String theTypeName = Util.getPrettyName(theTypeInfo.getName());
		
		add (createTitledLink(
				"Type: ", 
				theTypeName, 
				LogViewSeedFactory.getDefaultSeed(getGUIManager(), getLogBrowser(), theTypeInfo)));
		
		// Target
		add (createTitledPanel("Target: ", createInspectorLink(theEvent.getTarget())));
	}
}
