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
package tod.gui.components.eventsequences;

import java.awt.Color;
import java.awt.FlowLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;

import tod.core.database.browser.IObjectInspector;
import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IMemberInfo;
import tod.gui.FontConfig;
import tod.gui.IGUIManager;
import tod.gui.SeedHyperlink;
import tod.gui.activities.cflow.CFlowSeed;
import zz.utils.ui.ZLabel;
import zz.utils.ui.text.XFont;

public class MethodSequenceView extends AbstractMemberSequenceView
{
	public static final Color METHOD_COLOR = Color.GREEN;
	
	private IBehaviorInfo itsMethod;

	
	public MethodSequenceView(IGUIManager aGUIManager, IObjectInspector aInspector, IBehaviorInfo aMethod)
	{
		super(aGUIManager, METHOD_COLOR, aInspector);
		itsMethod = aMethod;
	}

	public String getTitle()
	{
		return "Method " + itsMethod.getName();
	}

	@Override
	protected JComponent getBaloon(ILogEvent aEvent)
	{
		if (aEvent instanceof IBehaviorCallEvent)
		{
			IBehaviorCallEvent theEvent = (IBehaviorCallEvent) aEvent;
			return createBehaviorCallBaloon(theEvent);
		}
		else return null;
	}
	
	private JComponent createBehaviorCallBaloon (IBehaviorCallEvent aEvent)
	{
		XFont theFont = FontConfig.TINY_FONT;
		JPanel theContainer = new JPanel(new FlowLayout(FlowLayout.LEFT));

		// Create hyperlink to call event
		CFlowSeed theSeed = new CFlowSeed(getLogBrowser(), aEvent);
		SeedHyperlink theHyperlink = SeedHyperlink.create(getGUIManager(), theSeed, "call");
		theContainer.add (theHyperlink);
		
		// Open parenthesis
		theContainer.add (ZLabel.create(" (", theFont, Color.BLACK));
		
		// Create links of individual arguments
		Object[] theArguments = aEvent.getArguments();
		boolean theFirst = true;
		for (Object theArgument : theArguments)
		{
			if (theFirst) theFirst = false;
			else
			{
				theContainer.add (ZLabel.create(", ", theFont, Color.BLACK));						
			}
			
			theContainer.add(createBaloon(theArgument));
		}
		
		// Close parenthesis
		theContainer.add (ZLabel.create(")", theFont, Color.BLACK));

		// Return value
		theContainer.add (ZLabel.create("return: ", theFont, Color.BLACK));
		theContainer.add (createBaloon(aEvent.getExitEvent().getResult()));
		
		return theContainer;
		
	}

	@Override
	public IMemberInfo getMember()
	{
		return itsMethod;
	}
}
