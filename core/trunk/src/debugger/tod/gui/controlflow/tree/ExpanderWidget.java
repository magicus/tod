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
import java.awt.geom.Rectangle2D;

import zz.csg.api.GraphicObjectContext;
import zz.csg.api.IDisplay;
import zz.csg.impl.AbstractRectangularGraphicObject;
import zz.utils.properties.IRWProperty;
import zz.utils.properties.SimpleRWProperty;
import zz.utils.ui.UIUtils;

/**
 * A widget that permits dive into the event to which it is
 * attached
 * @author gpothier
 */
public class ExpanderWidget extends AbstractRectangularGraphicObject
{
	public static final double WIDTH = 10;
	public static final double THICKNESS = 4;
	
	private Color itsColor;
	
	public ExpanderWidget(Color aColor)
	{
		itsColor = aColor;
	}

	@Override
	protected void paintTransformed(IDisplay aDisplay, GraphicObjectContext aContext, Graphics2D aGraphics, Area aVisibleArea)
	{
		Rectangle2D theBounds = pBounds().get();

		aGraphics.setColor(itsColor);
		aGraphics.fill(new Rectangle2D.Double(
				theBounds.getX() + theBounds.getWidth()/2 - THICKNESS/2, 
				theBounds.getY() + 1,
				THICKNESS,
				theBounds.getHeight() - 2));
	}
}
