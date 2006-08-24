/*
 * Created on Nov 2, 2005
 */
package tod.gui.controlflow;

import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import tod.core.database.event.EventUtils;
import tod.core.database.event.IConstructorChainingEvent;
import tod.core.database.event.IExceptionGeneratedEvent;
import tod.core.database.event.IFieldWriteEvent;
import tod.core.database.event.IInstantiationEvent;
import tod.core.database.event.ILocalVariableWriteEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.event.IMethodCallEvent;
import tod.core.database.event.IParentEvent;
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

	public AbstractEventNode buildRootNode (IParentEvent aRootEvent)
	{
		return new RootEventNode(itsView, (IParentEvent) aRootEvent);		
	}
	
	public List<AbstractEventNode> buildNodes (IParentEvent aContainer)
	{
		List<AbstractEventNode> theNodes = new ArrayList<AbstractEventNode>();
		
		List<ILogEvent> theChildren = aContainer.getChildren();
		if (theChildren == null || theChildren.size() == 0) return theNodes;
		
		for (ILogEvent theEvent : theChildren)
		{
			AbstractEventNode theNode = buildNode(theEvent);
			if (theNode != null) 
			{
				theNodes.add(theNode);
				theNode.checkValid();
			}
		}		
		
		return theNodes;
	}

	private AbstractEventNode buildNode(ILogEvent aEvent)
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
			return new ConstructorChainingNode(itsView, theEvent);
		}

		return new UnknownEventNode(itsView, aEvent);
	}

}
