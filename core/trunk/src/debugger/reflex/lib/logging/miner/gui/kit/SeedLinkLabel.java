/*
 * Created on Nov 13, 2004
 */
package reflex.lib.logging.miner.gui.kit;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import reflex.lib.logging.miner.gui.IGUIManager;
import reflex.lib.logging.miner.gui.seed.Seed;

/**
 * A hyperlink-like label, that permits to jump to a specific view.
 * @author gpothier
 */
public class SeedLinkLabel extends LinkLabel
{
	/**
	 * The seed to which to jump when this link is selected.
	 */
	private Seed itsTargetSeed;
	
	public SeedLinkLabel(IGUIManager aGUIManager, String aText, Seed aTargetSeed)
	{
		super(aGUIManager, aText);
		itsTargetSeed = aTargetSeed;
	}
	
	protected void link(int aButton,int aClickCount,boolean aCtrl,boolean aShift,boolean aAlt)
	{
		getGUIManager().openSeed(itsTargetSeed, aCtrl);
	}
	
}
