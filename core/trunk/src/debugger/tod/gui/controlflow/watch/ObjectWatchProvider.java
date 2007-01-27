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

import static tod.gui.FontConfig.STD_FONT;
import static tod.gui.FontConfig.STD_HEADER_FONT;

import java.awt.Color;
import java.util.List;

import tod.core.database.browser.ILogBrowser;
import tod.core.database.browser.IObjectInspector;
import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.event.IWriteEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IFieldInfo;
import tod.core.database.structure.ObjectId;
import tod.gui.Hyperlinks;
import tod.gui.JobProcessor;
import tod.gui.SVGUtils;
import zz.csg.api.IRectangularGraphicObject;
import zz.csg.api.layout.SequenceLayout;
import zz.csg.impl.SVGGraphicContainer;
import zz.csg.impl.figures.SVGFlowText;

public class ObjectWatchProvider implements IWatchProvider<IFieldInfo>
{
	private final WatchPanel itsWatchPanel;
	private final ILogBrowser itsLogBrowser;
	private final ILogEvent itsRefEvent;
	private final ObjectId itsObject;
	
	private IBehaviorCallEvent itsParentEvent;
	private boolean itsInvalid = false;
	
	private IObjectInspector itsInspector;
	
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
	}

	private IObjectInspector getInspector()
	{
		if (itsInspector == null && ! itsInvalid)
		{
			if (itsObject != null)
			{
				itsInspector = itsLogBrowser.createObjectInspector(itsObject);
			}
			else
			{
				IBehaviorCallEvent theParent = itsRefEvent.getParent();
				if (theParent == null)
				{
					itsInvalid = true;
					return null;
				}
				
				IBehaviorInfo theBehavior = theParent.getExecutedBehavior();
				if (theBehavior == null) theBehavior = theParent.getCalledBehavior();
				
				itsInspector = itsLogBrowser.createClassInspector(theBehavior.getType());
			}
			
			itsInspector.setCurrentEvent(itsRefEvent);
		}
		
		return itsInspector;
	}
	
	public IRectangularGraphicObject buildTitle(JobProcessor aJobProcessor)
	{
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
					aJobProcessor,
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
		
		// Setup history link
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
		return theContainer;
	}

	public ObjectId getCurrentObject()
	{
		return null;
	}

	public List<IFieldInfo> getEntries()
	{
		return getInspector().getFields();
	}

	public String getEntryName(IFieldInfo aEntry)
	{
		return aEntry.getName();
	}

	public IWriteEvent[] getEntrySetter(IFieldInfo aEntry)
	{
		return getInspector().getEntrySetter(aEntry);
	}

	public Object[] getEntryValue(IFieldInfo aEntry)
	{
		return getInspector().getEntryValue(aEntry);
	}
	

}
