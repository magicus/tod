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
package tod.gui.view.event;

import tod.Util;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.ITypeInfo;
import tod.gui.IGUIManager;

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
		String theTypeName = Util.getPrettyName(getType().getName());
		
//		add (createTitledLink(
//				"Type: ", 
//				theTypeName, 
//				LogViewSeedFactory.getDefaultSeed(getGUIManager(), getLogBrowser(), theTypeInfo)));
		
		// Target
		add (createTitledPanel("Target: ", createInspectorLink(theEvent.getTarget())));
	}
	
	protected ITypeInfo getType()
	{
		ITypeInfo theTypeInfo = null;
		IBehaviorInfo theExecutedBehavior = getEvent().getExecutedBehavior();
		if (theExecutedBehavior != null) theTypeInfo = theExecutedBehavior.getType();
		else
		{
			IBehaviorInfo theCalledBehavior = getEvent().getCalledBehavior();
			if (theCalledBehavior != null) theTypeInfo = theCalledBehavior.getType();
		}

		return theTypeInfo;
	}
}
