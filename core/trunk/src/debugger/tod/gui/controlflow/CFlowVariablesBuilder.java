/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier

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

Contact: gpothier -at- dcc . uchile . cl
*/
package tod.gui.controlflow;

import java.awt.Color;
import java.awt.Font;
import java.util.List;

import tod.core.ILocationRegistrer.LocalVariableInfo;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.browser.IVariablesInspector;
import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.ICallerSideEvent;
import tod.core.database.event.ILocalVariableWriteEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.gui.FontConfig;
import tod.gui.Hyperlinks;
import tod.gui.IGUIManager;
import zz.csg.api.IRectangularGraphicObject;
import zz.csg.api.layout.SequenceLayout;
import zz.csg.api.layout.StackLayout;
import zz.csg.impl.SVGGraphicContainer;
import zz.csg.impl.figures.SVGFlowText;
import zz.csg.impl.figures.SVGRectangle;
import zz.utils.ui.text.XFont;

/**
 * Builds the variables list for the CFlow view. 
 * @author gpothier
 */
public class CFlowVariablesBuilder
{
	public static final XFont FONT = XFont.DEFAULT_XPLAIN.deriveFont(FontConfig.FONT_SIZE);
	public static final XFont HEADER_FONT = XFont.DEFAULT_XPLAIN.deriveFont(Font.BOLD, FontConfig.HEADER_FONT_SIZE);

	private CFlowView itsView;
	
	
	
	public CFlowVariablesBuilder(CFlowView aView)
	{
		itsView = aView;
	}
	
	public CFlowView getView()
	{
		return itsView;
	}

	public ILogBrowser getEventTrace()
	{
		return getView().getLogBrowser(); 
	}
	
	public IGUIManager getGUIManager()
	{
		return getView().getGUIManager();
	}

	public IRectangularGraphicObject build(ILogEvent aRootEvent, ILogEvent aCurrentEvent)
	{
		SVGGraphicContainer theContainer = new SVGGraphicContainer();
		
		while (true)
		{
			theContainer.pChildren().add(build(aCurrentEvent));
			
			IBehaviorCallEvent theParent = aCurrentEvent.getParent();
			if (theParent == aRootEvent || theParent.getParent().getExecutedBehavior() == null) break;
			
			aCurrentEvent = theParent;

			theContainer.pChildren().add(SVGRectangle.create(0, 0, 50, 5, Color.BLACK));
		}
		
		theContainer.setLayoutManager(new StackLayout());
		return theContainer;
		
	}
	
	private IRectangularGraphicObject build (ILogEvent aCurrentEvent)
	{
		IBehaviorCallEvent theParent = aCurrentEvent.getParent();
		IVariablesInspector theInspector = getEventTrace().createVariablesInspector(theParent);
		theInspector.setCurrentEvent(aCurrentEvent);

		// Determine current object
		Object theCurrentObject = theParent.getTarget();
		
		// Determine available variables
		List<LocalVariableInfo> theVariables;
		
		if (aCurrentEvent instanceof ICallerSideEvent)
		{
			ICallerSideEvent theEvent = (ICallerSideEvent) aCurrentEvent;
			int theBytecodeIndex = theEvent.getOperationBytecodeIndex();
			theVariables = theInspector.getVariables(theBytecodeIndex);
		}
		else theVariables = theInspector.getVariables();
		
		// Create container
		SVGGraphicContainer theContainer = new SVGGraphicContainer();
		
		theContainer.pChildren().add(buildHeader(theParent.getExecutedBehavior()));
		
		if (theCurrentObject != null)
		{
			theContainer.pChildren().add(buildCurrentObjectLine(theCurrentObject));
		}
		
		for (LocalVariableInfo theVariable : theVariables)
		{
			if ("this".equals(theVariable.getVariableName())) continue;
			ILocalVariableWriteEvent theSetter = theInspector.getVariableSetter(theVariable);
			Object theValue = theInspector.getVariableValue(theVariable); // The value might be the initial parameter value, in which case there is no setter
			theContainer.pChildren().add(buildVariableLine(theVariable, theCurrentObject, theValue, theSetter));
		}

		theContainer.setLayoutManager(new StackLayout());
		return theContainer;
	}
	
	private IRectangularGraphicObject buildHeader(IBehaviorInfo aBehavior)
	{
		SVGGraphicContainer theContainer = new SVGGraphicContainer();
		
		if (aBehavior != null)
		{
			theContainer.pChildren().add(SVGFlowText.create("Behavior: ", HEADER_FONT, Color.BLACK));
			theContainer.pChildren().add(Hyperlinks.behavior(getGUIManager(), aBehavior, HEADER_FONT));
			theContainer.pChildren().add(SVGFlowText.create(" ("+aBehavior.getType().getName()+")", HEADER_FONT, Color.BLACK));

			theContainer.setLayoutManager(new SequenceLayout());
		}
		
		return theContainer;
	}
	
	private IRectangularGraphicObject buildVariableLine(
			LocalVariableInfo aVariable, 
			Object aCurrentObject, 
			Object aValue, 
			ILocalVariableWriteEvent aSetter)
	{
		SVGGraphicContainer theContainer = new SVGGraphicContainer();
		
		String theVariableName = aVariable.getVariableName();
		String theTypeName = aVariable.getVariableTypeName();
		String theText = /*theTypeName + " " + */theVariableName + " = ";
		
		theContainer.pChildren().add(SVGFlowText.create(theText, FONT, Color.BLACK));
		theContainer.pChildren().add(Hyperlinks.object(
				getGUIManager(), 
				getEventTrace(), 
				aCurrentObject,
				aValue,
				FONT));
		
		if (aSetter != null)
		{
			theContainer.pChildren().add(SVGFlowText.create(" (", FONT, Color.BLACK));
			theContainer.pChildren().add(Hyperlinks.event(
					getGUIManager(),
					getEventTrace(),
					"why?", 
					aSetter, 
					FONT));
			theContainer.pChildren().add(SVGFlowText.create(")", FONT, Color.BLACK));
		}
		
		theContainer.setLayoutManager(new SequenceLayout());
		return theContainer;		
	}
	
	private IRectangularGraphicObject buildCurrentObjectLine(Object aCurrentObject)
	{
		SVGGraphicContainer theContainer = new SVGGraphicContainer();
		
		theContainer.pChildren().add(SVGFlowText.create("this = ", FONT, Color.BLACK));
		theContainer.pChildren().add(Hyperlinks.object(
				getGUIManager(), 
				getEventTrace(), 
				null,
				aCurrentObject,
				FONT));
		
		theContainer.setLayoutManager(new SequenceLayout());
		return theContainer;		
	}
	
}
