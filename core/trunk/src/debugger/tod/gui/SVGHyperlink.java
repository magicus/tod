/*
 * Created on Oct 18, 2005
 */
package tod.gui;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import reflex.lib.logging.miner.gui.IGUIManager;
import reflex.lib.logging.miner.gui.seed.Seed;
import reflex.lib.logging.miner.gui.view.LogView;
import zz.csg.api.GraphicObjectContext;
import zz.csg.api.figures.IGOFlowText.DefaultSizeComputer;
import zz.csg.impl.figures.SVGFlowText;
import zz.utils.ui.MouseModifiers;
import zz.utils.ui.text.XFont;

public class SVGHyperlink extends SVGFlowText
{
	private IGUIManager itsGUIManager;
	private Seed itsSeed;
	private Color itsColor;
	
	public SVGHyperlink(IGUIManager aGUIManager, Seed aSeed, Color aColor)
	{
		itsGUIManager = aGUIManager;
		itsSeed = aSeed;
		itsColor = aColor;
	}

	@Override
	public void mouseEntered(GraphicObjectContext aContext)
	{
		pStrokePaint().set(itsColor.darker());
	}
	
	@Override
	public void mouseExited(GraphicObjectContext aContext)
	{
		pStrokePaint().set(itsColor);
	}
	
	@Override
	public boolean mouseClicked(GraphicObjectContext aContext, MouseEvent aEvent, Point2D aPoint)
	{
		itsGUIManager.openSeed(itsSeed, MouseModifiers.hasCtrl(aEvent));
		return true;
	}
	
	/**
	 * Creates a new flow text with default size computer.
	 */
	public static SVGHyperlink create(IGUIManager aGUIManager, Seed aSeed, String aText, XFont aFont, Color aColor)
	{
		XFont theFont = aFont.isUnderline() ? aFont : new XFont(aFont.getAWTFont(), true);
		
		SVGHyperlink theHyperlink = new SVGHyperlink(aGUIManager, aSeed, aColor);
		theHyperlink.pText().set(aText);
		theHyperlink.pStrokePaint().set(aColor);
		theHyperlink.pFont().set(theFont);
		theHyperlink.setSizeComputer(DefaultSizeComputer.getInstance());
		
		return theHyperlink;
	}
	
	/**
	 * Creates a new flow text with default size computer and font.
	 */
	public static SVGHyperlink create(IGUIManager aGUIManager, Seed aSeed, String aText, Color aColor)
	{
		return create(aGUIManager, aSeed, aText, XFont.DEFAULT_XUNDERLINED, aColor);
	}

	/**
	 * Creates a new flow text with default size computer and default font
	 * of the given size.
	 */
	public static SVGHyperlink create(IGUIManager aGUIManager, Seed aSeed, String aText, float aFontSize, Color aColor)
	{
		return create(aGUIManager, aSeed, aText, XFont.DEFAULT_XUNDERLINED.deriveFont(aFontSize), aColor);
	}

}
