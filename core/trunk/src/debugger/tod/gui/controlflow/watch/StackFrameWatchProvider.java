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

import static tod.gui.FontConfig.STD_HEADER_FONT;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import tod.Util;
import tod.core.ILocationRegisterer.LocalVariableInfo;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.browser.IVariablesInspector;
import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.ICallerSideEvent;
import tod.core.database.event.ILocalVariableWriteEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.gui.Hyperlinks;
import tod.gui.SVGUtils;
import zz.csg.api.IRectangularGraphicObject;
import zz.csg.api.layout.SequenceLayout;
import zz.csg.impl.SVGGraphicContainer;
import zz.csg.impl.figures.SVGFlowText;

/**
 * Watch provider for stack frame reconstitution
 * @author gpothier
 */
public class StackFrameWatchProvider implements IWatchProvider
{
	private final WatchPanel itsWatchPanel;
	private final ILogBrowser itsLogBrowser;
	private final ILogEvent itsRefEvent;
	
	private IRectangularGraphicObject itsTitle;
	private WatchEntry itsCurrentObject;
	private List<WatchEntry> itsEntries;
	
	public StackFrameWatchProvider(WatchPanel aWatchPanel, ILogBrowser aLogBrowser, ILogEvent aRefEvent)
	{
		itsWatchPanel = aWatchPanel;
		itsLogBrowser = aLogBrowser;
		itsRefEvent = aRefEvent;
		init();
	}
	
	private void init()
	{
		itsEntries = new ArrayList<WatchEntry>();
		IBehaviorCallEvent theParentEvent = itsRefEvent.getParent();

		if (theParentEvent == null)
		{
			itsTitle = SVGUtils.createMessage(
					"Variable information not available", 
					Color.DARK_GRAY,
					"Cause: the currently selected event is a control flow root.",
					Color.DARK_GRAY);
		}
		else if (! theParentEvent.isDirectParent())
		{
			itsTitle = SVGUtils.createMessage(
					"Variable information not available", 
					Color.DARK_GRAY,
					"Cause: missing control flow information, check working set.",
					Color.DARK_GRAY);
		}
		else
		{
			// Setup title
			IBehaviorInfo theBehavior = theParentEvent.getExecutedBehavior();
			
			SVGGraphicContainer theContainer = new SVGGraphicContainer();
			
			if (theBehavior != null)
			{
				theContainer.pChildren().add(SVGFlowText.create("Behavior: ", STD_HEADER_FONT, Color.BLACK));
				theContainer.pChildren().add(Hyperlinks.behavior(itsWatchPanel.getLogViewSeedFactory(), theBehavior, STD_HEADER_FONT));
				theContainer.pChildren().add(SVGFlowText.create(" ("+Util.getPrettyName(theBehavior.getType().getName())+")", STD_HEADER_FONT, Color.BLACK));

				theContainer.setLayoutManager(new SequenceLayout());
			}
			
			itsTitle = theContainer;
			
			// Find entries
			IVariablesInspector theInspector = itsLogBrowser.createVariablesInspector(theParentEvent);
			theInspector.setCurrentEvent(itsRefEvent);

			// Determine current object
			Object theCurrentObject = theParentEvent.getTarget();
			
			// Determine available variables
			List<LocalVariableInfo> theVariables;
			
			if (itsRefEvent instanceof ICallerSideEvent)
			{
				ICallerSideEvent theEvent = (ICallerSideEvent) itsRefEvent;
				int theBytecodeIndex = theEvent.getOperationBytecodeIndex();
				theVariables = theInspector.getVariables(theBytecodeIndex);
			}
			else theVariables = theInspector.getVariables();
			
			if (theCurrentObject != null) itsCurrentObject = new WatchEntry("this", theCurrentObject);
			
			for (LocalVariableInfo theVariable : theVariables)
			{
				if ("this".equals(theVariable.getVariableName())) continue;
				
				ILocalVariableWriteEvent theSetter = theInspector.getVariableSetter(theVariable);
				// The value might be the initial parameter value, in which case there is no setter
				Object theValue = theInspector.getVariableValue(theVariable);
				
				itsEntries.add(new WatchEntry(
						theVariable.getVariableName(), 
						theValue, 
						theSetter));
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
