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

import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.session.ISession;
import tod.gui.kit.Options;
import tod.gui.seed.LogViewSeed;

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
	public void openSeed (LogViewSeed aSeed, boolean aNewTab);
	
	/**
	 * Shows the location of the specified event in the source code.
	 */
	public void gotoEvent (ILogEvent aEvent);
	
	/**
	 * Returns a global job processor.
	 */
	public JobProcessor getJobProcessor();
	
	/**
	 * Stores a persistent property, which can be retrieved
	 * with {@link #getProperty(String)}.
	 */
	public void setProperty(String aKey, String aValue);
	
	/**
	 * Retrieves a persistent property previously stored with 
	 * {@link #setProperty(String, String)}.
	 * @see MinerUI#getIntProperty(IGUIManager, String, int)
	 * @see MinerUI#getBooleanProperty(IGUIManager, String, boolean)
	 */
	public String getProperty(String aKey);
	
	/**
	 * Returns the current debugging session.
	 */
	public ISession getSession();
	
	public void showNextEventForLine(IBehaviorInfo aBehavior, int aLine);
	public void showPreviousEventForLine(IBehaviorInfo aBehavior, int aLine);
	
	/**
	 * Whether the "Show next event for line" action should be enabled.
	 */
	public boolean canShowNextEventForLine();

	/**
	 * Whether the "Show previous event for line" action should be enabled.
	 */
	public boolean canShowPreviousEventForLine();

	/**
	 * Shows a list of all the events that occurred at the specified line.
	 */
	public void showEventsForLine(IBehaviorInfo aBehavior, int aLine);
	
	/**
	 * Returns the global GUI options of this GUI manager.
	 */
	public Options getOptions();
}
