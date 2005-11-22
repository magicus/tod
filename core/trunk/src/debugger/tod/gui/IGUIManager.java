/*
 * Created on Nov 13, 2004
 */
package tod.gui;

import tod.core.model.event.ILogEvent;
import tod.gui.seed.Seed;

/**
 * This interface permits to access the basic functionalities
 * of the UI, such as setting a new view, etc.
 * All interactive UI components should have a reference to
 * a GUI manager
 * @author gpothier
 */
public interface IGUIManager
{
	/**
	 * Sets the currently viewed seed.
	 * @param aNewTab If false, the viewer for the seed will replace the
	 * currently displayed viewer. If true, a new tab will be opened.
	 */
	public void openSeed (Seed aSeed, boolean aNewTab);
	
	/**
	 * Shows the location of the specified event in the source code.
	 */
	public void gotoEvent (ILogEvent aEvent);
}
