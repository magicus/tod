/*
Copyright (c) 2006-2008, Guillaume Pothier
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this 
      list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, 
      this list of conditions and the following disclaimer in the documentation 
      and/or other materials provided with the distribution.
    * Neither the name of the University of Chile nor the names of its contributors 
      may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
POSSIBILITY OF SUCH DAMAGE.

Parts of this work rely on the MD5 algorithm "derived from the RSA Data Security, 
Inc. MD5 Message-Digest Algorithm".
*/
package tod.gui;

import tod.core.database.browser.IEventFilter;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.SourceRange;
import tod.core.session.ISession;
import tod.gui.formatter.CustomFormatterRegistry;
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
	public void gotoSource (SourceRange aSourceRange);
	
	/**
	 * Returns a global job processor.
	 */
	public JobProcessor getJobProcessor();
	
	/**
	 * Returns the registry of custom object formatters.
	 */
	public CustomFormatterRegistry getCustomFormatterRegistry();
	
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
	 * @param aFilter An optional additional filter.
	 */
	public void showEventsForLine(IBehaviorInfo aBehavior, int aLine, IEventFilter aFilter);
	
	/**
	 * Returns the global GUI options of this GUI manager.
	 */
	public Options getOptions();
}
