/*
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
package tod.gui.eventsequences;

import java.awt.Color;
import java.awt.FlowLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;

import tod.core.database.browser.IObjectInspector;
import tod.core.database.event.IFieldWriteEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IFieldInfo;
import tod.core.database.structure.IMemberInfo;
import tod.gui.FontConfig;
import tod.gui.IGUIManager;
import tod.gui.SeedHyperlink;
import tod.gui.seed.CFlowSeed;
import zz.utils.ui.ZLabel;

public class FieldSequenceView extends AbstractMemberSequenceView
{
	public static final Color FIELD_COLOR = Color.BLUE;
	
	private IFieldInfo itsField;

	
	public FieldSequenceView(IGUIManager aGUIManager, IObjectInspector aInspector, IFieldInfo aField)
	{
		super(aGUIManager, FIELD_COLOR, aInspector);
		itsField = aField;
	}

	public String getTitle()
	{
		return "field " + itsField.getName();
	}

	@Override
	protected JComponent getBaloon(ILogEvent aEvent)
	{
		IFieldWriteEvent theEvent = (IFieldWriteEvent) aEvent;
		return createFieldWriteBaloon(theEvent);
	}
	
	private JComponent createFieldWriteBaloon(IFieldWriteEvent aEvent)
	{
		JPanel theContainer = new JPanel(new FlowLayout(FlowLayout.LEFT));

		// Create hyperlink to call event
		CFlowSeed theSeed = new CFlowSeed(getGUIManager(), getLogBrowser(), aEvent);
		SeedHyperlink theHyperlink = SeedHyperlink.create(theSeed, "set", 10, Color.BLUE);
		theContainer.add (theHyperlink);
		
		// Colon
		theContainer.add (ZLabel.create(": ", FontConfig.TINY_FONT, Color.BLACK));
		
		// Value
		theContainer.add(createBaloon(aEvent.getValue()));
		
		return theContainer;
	}

	@Override
	public IMemberInfo getMember()
	{
		return itsField;
	}
}
