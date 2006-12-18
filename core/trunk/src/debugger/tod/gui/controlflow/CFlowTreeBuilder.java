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
package tod.gui.controlflow;

import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import tod.core.database.event.EventUtils;
import tod.core.database.event.IArrayWriteEvent;
import tod.core.database.event.IBehaviorExitEvent;
import tod.core.database.event.IConstructorChainingEvent;
import tod.core.database.event.IExceptionGeneratedEvent;
import tod.core.database.event.IFieldWriteEvent;
import tod.core.database.event.IInstantiationEvent;
import tod.core.database.event.ILocalVariableWriteEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.event.IMethodCallEvent;
import tod.core.database.event.IParentEvent;
import tod.gui.FontConfig;
import zz.utils.ui.text.XFont;

/**
 * Permits to build the nodes that represent events in a CFlow tree.
 * @author gpothier
 */
public class CFlowTreeBuilder
{
	public static final XFont FONT = XFont.DEFAULT_XPLAIN.deriveFont(FontConfig.FONT_SIZE);
	public static final XFont HEADER_FONT = XFont.DEFAULT_XPLAIN.deriveFont(Font.BOLD, FontConfig.HEADER_FONT_SIZE);

	private CFlowView itsView;
	
	public CFlowTreeBuilder(CFlowView aView)
	{
		itsView = aView;
	}

	public AbstractEventNode buildRootNode (IParentEvent aRootEvent)
	{
		return new RootEventNode(itsView, aRootEvent);		
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
			
			if (theNodes.size() > 1000) break;
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
		else if (aEvent instanceof IArrayWriteEvent)
		{
			IArrayWriteEvent theEvent = (IArrayWriteEvent) aEvent;
			return new ArrayWriteNode(itsView, theEvent);
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
		else if (aEvent instanceof IBehaviorExitEvent)
		{
			return null;
		}

		return new UnknownEventNode(itsView, aEvent);
	}

}
