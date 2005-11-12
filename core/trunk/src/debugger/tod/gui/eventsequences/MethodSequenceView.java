/*
 * Created on Oct 18, 2005
 */
package tod.gui.eventsequences;

import java.awt.Color;

import reflex.lib.logging.miner.gui.IGUIManager;
import reflex.lib.logging.miner.gui.seed.CFlowSeed;
import reflex.lib.logging.miner.gui.view.LogView;
import tod.core.model.event.IAfterMethodCallEvent;
import tod.core.model.event.IBeforeMethodCallEvent;
import tod.core.model.event.IFieldWriteEvent;
import tod.core.model.event.ILogEvent;
import tod.core.model.structure.BehaviorInfo;
import tod.core.model.structure.FieldInfo;
import tod.core.model.structure.MemberInfo;
import tod.core.model.trace.IEventTrace;
import tod.core.model.trace.IEventBrowser;
import tod.core.model.trace.IObjectInspector;
import tod.gui.ObjectInspectorView;
import tod.gui.SVGHyperlink;
import zz.csg.api.IDisplay;
import zz.csg.api.IRectangularGraphicObject;
import zz.csg.api.layout.SequenceLayout;
import zz.csg.impl.SVGGraphicContainer;
import zz.csg.impl.figures.SVGFlowText;

public class MethodSequenceView extends AbstractMemberSequenceView
{
	public static final Color METHOD_COLOR = Color.GREEN;
	
	private BehaviorInfo itsMethod;

	
	public MethodSequenceView(IDisplay aDisplay, LogView aLogView, IObjectInspector aInspector, BehaviorInfo aMethod)
	{
		super(aDisplay, aLogView, aInspector, METHOD_COLOR);
		itsMethod = aMethod;
	}

	public String getTitle()
	{
		return "Method " + itsMethod.getName();
	}

	@Override
	protected IRectangularGraphicObject getBaloon(ILogEvent aEvent)
	{
		if (aEvent instanceof IBeforeMethodCallEvent)
		{
			IBeforeMethodCallEvent theEvent = (IBeforeMethodCallEvent) aEvent;
			return createBeforeCallBaloon(theEvent);
		}
		else if (aEvent instanceof IAfterMethodCallEvent)
		{
			IAfterMethodCallEvent theEvent = (IAfterMethodCallEvent) aEvent;
			return createAfterCallBaloon(theEvent);
		}
		else return null;
	}
	
	private IRectangularGraphicObject createBeforeCallBaloon (IBeforeMethodCallEvent aEvent)
	{
		SVGGraphicContainer theContainer = new SVGGraphicContainer();
		theContainer.setLayoutManager(new SequenceLayout());

		// Create hyperlink to call event
		IGUIManager theGUIManager = getLogView().getGUIManager();
		IEventTrace theLog = getLogView().getEventTrace();

		CFlowSeed theSeed = new CFlowSeed(theGUIManager, theLog, aEvent);
		SVGHyperlink theHyperlink = SVGHyperlink.create(theGUIManager, theSeed, "call", 10, Color.BLACK);
		theContainer.pChildren().add (theHyperlink);
		
		// Open parenthesis
		theContainer.pChildren().add (SVGFlowText.create("(", 10, Color.BLACK));
		
		// Create links of individual arguments
		Object[] theArguments = aEvent.getArguments();
		boolean theFirst = true;
		for (Object theArgument : theArguments)
		{
			if (theFirst) theFirst = false;
			else
			{
				theContainer.pChildren().add (SVGFlowText.create(", ", 10, Color.BLACK));						
			}
			
			theContainer.pChildren().add(createBaloon(theArgument));
		}
		
		// CLose parenthesis
		theContainer.pChildren().add (SVGFlowText.create(")", 10, Color.BLACK));
		
		return theContainer;
		
	}

	private IRectangularGraphicObject createAfterCallBaloon (IAfterMethodCallEvent aEvent)
	{
		SVGGraphicContainer theContainer = new SVGGraphicContainer();
		theContainer.setLayoutManager(new SequenceLayout());
		
		theContainer.pChildren().add (SVGFlowText.create("return: ", 10, Color.BLACK));
		theContainer.pChildren().add (createBaloon(aEvent.getReturnValue()));
		
		return theContainer;
		
	}
	
	@Override
	public MemberInfo getMember()
	{
		return itsMethod;
	}
}
