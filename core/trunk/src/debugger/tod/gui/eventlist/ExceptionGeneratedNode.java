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
package tod.gui.eventlist;

import java.awt.Color;

import tod.Util;
import tod.core.database.event.IExceptionGeneratedEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.gui.FontConfig;
import tod.gui.Hyperlinks;
import tod.gui.IGUIManager;
import tod.gui.kit.StdOptions;
import tod.gui.kit.html.HtmlBody;
import tod.gui.kit.html.HtmlText;

public class ExceptionGeneratedNode extends AbstractSimpleEventNode
{
	private IExceptionGeneratedEvent itsEvent;

	public ExceptionGeneratedNode(
			IGUIManager aGUIManager, 
			EventListPanel aListPanel,
			IExceptionGeneratedEvent aEvent)
	{
		super(aGUIManager, aListPanel);
		itsEvent = aEvent;
		createUI();
	}
	
	@Override
	protected void createHtmlUI(HtmlBody aBody)
	{
		boolean theRed = getListPanel() != null ?
				getListPanel().getCurrentValue(StdOptions.EXCEPTION_EVENTS_RED)
				: false;
		
		aBody.add(HtmlText.create(
				"Exception: ", 
				FontConfig.NORMAL, 
				theRed ? Color.RED : Color.BLACK));
		
		aBody.add(Hyperlinks.object(
				getGUIManager(),
				Hyperlinks.HTML,
				getJobProcessor(),
				itsEvent.getException(),
				itsEvent,
				showPackageNames()));

		boolean theLocation = getListPanel() != null ?
				getListPanel().getCurrentValue(StdOptions.SHOW_EVENTS_LOCATION)
				: false;
				
		if (theLocation)
		{
			IBehaviorInfo theBehavior = getEvent().getOperationBehavior();
			String theBehaviorName = theBehavior != null ? 
					Util.getSimpleName(theBehavior.getType().getName()) + "." + theBehavior.getName() 
					: "<unknown>"; 
			
			aBody.addText(" (in "+theBehaviorName+")");
		}
				
		createDebugInfo(aBody);
	}
	
	@Override
	protected IExceptionGeneratedEvent getEvent()
	{
		return itsEvent;
	}

}
