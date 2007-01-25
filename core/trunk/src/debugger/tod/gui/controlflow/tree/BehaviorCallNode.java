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
import tod.gui.FontConfig;
import tod.gui.controlflow.CFlowView;
import tod.gui.controlflow.CFlowViewUtils;
import zz.csg.api.GraphicObjectContext;
import zz.csg.api.IGraphicContainer;
import zz.csg.api.IRectangularGraphicObject;
import zz.csg.api.layout.AbstractSimpleLayout;
import zz.utils.ui.UIUtils;

public class BehaviorCallNode extends AbstractEventNode
{
	private IBehaviorCallEvent itsEvent;
	private String itsHeaderPrefix;
	private String itsFooterPrefix;
	
	private IRectangularGraphicObject itsHeader;
	private IRectangularGraphicObject itsFooter;
	private ExpanderWidget itsExpanderWidget;
	
	public BehaviorCallNode(
			CFlowView aView,
			IBehaviorCallEvent aEvent,
			String aHeaderPrefix, 
			String aFooterPrefix)
	{
		super (aView);
		
		itsEvent = aEvent;
		itsHeaderPrefix = aHeaderPrefix;
		itsFooterPrefix = aFooterPrefix;
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
		
		itsHeader = CFlowViewUtils.createBehaviorCallHeader(
				getSeedFactory(), 
				getLogBrowser(), 
				getEvent(), 
				itsHeaderPrefix,
				FontConfig.STD_FONT);
		
		itsFooter = CFlowViewUtils.createBehaviorCallFooter(
				getSeedFactory(), 
				getLogBrowser(), 
				getEvent(), 
				itsFooterPrefix,
				FontConfig.STD_FONT);
		
		pChildren().add(itsHeader);
		pChildren().add(itsFooter);
		
		setLayoutManager(new MyLayout());
	}
	
	@Override
	protected IBehaviorCallEvent getEvent()
	{
		return itsEvent;
	}
	
	
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
