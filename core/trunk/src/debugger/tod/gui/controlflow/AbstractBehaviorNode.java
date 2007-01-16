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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.IBehaviorExitEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.ITypeInfo;
import tod.gui.Hyperlinks;
import zz.csg.api.GraphicObjectContext;
import zz.csg.api.IDisplay;
import zz.csg.api.IGraphicContainer;
import zz.csg.api.IRectangularGraphicContainer;
import zz.csg.api.layout.AbstractSimpleLayout;
import zz.csg.api.layout.SequenceLayout;
import zz.csg.api.layout.StackLayout;
import zz.csg.impl.AbstractRectangularGraphicObject;
import zz.csg.impl.SVGGraphicContainer;
import zz.csg.impl.figures.SVGFlowText;
import zz.utils.properties.IRWProperty;
import zz.utils.properties.PropertyUtils;
import zz.utils.properties.SimpleRWProperty;
import zz.utils.ui.text.XFont;

public abstract class AbstractBehaviorNode extends AbstractEventNode
{
	private IBehaviorCallEvent itsEvent;
	
	private IRectangularGraphicContainer itsChildrenContainer;
	private IRectangularGraphicContainer itsHeader;
	private IRectangularGraphicContainer itsFooter;
	private ExpanderWidget itsExpanderWidget;
	
	private Map<ILogEvent, AbstractEventNode> itsNodesMap = 
		new HashMap<ILogEvent, AbstractEventNode>();
	
	
	private IRWProperty<Boolean> pExpanded = new SimpleRWProperty<Boolean>(this, false)
	{
		@Override
		protected void changed(Boolean aOldValue, Boolean aNewValue)
		{
			if (aNewValue) doExpand();
			else doCollapse();
		}
	};
	
	public AbstractBehaviorNode(
			CFlowView aView,
			IBehaviorCallEvent aEvent)
	{
		super (aView);
		
		itsEvent = aEvent;
		
		itsExpanderWidget = new ExpanderWidget(
				getEvent().getExitEvent().hasThrown() ? Color.RED : Color.BLUE,
				4,
				getEvent().hasRealChildren());
		
		pChildren().add(itsExpanderWidget);
		
		PropertyUtils.connect(pExpanded(), itsExpanderWidget.pExpanded(), true);
		
		itsChildrenContainer = new SVGGraphicContainer();
		itsChildrenContainer.setLayoutManager(new StackLayout());
		pChildren().add(itsChildrenContainer);
		
		itsHeader = buildHeader();
		pChildren().add(itsHeader);
		
		itsFooter = buildFooter();
		pChildren().add(itsFooter);
		
		setLayoutManager(new MyLayout());

	}
	
	@Override
	protected IBehaviorCallEvent getEvent()
	{
		return itsEvent;
	}
	
//	@Override
//	protected boolean validate()
//	{
//		if (itsHeader != null) pChildren().remove(itsHeader);
//		if (itsFooter != null) pChildren().remove(itsFooter);
//		
//		itsHeader = buildHeader();
//		pChildren().add(itsHeader);
//		
//		itsFooter = buildFooter();
//		pChildren().add(itsFooter);
//		
//		setLayoutManager(new MyLayout());
//		
//		super.validate();
//		return true;
//	}
	
	@Override
	public void invalidate()
	{
		super.invalidate();
		itsChildrenContainer.invalidate();
	}
	
	@Override
	public AbstractEventNode getNode(ILogEvent aEvent)
	{
		AbstractEventNode theNode = super.getNode(aEvent);
		if (theNode != null) return theNode;
		else return itsNodesMap.get(aEvent);
	}

	/**
	 * Whether this node is expanded or collapsed.
	 */
	public IRWProperty<Boolean> pExpanded()
	{
		return pExpanded;
	}
	
	public void expand()
	{
		pExpanded().set(true);
	}
	
	private void doExpand()
	{
		invalidate();
		List<AbstractEventNode> theNodes = getBuilder().buildNodes(getEvent());
		
		itsNodesMap.clear();
		
		if (!itsEvent.isDirectParent()) 
		{
			itsChildrenContainer.pChildren().add(new IndirectDotsWidget(true, 4));
		}
		
		for (AbstractEventNode theNode : theNodes)
		{
			itsChildrenContainer.pChildren().add(theNode);
			itsNodesMap.put(theNode.getEvent(), theNode);
		}

		if (!itsEvent.isDirectParent()) 
		{
			itsChildrenContainer.pChildren().add(new IndirectDotsWidget(false, 4));
		}

		repaintAllContexts();
	}
	
	public void collapse()
	{
		pExpanded().set(false);
	}
	
	private void doCollapse()
	{
		invalidate();
		itsChildrenContainer.pChildren().clear();		
		itsNodesMap.clear();
		
		repaintAllContexts();
	}
	
	protected IRectangularGraphicContainer buildHeader()
	{
		IRectangularGraphicContainer theHeader = new SVGGraphicContainer();
		theHeader.setLayoutManager(new SequenceLayout());

		fillHeader(theHeader);
		
		return theHeader;
	}
	
	protected IRectangularGraphicContainer buildFooter()
	{
		IRectangularGraphicContainer theFooter = new SVGGraphicContainer();
		theFooter.setLayoutManager(new SequenceLayout());

		fillFooter(theFooter);
		
		return theFooter;
	}

	
	protected void fillHeader(IRectangularGraphicContainer aContainer)
	{
		XFont theFont = getHeaderFont();
		
		IBehaviorInfo theBehavior = getEvent().getExecutedBehavior();
		if (theBehavior == null)
		{
			theFont = theFont.deriveFont(Font.ITALIC, theFont.getAWTFont().getSize2D());
			theBehavior = getEvent().getCalledBehavior();
		}
		ITypeInfo theType = theBehavior.getType();
		Object[] theArguments = getEvent().getArguments();
		
		aContainer.pChildren().add(Hyperlinks.type(getGUIManager(), theType, theFont));
		aContainer.pChildren().add(SVGFlowText.create(".", theFont, Color.BLACK));
		aContainer.pChildren().add(Hyperlinks.behavior(getGUIManager(), theBehavior, theFont));

		addArguments(aContainer, theArguments, theFont);
	}
	
	protected void fillFooter(IRectangularGraphicContainer aContainer)
	{
		XFont theFont = getHeaderFont();

		IBehaviorInfo theBehavior = getEvent().getExecutedBehavior();
		if (theBehavior == null) theBehavior = getEvent().getCalledBehavior();
		
		IBehaviorExitEvent theExitEvent = getEvent().getExitEvent();
		if (theExitEvent.hasThrown())
		{
			aContainer.pChildren().add(SVGFlowText.create("Thrown ", theFont, Color.RED));

			aContainer.pChildren().add(Hyperlinks.object(
					getGUIManager(), 
					getEventTrace(), 
					theExitEvent.getResult(), 
					theFont));
		}
		else if (theBehavior.getReturnType().isVoid())
		{
			aContainer.pChildren().add(SVGFlowText.create("Returned", theFont, Color.BLACK));
		}
		else 
		{
			aContainer.pChildren().add(SVGFlowText.create("Returned ", theFont, Color.BLACK));
			
			aContainer.pChildren().add(Hyperlinks.object(
					getGUIManager(), 
					getEventTrace(), 
					theExitEvent.getResult(), 
					theFont));
		}
	}
	
	/**
	 * Returns the font size that should be used for headers.
	 * The font is bigger when the node is expanded.
	 */
	protected XFont getHeaderFont()
	{
//		return pExpanded().get() ? CFlowTreeBuilder.HEADER_FONT : CFlowTreeBuilder.FONT;
		return CFlowTreeBuilder.FONT;
	}
	
	/**
	 * Adds the hyperlinks representing the behavior's arguments to the given container.
	 * Can be used for the implementation of {@link #buildHeader()}
	 */
	protected void addArguments(IGraphicContainer aContainer, Object[] aArguments, XFont aFont)
	{
		aContainer.pChildren().add(SVGFlowText.create("(", aFont, Color.BLACK));
		
		if (aArguments != null)
		{
			boolean theFirst = true;
			for (Object theArgument : aArguments)
			{
				if (theFirst) theFirst = false;
				else aContainer.pChildren().add(SVGFlowText.create(", ", aFont, Color.BLACK));
				
				aContainer.pChildren().add(Hyperlinks.object(
						getGUIManager(),
						getEventTrace(),
						theArgument, 
						aFont));
			}
		}
		else
		{
			aContainer.pChildren().add(SVGFlowText.create("...", aFont, Color.BLACK));
		}
		
		aContainer.pChildren().add(SVGFlowText.create(")", aFont, Color.BLACK));
	}
	
	private class MyLayout extends AbstractSimpleLayout
	{

		@Override
		protected void layout(IGraphicContainer aContainer)
		{
			assert aContainer == AbstractBehaviorNode.this;
			
			double theY = 0;

			if (itsHeader != null)
			{
				itsHeader.pBounds().setPosition(ExpanderWidget.WIDTH, theY);
				theY += itsHeader.pBounds().get().getHeight();
			}				
			
			itsChildrenContainer.pBounds().setPosition(
					ExpanderWidget.WIDTH + 10, 
					theY);
			
			theY += itsChildrenContainer.pBounds().get().getHeight();
			
			if (itsFooter != null)
			{
				itsFooter.pBounds().setPosition(ExpanderWidget.WIDTH, theY);
				theY += itsFooter.pBounds().get().getHeight();
			}
			
			itsExpanderWidget.pBounds().set(0, 0, ExpanderWidget.WIDTH, theY);
			
			Rectangle2D theBounds = computeBounds(null);
			resize(theBounds.getWidth(), theBounds.getHeight());
		}
		
	}
	
	/**
	 * This widget shows a few dots in a diagonal to indicate
	 * that the event is not a direct parent.
	 * @author gpothier
	 */
	private static class IndirectDotsWidget extends AbstractRectangularGraphicObject
	{
		public static final double SIZE = 20;
		public static final double NDOTS = 3;
		
		private boolean itsEnter;
		private double itsThickness;
		
		public IndirectDotsWidget(boolean aEnter, double aThickness)
		{
			itsEnter = aEnter;
			itsThickness = aThickness;
			pBounds().set(0, 0, SIZE, SIZE);
		}

		
		public void paint(IDisplay aDisplay, GraphicObjectContext aContext, Graphics2D aGraphics, Area aVisibleArea)
		{
			Rectangle2D theBounds = pBounds().get();
			
			double theX = theBounds.getX();
			if (! itsEnter) theX += theBounds.getWidth()-itsThickness;
			double theY = theBounds.getY();
			
			double theDX = (theBounds.getWidth()-itsThickness)/(NDOTS-1);
			double theDY = (theBounds.getHeight()-itsThickness)/(NDOTS-1);

			aGraphics.setColor(Color.BLACK);
			for (int i=0;i<NDOTS;i++)
			{
				aGraphics.fill(new Ellipse2D.Double(
						theX, 
						theY, 
						itsThickness, 
						itsThickness));
				
				theX = itsEnter ? theX + theDX : theX - theDX;
				theY += theDY;
			}
		}
	}

}
