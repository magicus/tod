/*
 * Created on Oct 18, 2005
 */
package tod.gui.eventsequences;

import java.awt.Color;

import tod.core.model.event.IBehaviorCallEvent;
import tod.core.model.event.ILogEvent;
import tod.core.model.structure.IBehaviorInfo;
import tod.core.model.structure.IMemberInfo;
import tod.core.model.trace.IEventTrace;
import tod.core.model.trace.IObjectInspector;
import tod.gui.IGUIManager;
import tod.gui.SVGHyperlink;
import tod.gui.seed.CFlowSeed;
import tod.gui.view.LogView;
import zz.csg.api.IDisplay;
import zz.csg.api.IRectangularGraphicObject;
import zz.csg.api.layout.SequenceLayout;
import zz.csg.impl.SVGGraphicContainer;
import zz.csg.impl.figures.SVGFlowText;

public class MethodSequenceView extends AbstractMemberSequenceView
{
	public static final Color METHOD_COLOR = Color.GREEN;
	
	private IBehaviorInfo itsMethod;

	
	public MethodSequenceView(IDisplay aDisplay, LogView aLogView, IObjectInspector aInspector, IBehaviorInfo aMethod)
	{
		super(aDisplay, aLogView, METHOD_COLOR, aInspector);
		itsMethod = aMethod;
	}

	public String getTitle()
	{
		return "Method " + itsMethod.getName();
	}

	@Override
	protected IRectangularGraphicObject getBaloon(ILogEvent aEvent)
	{
		if (aEvent instanceof IBehaviorCallEvent)
		{
			IBehaviorCallEvent theEvent = (IBehaviorCallEvent) aEvent;
			return createBehaviorCallBaloon(theEvent);
		}
		else return null;
	}
	
	private IRectangularGraphicObject createBehaviorCallBaloon (IBehaviorCallEvent aEvent)
	{
		SVGGraphicContainer theContainer = new SVGGraphicContainer();
		theContainer.setLayoutManager(new SequenceLayout());

		// Create hyperlink to call event
		IGUIManager theGUIManager = getLogView().getGUIManager();
		IEventTrace theLog = getLogView().getTrace();

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
		
		// Close parenthesis
		theContainer.pChildren().add (SVGFlowText.create(")", 10, Color.BLACK));

		// Return value
		theContainer.pChildren().add (SVGFlowText.create("return: ", 10, Color.BLACK));
		theContainer.pChildren().add (createBaloon(aEvent.getExitEvent().getResult()));
		
		return theContainer;
		
	}

	@Override
	public IMemberInfo getMember()
	{
		return itsMethod;
	}
}
