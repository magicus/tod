/*
 * Created on Nov 4, 2005
 */
package tod.gui.controlflow;

import java.awt.Color;
import java.awt.Font;
import java.util.List;

import reflex.lib.logging.miner.gui.IGUIManager;
import tod.core.model.event.IBehaviorCallEvent;
import tod.core.model.event.ILogEvent;
import tod.core.model.structure.FieldInfo;
import tod.core.model.structure.ObjectId;
import tod.core.model.trace.IEventTrace;
import tod.core.model.trace.IObjectInspector;
import tod.gui.Hyperlinks;
import zz.csg.api.IRectangularGraphicObject;
import zz.csg.api.layout.SequenceLayout;
import zz.csg.api.layout.StackLayout;
import zz.csg.impl.SVGGraphicContainer;
import zz.csg.impl.figures.SVGFlowText;
import zz.csg.impl.figures.SVGRectangle;
import zz.utils.ui.text.XFont;

/**
 * Builds a snapshot view of objects on the call stack for the CFlow view. 
 * @author gpothier
 */
public class CFlowObjectsBuilder
{
	public static final XFont FONT = XFont.DEFAULT_XPLAIN.deriveFont(12);
	public static final XFont HEADER_FONT = XFont.DEFAULT_XPLAIN.deriveFont(Font.BOLD, 14);

	private CFlowView itsView;
	
	public CFlowObjectsBuilder(CFlowView aView)
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
		ObjectId theCurrentObject = (ObjectId) theParent.getTarget();
		
		IObjectInspector theInspector = theCurrentObject != null ?
				getEventTrace().createObjectInspector(theCurrentObject)
				: getEventTrace().createClassInspector(theParent.getExecutedBehavior().getType());
		
		theInspector.setCurrentEvent(aCurrentEvent);

		// Determine available fields
		List<FieldInfo> theFields = theInspector.getFields();
		
		// Create container
		SVGGraphicContainer theContainer = new SVGGraphicContainer();
		
		theContainer.pChildren().add(buildHeader(theCurrentObject));

		for (FieldInfo theField : theFields)
		{
			List<Object> theValues = theInspector.getFieldValue(theField);
			theContainer.pChildren().add(buildFieldLine(theField, theCurrentObject, theValues));
		}

		theContainer.setLayoutManager(new StackLayout());
		return theContainer;
	}
	
	private IRectangularGraphicObject buildHeader(ObjectId aCurrentObject)
	{
		SVGGraphicContainer theContainer = new SVGGraphicContainer();
		
		theContainer.pChildren().add(SVGFlowText.create("Object: ", HEADER_FONT, Color.BLACK));
		theContainer.pChildren().add(Hyperlinks.object(getGUIManager(), getEventTrace(), aCurrentObject, HEADER_FONT));
		
		theContainer.setLayoutManager(new SequenceLayout());
		return theContainer;
	}
	
	private IRectangularGraphicObject buildFieldLine(
			FieldInfo aField, 
			Object aCurrentObject, 
			List<Object> aValues)
	{
		SVGGraphicContainer theContainer = new SVGGraphicContainer();
		
		String theFieldName = aField.getName();
		String theTypeName = aField.getClass().getName();
		String theText = /*theTypeName + " " + */theFieldName + " = ";
		
		theContainer.pChildren().add(SVGFlowText.create(theText, FONT, Color.BLACK));
		
		boolean theFirst = true;
		for (Object theValue : aValues)
		{
			if (theFirst) theFirst = false;
			else theContainer.pChildren().add(SVGFlowText.create(" / ", FONT, Color.BLACK));
			theContainer.pChildren().add(Hyperlinks.object(
					getGUIManager(), 
					getEventTrace(), 
					aCurrentObject,
					theValue,
					FONT));
			
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
