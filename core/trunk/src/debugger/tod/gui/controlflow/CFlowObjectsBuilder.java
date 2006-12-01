/*
 * Created on Nov 4, 2005
 */
package tod.gui.controlflow;

import java.awt.Color;
import java.awt.Font;
import java.util.List;

import tod.core.database.browser.ILogBrowser;
import tod.core.database.browser.IObjectInspector;
import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.IFieldWriteEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IFieldInfo;
import tod.core.database.structure.ObjectId;
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
 * Builds a snapshot view of objects on the call stack for the CFlow view. 
 * @author gpothier
 */
public class CFlowObjectsBuilder
{
	public static final XFont FONT = XFont.DEFAULT_XPLAIN.deriveFont(FontConfig.FONT_SIZE);
	public static final XFont HEADER_FONT = XFont.DEFAULT_XPLAIN.deriveFont(Font.BOLD, FontConfig.HEADER_FONT_SIZE);

	private CFlowView itsView;
	
	public CFlowObjectsBuilder(CFlowView aView)
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
		ObjectId theCurrentObject = (ObjectId) theParent.getTarget();
		
		IObjectInspector theInspector = theCurrentObject != null ?
				getEventTrace().createObjectInspector(theCurrentObject)
				: getEventTrace().createClassInspector(theParent.getExecutedBehavior().getType());
		
		theInspector.setCurrentEvent(aCurrentEvent);

		// Determine available fields
		List<IFieldInfo> theFields = theInspector.getFields();
		
		// Create container
		SVGGraphicContainer theContainer = new SVGGraphicContainer();
		
		theContainer.pChildren().add(buildHeader(theCurrentObject));

		for (IFieldInfo theField : theFields)
		{
			List<IFieldWriteEvent> theSetters = theInspector.getFieldSetter(theField);
			theContainer.pChildren().add(buildFieldLine(theField, theCurrentObject, theSetters));
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
			IFieldInfo aField, 
			Object aCurrentObject, 
			List<IFieldWriteEvent> aSetters)
	{
		SVGGraphicContainer theContainer = new SVGGraphicContainer();
		
		String theFieldName = aField.getName();
		String theTypeName = aField.getClass().getName();
		String theText = /*theTypeName + " " + */theFieldName + " = ";
		
		theContainer.pChildren().add(SVGFlowText.create(theText, FONT, Color.BLACK));
		
		boolean theFirst = true;
		for (IFieldWriteEvent theSetter : aSetters)
		{
			if (theFirst) theFirst = false;
			else theContainer.pChildren().add(SVGFlowText.create(" / ", FONT, Color.BLACK));
			theContainer.pChildren().add(Hyperlinks.object(
					getGUIManager(), 
					getEventTrace(), 
					aCurrentObject,
					theSetter.getValue(),
					FONT));
			
			theContainer.pChildren().add(SVGFlowText.create(" (", FONT, Color.BLACK));
			theContainer.pChildren().add(Hyperlinks.event(
					getGUIManager(),
					getEventTrace(),
					"why?", 
					theSetter, 
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
