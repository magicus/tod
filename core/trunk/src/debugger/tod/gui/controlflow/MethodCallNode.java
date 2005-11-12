/*
 * Created on Nov 2, 2005
 */
package tod.gui.controlflow;

import java.awt.Color;

import tod.core.model.event.IAfterMethodCallEvent;
import tod.core.model.event.IBeforeMethodCallEvent;
import tod.core.model.event.IBehaviorEnterEvent;
import tod.core.model.structure.BehaviorInfo;
import tod.gui.Hyperlinks;
import zz.csg.api.IRectangularGraphicContainer;
import zz.csg.api.layout.SequenceLayout;
import zz.csg.impl.SVGGraphicContainer;
import zz.csg.impl.figures.SVGFlowText;
import zz.utils.ui.text.XFont;

public class MethodCallNode extends AbstractBehaviorNode
{
	private BehaviorInfo itsBehavior;
	private Object[] itsArguments;
	private Object itsReturnValue;

	public MethodCallNode(
			CFlowView aView,
			IBeforeMethodCallEvent aBeforeMethodCallEvent,
			IBehaviorEnterEvent aBehaviorEnterEvent,
			IAfterMethodCallEvent aAfterMethodCallEvent)
	{
		super (aView, aBehaviorEnterEvent, aBeforeMethodCallEvent);
		itsBehavior = aBehaviorEnterEvent.getBehavior();
		itsArguments = aBeforeMethodCallEvent.getArguments();
		itsReturnValue = aAfterMethodCallEvent.getReturnValue();
	}

	public MethodCallNode(
			CFlowView aView,
			IBehaviorEnterEvent aBehaviorEnterEvent)
	{
		super (aView, aBehaviorEnterEvent, aBehaviorEnterEvent);
		itsBehavior = aBehaviorEnterEvent.getBehavior();
	}
	
	public MethodCallNode(
			CFlowView aView,
			IBeforeMethodCallEvent aBeforeMethodCallEvent,
			IAfterMethodCallEvent aAfterMethodCallEvent)
	{
		super (aView, null, aBeforeMethodCallEvent);
		itsBehavior = aBeforeMethodCallEvent.getBehavior();
		itsArguments = aBeforeMethodCallEvent.getArguments();
		itsReturnValue = aAfterMethodCallEvent.getReturnValue();
	}
	
	protected IRectangularGraphicContainer buildHeader()
	{
		XFont theFont = getHeaderFont();
		
		IRectangularGraphicContainer theHeader = new SVGGraphicContainer();
		theHeader.setLayoutManager(new SequenceLayout());
		
		theHeader.pChildren().add(SVGFlowText.create("Entering ", theFont, Color.BLACK));
		theHeader.pChildren().add(Hyperlinks.type(getGUIManager(), itsBehavior.getType(), theFont));
		theHeader.pChildren().add(SVGFlowText.create(".", theFont, Color.BLACK));
		theHeader.pChildren().add(Hyperlinks.behavior(getGUIManager(), itsBehavior, theFont));

		addArguments(theHeader, itsArguments, theFont);
		
		
		return theHeader;
	}
	
	protected IRectangularGraphicContainer buildFooter()
	{
		XFont theFont = getHeaderFont();
		
		IRectangularGraphicContainer theFooter = new SVGGraphicContainer();
		theFooter.setLayoutManager(new SequenceLayout());

		if (itsReturnValue == null) 
		{
			theFooter.pChildren().add(SVGFlowText.create("Returned", theFont, Color.BLACK));			
		}
		else 
		{
			theFooter.pChildren().add(SVGFlowText.create("Returned ", theFont, Color.BLACK));
			theFooter.pChildren().add(Hyperlinks.object(
					getGUIManager(), 
					getEventTrace(), 
					itsReturnValue, 
					theFont));
		}
		
		return theFooter;
	}
}
