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
	private boolean itsMouseOver = false;
	
	public SVGHyperlink(IGUIManager aGUIManager, Seed aSeed)
	{
		itsGUIManager = aGUIManager;
		itsSeed = aSeed;
	}

	@Override
	public void mouseEntered(GraphicObjectContext aContext, MouseEvent aEvent)
	{
		setMouseOver(MouseModifiers.hasCtrl(aEvent));
	}
	
	@Override
	public void mouseExited(GraphicObjectContext aContext, MouseEvent aEvent)
	{
		setMouseOver(false);
	}
	
	@Override
	public boolean mouseMoved(GraphicObjectContext aContext, MouseEvent aEvent, Point2D aPoint)
	{
		boolean theCtrl = MouseModifiers.hasCtrl(aEvent); 
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
		if (MouseModifiers.hasCtrl(aEvent))
		{
			itsGUIManager.openSeed(itsSeed, MouseModifiers.hasCtrl(aEvent));
			return true;
		}
		else return false;
	}
	
	/**
	 * Creates a new flow text with default size computer.
	 */
	public static SVGHyperlink create(IGUIManager aGUIManager, Seed aSeed, String aText, XFont aFont, Color aColor)
	{
		XFont theFont = aFont.isUnderline() ? new XFont(aFont.getAWTFont(), false) : aFont;
		
		SVGHyperlink theHyperlink = new SVGHyperlink(aGUIManager, aSeed);
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
