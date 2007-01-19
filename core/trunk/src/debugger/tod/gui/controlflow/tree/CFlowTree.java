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
package tod.gui.controlflow.tree;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import tod.core.database.event.EventUtils;
import tod.core.database.event.IArrayWriteEvent;
import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.IBehaviorExitEvent;
import tod.core.database.event.IConstructorChainingEvent;
import tod.core.database.event.IExceptionGeneratedEvent;
import tod.core.database.event.IFieldWriteEvent;
import tod.core.database.event.IInstantiationEvent;
import tod.core.database.event.ILocalVariableWriteEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.event.IMethodCallEvent;
import tod.core.database.event.IParentEvent;
import tod.gui.controlflow.CFlowView;
import tod.gui.eventlist.EventListCore;
import zz.csg.ZInsets;
import zz.csg.api.GraphicObjectContext;
import zz.csg.api.IDisplay;
import zz.csg.api.IGraphicContainer;
import zz.csg.api.IGraphicObject;
import zz.csg.api.IRectangularGraphicContainer;
import zz.csg.api.IRectangularGraphicObject;
import zz.csg.api.PickResult;
import zz.csg.api.layout.SequenceLayout;
import zz.csg.api.layout.StackLayout;
import zz.csg.impl.SVGGraphicContainer;
import zz.utils.properties.IProperty;
import zz.utils.properties.IPropertyListener;
import zz.utils.properties.PropertyListener;

public class CFlowTree extends SVGGraphicContainer
{
	private static final String METHOD_CALL_HEADER_PREFIX = "Call to ";
	private static final String METHOD_CALL_FOOTER_PREFIX = "Returned ";
	private static final String CC_HEADER_PREFIX = "Call to ";
	private static final String CC_FOOTER_PREFIX = "Returned ";
	private static final String INSTANTIATION_HEADER_PREFIX = "new ";
	private static final String INSTANTIATION_FOOTER_PREFIX = "Created ";
	
	private final CFlowView itsView;

	private EventListCore itsCore;
	private SVGGraphicContainer itsEventList;
	
	private long itsFirstDisplayedTimestamp;
	private long itsLastDisplayedTimestamp;
	
	public CFlowTree(CFlowView aView)
	{
		itsView = aView;
		
		IParentEvent theRoot = itsView.getSeed().pRootEvent().get();
		itsCore = new EventListCore(theRoot.getChildrenBrowser(), 10);
		createUI();
	}

	public void forward(int aCount)
	{
		itsCore.forward(aCount);
		updateList();
	}
	
	public void backward(int aCount)
	{
		itsCore.backward(aCount);
		updateList();
	}
	
	public void setTimestamp(long aTimestamp)
	{
		itsCore.setTimestamp(aTimestamp);
		updateList();
	}

	@Override
	public void attached(GraphicObjectContext aContext)
	{
		super.attached(aContext);
	}
	
	@Override
	public void detached(GraphicObjectContext aContext)
	{
		super.detached(aContext);
	}
	
	public void setParent(IParentEvent aEvent)
	{
		itsCore = new EventListCore(aEvent.getChildrenBrowser(), 10);
		update();
	}

	public void update()
	{
		pChildren().clear();
		createUI();
		checkValid();
		repaintAllContexts();
	}
	
	private void updateList()
	{
		itsEventList.pChildren().clear();
		fillEventList(itsEventList);
	}
	
	@Override
	public void paint(IDisplay aDisplay, GraphicObjectContext aContext, Graphics2D aGraphics, Area aVisibleArea)
	{
		super.paint(aDisplay, aContext, aGraphics, aVisibleArea);
	}
	
	private void createUI()
	{
		IRectangularGraphicObject theStack = buildStack();
		
		SVGGraphicContainer theContainer = new SVGGraphicContainer()
		{
			@Override
			protected void paintBackground(IDisplay aDisplay, GraphicObjectContext aContext, Graphics2D aGraphics, Area aVisibleArea)
			{
				aGraphics.setColor(Color.ORANGE);
				aGraphics.fill(pBounds().get());
			}
		};
		theContainer.setSize(
				theStack.getBounds(null).getHeight(), 
				getBounds(null).getHeight());
		theContainer.pChildren().add(theStack);
		
		AffineTransform theTransform = new AffineTransform();
		theTransform.translate(0, pBounds().get().getHeight());
		theTransform.rotate(-Math.PI/2);
		theStack.pTransform().set(theTransform);
		
		pChildren().add(theContainer);
		
		itsEventList = new SVGGraphicContainer();
		itsEventList.setLayoutManager(new EventListLayout());
		fillEventList(itsEventList);
		pChildren().add(itsEventList);
		
		SequenceLayout theLayout = new SequenceLayout()
		{
			@Override
			protected void resize(double aW, double aH)
			{
				super.resize(aW, pBounds().get().getHeight());
			}
		};
		setLayoutManager(theLayout);
	}
	
	private void fillEventList(IRectangularGraphicContainer aContainer)
	{
		LinkedList<ILogEvent> theEvents = itsCore.getDisplayedEvents();
		
		for (ILogEvent theEvent : theEvents)
		{
			AbstractEventNode theNode = buildEventNode(theEvent);
			if (theNode != null) aContainer.pChildren().add(theNode);
		}
	}
	
	public boolean isVisible(ILogEvent aEvent)
	{
		long theTimestamp = aEvent.getTimestamp();
		return theTimestamp >= itsFirstDisplayedTimestamp 
				&& theTimestamp <= itsLastDisplayedTimestamp;
	}
	
	private IBehaviorCallEvent getCurrentParent()
	{
		return itsView.getSeed().pParentEvent().get();
	}
	
	@Override
	public boolean mouseWheelMoved(GraphicObjectContext aContext, MouseWheelEvent aEvent, Point2D aPoint)
	{
		int theRotation = aEvent.getWheelRotation();
		if (theRotation < 0) backward(1);
		else if (theRotation > 0) forward(1);
		
		return true;
	}

	/**
	 * We need to ensure that a click outside of a child node is captured
	 * by this object.
	 */
	@Override
	public PickResult pick(GraphicObjectContext aContext, Point2D aPoint)
	{
		PickResult theResult = super.pick(aContext, aPoint);
		if (theResult != null) return theResult;
		else if (isInside(aContext, aPoint)) return new PickResult(this, aContext);
		else return null;
	}

	
	/**
	 * Builds the stack of ancestor events.
	 */
	private IRectangularGraphicObject buildStack()
	{
		List<IBehaviorCallEvent> theAncestors = new ArrayList<IBehaviorCallEvent>();
		IBehaviorCallEvent theCurrentParent = getCurrentParent();
		while (theCurrentParent != null)
		{
			theAncestors.add(theCurrentParent);
			theCurrentParent = theCurrentParent.getParent();
		}
		
		SVGGraphicContainer theContainer = new SVGGraphicContainer()
		{
			@Override
			protected void paintBackground(IDisplay aDisplay, GraphicObjectContext aContext, Graphics2D aGraphics, Area aVisibleArea)
			{
				aGraphics.setColor(Color.PINK);
				aGraphics.fill(pBounds().get());
			}
		};
		if (theAncestors.size() > 0) for(int i=theAncestors.size()-1;i>=0;i--)
		{
			IBehaviorCallEvent theAncestor = theAncestors.get(i);
			theContainer.pChildren().add(buildStackNode(theAncestor));
		}
		theContainer.setLayoutManager(new StackLayout(2, ZInsets.EMPTY));
		
		return theContainer;
	}
	
	private AbstractEventNode buildEventNode(ILogEvent aEvent)
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
			return new BehaviorCallNode(
					itsView, 
					theEvent, 
					METHOD_CALL_HEADER_PREFIX,
					METHOD_CALL_FOOTER_PREFIX);
		}
		else if (aEvent instanceof IInstantiationEvent)
		{
			IInstantiationEvent theEvent = (IInstantiationEvent) aEvent;
			return new BehaviorCallNode(
					itsView, 
					theEvent, 
					INSTANTIATION_HEADER_PREFIX,
					INSTANTIATION_FOOTER_PREFIX);
		}
		else if (aEvent instanceof IConstructorChainingEvent)
		{
			IConstructorChainingEvent theEvent = (IConstructorChainingEvent) aEvent;
			return new BehaviorCallNode(
					itsView, 
					theEvent, 
					CC_HEADER_PREFIX,
					CC_FOOTER_PREFIX);
		}
		else if (aEvent instanceof IBehaviorExitEvent)
		{
			return null;
		}

		return new UnknownEventNode(itsView, aEvent);
	}


	
	private StackNode buildStackNode(IBehaviorCallEvent aEvent)
	{
		if (aEvent instanceof IMethodCallEvent)
		{
			return new StackNode(itsView, aEvent);
		}
		else if (aEvent instanceof IInstantiationEvent)
		{
			return new StackNode(itsView, aEvent);
		}
		else if (aEvent instanceof IConstructorChainingEvent)
		{
			return new StackNode(itsView, aEvent);
		}
		else throw new RuntimeException("Not handled: "+aEvent);
	}
	
	/**
	 * Special layout manager for the event list. It updates the 
	 * number of visible events.
	 * @author gpothier
	 */
	private class EventListLayout extends StackLayout
	{

		@Override
		protected void layout(IGraphicContainer aContainer)
		{
			super.layout(aContainer);
			
			double theHeight = pBounds().get().getHeight();

			// Find first & last displayed timestamps.
			long theFirst = 0;
			long theLast = 0;
			for (IGraphicObject theGraphicObject : aContainer.pChildren())
			{
				AbstractEventNode theNode = (AbstractEventNode) theGraphicObject;
				long theTimestamp = theNode.getEvent().getTimestamp();
				
				if (theFirst == 0) theFirst = theTimestamp;
				
				Rectangle2D theBounds = theNode.getBounds(null);
				theBounds = theNode.getTransformedBounds(theBounds);
				
				if (theBounds.getY() > theHeight) break;
				theLast = theTimestamp;
			}
			
			itsFirstDisplayedTimestamp = theFirst;
			itsLastDisplayedTimestamp = theLast;
		}
	}
}
