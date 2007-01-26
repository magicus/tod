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
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.IBehaviorExitEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.gui.FontConfig;
import tod.gui.Hyperlinks;
import tod.gui.controlflow.CFlowView;
import tod.gui.controlflow.CFlowViewUtils;
import zz.csg.api.GraphicObjectContext;
import zz.csg.api.IGraphicContainer;
import zz.csg.api.IRectangularGraphicContainer;
import zz.csg.api.IRectangularGraphicObject;
import zz.csg.api.layout.AbstractSimpleLayout;
import zz.csg.api.layout.SequenceLayout;
import zz.csg.impl.SVGGraphicContainer;
import zz.csg.impl.figures.SVGFlowText;
import zz.utils.ui.UIUtils;
import zz.utils.ui.text.XFont;

public abstract class BehaviorCallNode extends AbstractEventNode
{
	private IBehaviorCallEvent itsEvent;
	
	private IRectangularGraphicObject itsHeader;
	private IRectangularGraphicObject itsFooter;
	private ExpanderWidget itsExpanderWidget;
	
	public BehaviorCallNode(
			CFlowView aView,
			IBehaviorCallEvent aEvent)
	{
		super (aView);
		
		itsEvent = aEvent;
		createUI();
	}

	private void createUI()
	{
		IBehaviorExitEvent theExitEvent = getEvent().getExitEvent();
		
		Color theExpanderColor;
		if (theExitEvent == null) theExpanderColor = Color.BLACK;
		else theExpanderColor = theExitEvent.hasThrown() ? Color.RED : Color.BLUE;
		
		if (!getEvent().hasRealChildren()) 
			theExpanderColor = UIUtils.getLighterColor(theExpanderColor, 0.2f);
		
		itsExpanderWidget = new ExpanderWidget(theExpanderColor)
		{
			@Override
			public boolean mousePressed(GraphicObjectContext aContext, MouseEvent aEvent, Point2D aPoint)
			{
				getView().getSeed().pParentEvent().set(getEvent());
				return true;
			}
		};
		
		pChildren().add(itsExpanderWidget);
		
		itsHeader = createHeader(FontConfig.STD_FONT);
		itsFooter = createFooter(FontConfig.STD_FONT);
		
		pChildren().add(itsHeader);
		pChildren().add(itsFooter);
		
		setLayoutManager(new MyLayout());
	}
	
	@Override
	protected IBehaviorCallEvent getEvent()
	{
		return itsEvent;
	}
	
	protected IRectangularGraphicObject createHeader(XFont aFont)
	{
		SVGGraphicContainer theContainer = new SVGGraphicContainer();
		
		fillHeaderPrefix(theContainer, aFont);
		Object[] theArguments = getEvent().getArguments();
		CFlowViewUtils.addArguments(getSeedFactory(), getLogBrowser(), theContainer, theArguments, aFont);

		theContainer.setLayoutManager(new SequenceLayout());
		return theContainer;
	}

	
	protected IRectangularGraphicObject createFooter(XFont aFont)
	{
		SVGGraphicContainer theContainer = new SVGGraphicContainer();

		IBehaviorInfo theBehavior = getEvent().getExecutedBehavior();
		if (theBehavior == null) theBehavior = getEvent().getCalledBehavior();
		
		IBehaviorExitEvent theExitEvent = getEvent().getExitEvent();
		Object theResult = theExitEvent != null ? theExitEvent.getResult() : null;
		
		if (theExitEvent == null)
		{
			theContainer.pChildren().add(SVGFlowText.create("Behavior never returned", aFont, Color.BLACK));
		}
		else if (theExitEvent.hasThrown())
		{
			theContainer.pChildren().add(SVGFlowText.create("Thrown ", aFont, Color.RED));

			theContainer.pChildren().add(Hyperlinks.object(
					getSeedFactory(), 
					getLogBrowser(), 
					theExitEvent.getResult(), 
					aFont));
		}
		else
		{
			fillFooterPrefix(theContainer, aFont);

			if (theResult != null)
			{
				theContainer.pChildren().add(Hyperlinks.object(
						getSeedFactory(), 
						getLogBrowser(), 
						theExitEvent.getResult(), 
						aFont));
			}
			else if (theBehavior.getReturnType().isVoid())
			{
				theContainer.pChildren().add(SVGFlowText.create("void", aFont, Color.BLACK));
			}
			else 
			{
				theContainer.pChildren().add(SVGFlowText.create("internal error", aFont, Color.RED));
			}
		}
		
		theContainer.setLayoutManager(new SequenceLayout());
		return theContainer;
	}
	
	/**
	 * Adds the prefix to the header. Eg.: "new MyClass" or "MyClass.myMethod" 
	 */
	protected abstract void fillHeaderPrefix(
			IRectangularGraphicContainer aContainer,
			XFont aFont);
	
	/**
	 * Adds the prefix to the footer. Eg.: "Created " or "Returned "
	 */
	protected abstract void fillFooterPrefix(
			IRectangularGraphicContainer aContainer,
			XFont aFont);
	
	
	private class MyLayout extends AbstractSimpleLayout
	{

		@Override
		protected void layout(IGraphicContainer aContainer)
		{
			assert aContainer == BehaviorCallNode.this;
			
			double theY = 0;

			if (itsHeader != null)
			{
				itsHeader.pBounds().setPosition(ExpanderWidget.WIDTH, theY);
				theY += itsHeader.pBounds().get().getHeight();
			}				
			
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
}
