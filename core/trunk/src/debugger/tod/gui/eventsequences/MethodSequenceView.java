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
package tod.gui.eventsequences;

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
import tod.gui.seed.CFlowSeed;
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
		CFlowSeed theSeed = new CFlowSeed(getGUIManager(), getLogBrowser(), aEvent);
		SeedHyperlink theHyperlink = SeedHyperlink.create(theSeed, "call", 10, Color.BLUE);
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
