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
package tod.gui.components.event;

import tod.core.database.event.IExceptionGeneratedEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.gui.IGUIManager;
import tod.gui.activities.ActivitySeed;
import tod.gui.activities.ActivitySeedFactory;

public class ExceptionGeneratedView extends EventView
{
	private IExceptionGeneratedEvent itsEvent;
	
	public ExceptionGeneratedView(IGUIManager aManager, IExceptionGeneratedEvent aEvent)
	{
		super(aManager);
		itsEvent = aEvent;
	}
	
	@Override
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
		
		ActivitySeed theSeed = ActivitySeedFactory.getDefaultSeed(getLogBrowser(), theBehavior);
		add (createTitledLink("Occured in: ", theBehaviorName, theSeed));
	}

}
