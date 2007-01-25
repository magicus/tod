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
package tod.gui.controlflow.watch;

import static tod.gui.FontConfig.*;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import tod.core.database.browser.ILogBrowser;
import tod.core.database.browser.IObjectInspector;
import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.IFieldWriteEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IFieldInfo;
import tod.core.database.structure.ObjectId;
import tod.gui.Hyperlinks;
import tod.gui.SVGHyperlink;
import tod.gui.SVGUtils;
import tod.gui.seed.FilterSeed;
import zz.csg.api.IRectangularGraphicObject;
import zz.csg.api.layout.SequenceLayout;
import zz.csg.impl.SVGGraphicContainer;
import zz.csg.impl.figures.SVGFlowText;

public class ObjectWatchProvider implements IWatchProvider
{
	private final WatchPanel itsWatchPanel;
	private final ILogBrowser itsLogBrowser;
	private final ILogEvent itsRefEvent;
	private final ObjectId itsObject;
	
	private IRectangularGraphicObject itsTitle;
	private WatchEntry itsCurrentObject;
	private List<WatchEntry> itsEntries;
	
	public ObjectWatchProvider(
			WatchPanel aWatchPanel,
			ILogBrowser aLogBrowser, 
			ILogEvent aRefEvent,
			ObjectId aObject)
	{
		itsWatchPanel = aWatchPanel;
		itsLogBrowser = aLogBrowser;
		itsRefEvent = aRefEvent;
		itsObject = aObject;
		init();
	}

	private void init()
	{
		IBehaviorCallEvent theParent = itsRefEvent.getParent();
		
		if (theParent == null)
		{
			itsTitle = SVGUtils.createMessage(
					"Object information not available",
					Color.DARK_GRAY,
					"Cause: the currently selected event is a control flow root.",
					Color.DARK_GRAY);
		}
		else
		{
			// Setup title
			SVGGraphicContainer theContainer = new SVGGraphicContainer();
			
			theContainer.pChildren().add(SVGFlowText.create(
					"Object: ", 
					STD_HEADER_FONT, 
					Color.BLACK));
			
			if (itsObject != null)
			{
				theContainer.pChildren().add(Hyperlinks.object(
						itsWatchPanel.getWatchSeedFactory(), 
						itsLogBrowser, 
						itsObject, 
						STD_HEADER_FONT));
			}
			else
			{
				theContainer.pChildren().add(SVGFlowText.create(
						"(static)", 
						STD_HEADER_FONT, 
						Color.GRAY));
			}
			
			theContainer.pChildren().add(SVGFlowText.create(
					" (",
					STD_FONT,
					Color.BLACK));
			
			theContainer.pChildren().add(Hyperlinks.history(
					itsWatchPanel.getLogViewSeedFactory(),
					itsObject,
					STD_FONT));
			
			theContainer.pChildren().add(SVGFlowText.create(
					")",
					STD_FONT,
					Color.BLACK));
			
			theContainer.setLayoutManager(new SequenceLayout());
			itsTitle = theContainer;

			// Find fields
			itsEntries = new ArrayList<WatchEntry>();
			
			IObjectInspector theInspector;
			if (itsObject != null)
			{
				theInspector = itsLogBrowser.createObjectInspector(itsObject);
			}
			else
			{
				IBehaviorInfo theBehavior = theParent.getExecutedBehavior();
				if (theBehavior == null) theBehavior = theParent.getCalledBehavior();
				
				theInspector = itsLogBrowser.createClassInspector(theBehavior.getType());
			}
			
			theInspector.setCurrentEvent(itsRefEvent);

			// Determine available fields
			List<IFieldInfo> theFields = theInspector.getFields();
			
			for (IFieldInfo theField : theFields)
			{
				List<IFieldWriteEvent> theSetters = theInspector.getFieldSetter(theField);
				Object[] theValuesArray = new Object[theSetters.size()];
				ILogEvent[] theSettersArray = new ILogEvent[theSetters.size()];

				for(int i=0;i<theSetters.size();i++)
				{
					IFieldWriteEvent theSetter = theSetters.get(i);
					theSettersArray[i] = theSetter;
					theValuesArray[i] = theSetter.getValue();
				}
				
				itsEntries.add(new WatchEntry(theField.getName(), theValuesArray, theSettersArray)); 
			}
		}
	}
	
	public IRectangularGraphicObject buildTitle()
	{
		return itsTitle;
	}

	public WatchEntry getCurrentObject()
	{
		return itsCurrentObject;
	}

	public List<WatchEntry> getEntries()
	{
		return itsEntries;
	}

}
