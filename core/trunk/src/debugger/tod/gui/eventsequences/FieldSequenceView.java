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

import tod.core.database.browser.ILogBrowser;
import tod.core.database.browser.IObjectInspector;
import tod.core.database.event.IFieldWriteEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IFieldInfo;
import tod.core.database.structure.IMemberInfo;
import tod.gui.IGUIManager;
import tod.gui.SVGHyperlink;
import tod.gui.seed.CFlowSeed;
import tod.gui.view.LogView;
import zz.csg.api.IRectangularGraphicObject;
import zz.csg.api.layout.SequenceLayout;
import zz.csg.impl.SVGGraphicContainer;
import zz.csg.impl.figures.SVGFlowText;

public class FieldSequenceView extends AbstractMemberSequenceView
{
	public static final Color FIELD_COLOR = Color.BLUE;
	
	private IFieldInfo itsField;

	
	public FieldSequenceView(LogView aLogView, IObjectInspector aInspector, IFieldInfo aField)
	{
		super(aLogView, FIELD_COLOR, aInspector);
		itsField = aField;
	}

	public String getTitle()
	{
		return "field " + itsField.getName();
	}

	@Override
	protected IRectangularGraphicObject getBaloon(ILogEvent aEvent)
	{
		IFieldWriteEvent theEvent = (IFieldWriteEvent) aEvent;
		return createFieldWriteBaloon(theEvent);
	}
	
	private IRectangularGraphicObject createFieldWriteBaloon(IFieldWriteEvent aEvent)
	{
		SVGGraphicContainer theContainer = new SVGGraphicContainer();
		theContainer.setLayoutManager(new SequenceLayout());

		// Create hyperlink to call event
		IGUIManager theGUIManager = getLogView().getGUIManager();
		ILogBrowser theLog = getLogView().getLogBrowser();

		CFlowSeed theSeed = new CFlowSeed(theGUIManager, theLog, aEvent);
		SVGHyperlink theHyperlink = SVGHyperlink.create(theSeed, "set", 10, Color.BLUE);
		theContainer.pChildren().add (theHyperlink);
		
		// Colon
		theContainer.pChildren().add (SVGFlowText.create(": ", 10, Color.BLACK));
		
		// Value
		theContainer.pChildren().add(createBaloon(aEvent.getValue()));
		
		return theContainer;
	}

	@Override
	public IMemberInfo getMember()
	{
		return itsField;
	}
}
