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
package tod.gui;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import tod.gui.seed.Seed;
import zz.csg.api.GraphicObjectContext;
import zz.csg.impl.figures.SVGFlowText;
import zz.utils.ui.MouseModifiers;
import zz.utils.ui.text.XFont;

public class SVGHyperlink extends SVGFlowText
{
	private static final boolean WITH_CTRL = false;
	private Seed itsSeed;
	private boolean itsMouseOver = false;
	
	public SVGHyperlink()
	{
	}

	public SVGHyperlink(Seed aSeed)
	{
		itsSeed = aSeed;
	}
	
	public void setSeed(Seed aSeed)
	{
		itsSeed = aSeed;
	}

	@Override
	public void mouseEntered(GraphicObjectContext aContext, MouseEvent aEvent)
	{
		setMouseOver(! WITH_CTRL || MouseModifiers.hasCtrl(aEvent));
	}
	
	@Override
	public void mouseExited(GraphicObjectContext aContext, MouseEvent aEvent)
	{
		setMouseOver(false);
	}
	
	@Override
	public boolean mouseMoved(GraphicObjectContext aContext, MouseEvent aEvent, Point2D aPoint)
	{
		boolean theCtrl = ! WITH_CTRL || MouseModifiers.hasCtrl(aEvent); 
		setMouseOver(theCtrl);
		return theCtrl;
	}
	
	private void setMouseOver(boolean aMouseOver)
	{
		if (itsMouseOver != aMouseOver)
		{
			XFont theFont = pFont().get();
			pFont().set(new XFont(theFont.getAWTFont(), aMouseOver));
			itsMouseOver = aMouseOver;
		}
	}
	
	@Override
	public boolean mouseClicked(GraphicObjectContext aContext, MouseEvent aEvent, Point2D aPoint)
	{
		if (! WITH_CTRL || MouseModifiers.hasCtrl(aEvent))
		{
			itsSeed.open();
			return true;
		}
		else return false;
	}
	
	/**
	 * Creates a new flow text with default size computer.
	 */
	public static SVGHyperlink create(
			Seed aSeed, 
			String aText, 
			XFont aFont, 
			Color aColor)
	{
		XFont theFont = aFont.isUnderline() ? new XFont(aFont.getAWTFont(), false) : aFont;
		
		SVGHyperlink theHyperlink = new SVGHyperlink(aSeed);
		theHyperlink.pText().set(aText);
		theHyperlink.pStrokePaint().set(aColor);
		theHyperlink.pFont().set(theFont);
		theHyperlink.setSizeComputer(DefaultSizeComputer.getInstance());
		
		return theHyperlink;
	}
	
	/**
	 * Creates a new flow text with default size computer and font.
	 */
	public static SVGHyperlink create(Seed aSeed, String aText, Color aColor)
	{
		return create(aSeed, aText, XFont.DEFAULT_XUNDERLINED, aColor);
	}

	/**
	 * Creates a new flow text with default size computer and default font
	 * of the given size.
	 */
	public static SVGHyperlink create(Seed aSeed, String aText, float aFontSize, Color aColor)
	{
		return create(aSeed, aText, XFont.DEFAULT_XUNDERLINED.deriveFont(aFontSize), aColor);
	}

}
