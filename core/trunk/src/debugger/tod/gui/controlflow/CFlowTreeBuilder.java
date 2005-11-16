/*
 * Created on Nov 2, 2005
 */
package tod.gui.controlflow;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import reflex.lib.logging.miner.gui.formatter.EventFormatter;
import tod.core.model.event.EventUtils;
import tod.core.model.event.IConstructorChainingEvent;
import tod.core.model.event.IExceptionGeneratedEvent;
import tod.core.model.event.IFieldWriteEvent;
import tod.core.model.event.IInstantiationEvent;
import tod.core.model.event.ILocalVariableWriteEvent;
import tod.core.model.event.ILogEvent;
import tod.core.model.event.IMethodCallEvent;
import tod.core.model.event.IParentEvent;
import zz.csg.api.IRectangularGraphicObject;
import zz.csg.impl.figures.SVGFlowText;
import zz.utils.ui.text.XFont;

/**
 * Permits to build the nodes that represent events in a CFlow tree.
 * @author gpothier
 */
public class CFlowTreeBuilder
{
	public static final XFont FONT = XFont.DEFAULT_XPLAIN.deriveFont(12);
	public static final XFont HEADER_FONT = XFont.DEFAULT_XPLAIN.deriveFont(Font.BOLD, 14);
	
	private CFlowView itsView;
	
	public CFlowTreeBuilder(CFlowView aView)
	{
		itsView = aView;
	}

	public IRectangularGraphicObject buildRootNode (IParentEvent aRootEvent)
	{
		return new RootEventNode(itsView, (IParentEvent) aRootEvent);		
	}
	
	public List<IRectangularGraphicObject> buildNodes (IParentEvent aContainer)
	{
		List<IRectangularGraphicObject> theNodes = new ArrayList<IRectangularGraphicObject>();
		
		List<ILogEvent> theChildren = aContainer.getChildren();
		if (theChildren == null || theChildren.size() == 0) return theNodes;
		
		for (ILogEvent theEvent : theChildren)
		{
			IRectangularGraphicObject theNode = buildNode(theEvent);
			if (theNode != null) 
			{
				theNodes.add(theNode);
				theNode.checkValid();
			}
		}		
		
		return theNodes;
	}

	private IRectangularGraphicObject buildNode(ILogEvent aEvent)
	{
		if (aEvent instanceof IFieldWriteEvent)
		{
			IFieldWriteEvent theEvent = (IFieldWriteEvent) aEvent;
			return new FieldWriteNode(itsView, theEvent);
		}
		else if (aEvent instanceof ILocalVariableWriteEvent)
		{
			ILocalVariableWriteEvent theEvent = (ILocalVariableWriteEvent) aEvent;
			return new LocalVariableWriteNode(itsView, theEvent);
		}
		else if (aEvent instanceof IExceptionGeneratedEvent)
		{
			IExceptionGeneratedEvent theEvent = (IExceptionGeneratedEvent) aEvent;
			if (EventUtils.isIgnorableException(theEvent)) return null;
			else return new ExceptionGeneratedNode(itsView, theEvent);
		}
		else if (aEvent instanceof IMethodCallEvent)
		{
			IMethodCallEvent theEvent = (IMethodCallEvent) aEvent;
			return new MethodCallNode(itsView, theEvent);
		}
		else if (aEvent instanceof IInstantiationEvent)
		{
			IInstantiationEvent theEvent = (IInstantiationEvent) aEvent;
			return new InstantiationNode(itsView, theEvent);
		}
		else if (aEvent instanceof IConstructorChainingEvent)
		{
			IConstructorChainingEvent theEvent = (IConstructorChainingEvent) aEvent;
			// TODO: implement
		}

		String theText = "Not handled: "+EventFormatter.getInstance().getPlainText(aEvent);
		return SVGFlowText.create(theText, FONT, Color.RED);
	}

}
