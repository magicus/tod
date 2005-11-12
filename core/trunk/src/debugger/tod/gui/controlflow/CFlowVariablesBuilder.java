/*
 * Created on Nov 4, 2005
 */
package tod.gui.controlflow;

import java.awt.Color;
import java.awt.Font;
import java.util.List;

import javax.swing.text.StyledEditorKit.FontSizeAction;

import reflex.lib.logging.miner.gui.IGUIManager;
import tod.core.ILocationRegistrer.LocalVariableInfo;
import tod.core.model.event.IBehaviorEnterEvent;
import tod.core.model.event.IEvent_Location;
import tod.core.model.event.ILogEvent;
import tod.core.model.structure.BehaviorInfo;
import tod.core.model.trace.IEventTrace;
import tod.core.model.trace.IVariablesInspector;
import tod.gui.Hyperlinks;
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
	public static final XFont FONT = XFont.DEFAULT_XPLAIN.deriveFont(12);
	public static final XFont HEADER_FONT = XFont.DEFAULT_XPLAIN.deriveFont(Font.BOLD, 14);

	private CFlowView itsView;
	
	
	
	public CFlowVariablesBuilder(CFlowView aView)
	{
		itsView = aView;
	}
	
	public CFlowView getView()
	{
		return itsView;
	}

	public IEventTrace getEventTrace()
	{
		return getView().getEventTrace(); 
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
			
			IBehaviorEnterEvent theBehaviorEnter = aCurrentEvent.getParent();
			if (theBehaviorEnter == aRootEvent || theBehaviorEnter.getParent().getBehavior() == null) break;
			
			aCurrentEvent = theBehaviorEnter;

			theContainer.pChildren().add(SVGRectangle.create(0, 0, 50, 5, Color.BLACK));
		}
		
		theContainer.setLayoutManager(new StackLayout());
		return theContainer;
		
	}
	
	private IRectangularGraphicObject build (ILogEvent aCurrentEvent)
	{
		IBehaviorEnterEvent theBehaviorEnter = aCurrentEvent.getParent();
		IVariablesInspector theInspector = getEventTrace().createVariablesInspector(theBehaviorEnter);
		theInspector.setCurrentEvent(aCurrentEvent);

		// Determine current object
		Object theCurrentObject = theBehaviorEnter.getTarget();
		
		// Determine available variables
		List<LocalVariableInfo> theVariables;
		
		if (aCurrentEvent instanceof IEvent_Location)
		{
			IEvent_Location theEvent = (IEvent_Location) aCurrentEvent;
			int theBytecodeIndex = theEvent.getOperationBytecodeIndex();
			theVariables = theInspector.getVariables(theBytecodeIndex);
		}
		else theVariables = theInspector.getVariables();
		
		// Create container
		SVGGraphicContainer theContainer = new SVGGraphicContainer();
		
		theContainer.pChildren().add(buildHeader(theBehaviorEnter.getBehavior()));
		
		if (theCurrentObject != null)
		{
			theContainer.pChildren().add(buildCurrentObjectLine(theCurrentObject));
		}
		
		for (LocalVariableInfo theVariable : theVariables)
		{
			if ("this".equals(theVariable.getVariableName())) continue;
			Object theValue = theInspector.getVariableValue(theVariable);
			theContainer.pChildren().add(buildVariableLine(theVariable, theCurrentObject, theValue));
		}

		theContainer.setLayoutManager(new StackLayout());
		return theContainer;
	}
	
	private IRectangularGraphicObject buildHeader(BehaviorInfo aBehavior)
	{
		SVGGraphicContainer theContainer = new SVGGraphicContainer();
		
		theContainer.pChildren().add(SVGFlowText.create("Behavior: ", HEADER_FONT, Color.BLACK));
		theContainer.pChildren().add(Hyperlinks.behavior(getGUIManager(), aBehavior, HEADER_FONT));
		
		theContainer.setLayoutManager(new SequenceLayout());
		return theContainer;
	}
	
	private IRectangularGraphicObject buildVariableLine(
			LocalVariableInfo aVariable, 
			Object aCurrentObject, 
			Object aValue)
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
