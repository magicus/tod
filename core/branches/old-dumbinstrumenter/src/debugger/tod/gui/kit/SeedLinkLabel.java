/*
 * Created on Nov 13, 2004
 */
package tod.gui.kit;

import tod.gui.IGUIManager;
import tod.gui.seed.Seed;

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
