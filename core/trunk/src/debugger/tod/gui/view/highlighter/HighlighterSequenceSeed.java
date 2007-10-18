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
package tod.gui.view.highlighter;

import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.IEventFilter;
import tod.gui.IGUIManager;
import tod.gui.eventsequences.IEventSequenceSeed;
import tod.gui.eventsequences.IEventSequenceView;
import zz.utils.properties.IRWProperty;
import zz.utils.properties.SimpleRWProperty;

public class HighlighterSequenceSeed implements IEventSequenceSeed
{
	private String itsTitle;
	
	private IRWProperty<IEventBrowser> pBackgroundBrowser =
		new SimpleRWProperty<IEventBrowser>();
	
	private IRWProperty<IEventBrowser> pForegroundBrowser =
		new SimpleRWProperty<IEventBrowser>();

	public HighlighterSequenceSeed(
			String aTitle,
			IEventBrowser aBackgroundBrowser,
			IEventBrowser aForegroundBrowser)
	{
		itsTitle = aTitle;
		pBackgroundBrowser.set(aBackgroundBrowser);
		pForegroundBrowser.set(aForegroundBrowser);
	}

	public IEventSequenceView createView(IGUIManager aGUIManager)
	{
		return new HighlighterSequenceView(aGUIManager, this);
	}

	public IRWProperty<IEventBrowser> pBackgroundBrowser()
	{
		return pBackgroundBrowser;
	}

	public IRWProperty<IEventBrowser> pForegroundBrowser()
	{
		return pForegroundBrowser;
	}
	
	public String getTitle()
	{
		return itsTitle;
	}

	/**
	 * Sets the foreground browser to the intersection of the background
	 * browser and the specified filter.
	 */
	public void setFilter(IEventFilter aFilter)
	{
		IEventBrowser theEventBrowser = aFilter != null ? 
				pBackgroundBrowser().get().createIntersection(aFilter)
				: null;
				
		pForegroundBrowser().set(theEventBrowser);
	}
	
	
}
