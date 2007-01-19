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
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.Point2D;

import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.ITypeInfo;
import tod.gui.FontConfig;
import tod.gui.controlflow.CFlowView;
import tod.gui.seed.CFlowSeed;
import zz.csg.api.GraphicObjectContext;
import zz.csg.api.IDisplay;
import zz.csg.api.layout.StackLayout;
import zz.csg.impl.figures.SVGFlowText;
import zz.utils.ui.UIUtils;

/**
 * Represents a stack item in the control flow.
 * @author gpothier
 */
public class StackNode extends AbstractCFlowNode
{
	private IBehaviorCallEvent itsEvent;

	private boolean itsMouseOver = false;
	
	public StackNode(
			CFlowView aView,
			IBehaviorCallEvent aEvent)
	{
		super(aView);
		itsEvent = aEvent;
		createUI();
	}

	public IBehaviorCallEvent getEvent()
	{
		return itsEvent;
	}

	private void createUI()
	{
		StringBuilder theBuilder = new StringBuilder();

		// Create caption
		IBehaviorInfo theBehavior = getEvent().getExecutedBehavior();
		if (theBehavior == null) theBehavior = getEvent().getCalledBehavior();
		ITypeInfo theType = theBehavior.getType();
		Object[] theArguments = getEvent().getArguments();
		
		// Type.method
		theBuilder.append(theType.getName());
		theBuilder.append(".");
		theBuilder.append(theBehavior.getName());
		
		// Arguments
		theBuilder.append("(");
		
		if (theArguments != null)
		{
			boolean theFirst = true;
			for (Object theArgument : theArguments)
			{
				if (theFirst) theFirst = false;
				else theBuilder.append(", ");
				
				theBuilder.append(getView().getFormatter().formatObject(theArgument));
			}
		}
		else
		{
			theBuilder.append("...");
		}
		
		theBuilder.append(")");

		SVGFlowText theText = SVGFlowText.create(
				theBuilder.toString(), 
				FontConfig.SMALL_FONT, 
				Color.BLACK);
		
		pChildren().add(theText);
		setLayoutManager(new StackLayout());
	}
	
	@Override
	protected void paintBackground(
			IDisplay aDisplay, 
			GraphicObjectContext aContext, 
			Graphics2D aGraphics, 
			Area aVisibleArea)
	{
		Color theColor = Color.ORANGE;
		if (itsMouseOver) theColor = UIUtils.getLighterColor(theColor);
		aGraphics.setColor(theColor);
		aGraphics.fill(pBounds().get());
	}

	@Override
	public void mouseEntered(GraphicObjectContext aContext, MouseEvent aEvent)
	{
		itsMouseOver = true;
		repaintAllContexts();
	}

	@Override
	public void mouseExited(GraphicObjectContext aContext, MouseEvent aEvent)
	{
		itsMouseOver = false;
		repaintAllContexts();
	}

	@Override
	public boolean mousePressed(GraphicObjectContext aContext, MouseEvent aEvent, Point2D aPoint)
	{
		CFlowSeed theSeed = getView().getSeed();
		theSeed.pParentEvent().set(getEvent().getParent());
		theSeed.pSelectedEvent().set(getEvent());
		
		return true;
	}
	
	
	
}
