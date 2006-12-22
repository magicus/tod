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
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import zz.csg.api.GraphicObjectContext;
import zz.csg.impl.AbstractRectangularGraphicObject;
import zz.utils.properties.IRWProperty;
import zz.utils.properties.SimpleRWProperty;
import zz.utils.ui.UIUtils;

/**
 * A widget that permits to expand/collapse a section of a tree
 * @author gpothier
 */
public class ExpanderWidget extends AbstractRectangularGraphicObject
{
	public static final double WIDTH = 10;
	
	private final IRWProperty<Boolean> pExpanded = new SimpleRWProperty<Boolean>(this, false)
	{
		@Override
		protected void changed(Boolean aOldValue, Boolean aNewValue)
		{
			repaintAllContexts();
		}
	};
	
	private Color itsColor;
	private double itsThickness;

	private boolean itsEnabled;
	
	public ExpanderWidget(Color aColor, double aThickness, boolean aEnabled)
	{
		itsColor = aColor;
		itsThickness = aThickness;
		itsEnabled = aEnabled;
	}

	/**
	 * This property reflects the expanded/collapsed state of this widget.
	 */
	public IRWProperty<Boolean> pExpanded()
	{
		return pExpanded;
	}
	
	public void paint(GraphicObjectContext aContext, Graphics2D aGraphics, Area aVisibleArea)
	{
		Rectangle2D theBounds = pBounds().get();
		Color theColor = itsEnabled ? 
				itsColor 
				: UIUtils.getLighterColor(itsColor, 0.2f);
		
		aGraphics.setColor(theColor);
		aGraphics.fill(new Rectangle2D.Double(
				theBounds.getX() + theBounds.getWidth()/2 - itsThickness/2, 
				theBounds.getY() + 1,
				itsThickness,
				theBounds.getHeight() - 2));
	}
	
	@Override
	public boolean mousePressed(GraphicObjectContext aContext, MouseEvent aEvent, Point2D aPoint)
	{
		if (itsEnabled) 
		{
			pExpanded().set(! pExpanded().get());
			return true;
		}
		else return false;
	}
	
}
