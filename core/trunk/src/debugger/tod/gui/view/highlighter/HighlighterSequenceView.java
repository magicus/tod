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

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import tod.core.database.browser.IEventBrowser;
import tod.gui.BrowserData;
import tod.gui.IGUIManager;
import tod.gui.eventsequences.AbstractSequenceView;
import zz.utils.properties.IProperty;
import zz.utils.properties.IPropertyListener;
import zz.utils.properties.PropertyListener;

/**
 * A sequence view that displays a browser as background, which permits to
 * give a context to a foreground browser.
 * @author gpothier
 */
public class HighlighterSequenceView extends AbstractSequenceView
{
	private HighlighterSequenceSeed itsSeed;
	
	private IPropertyListener<IEventBrowser> itsListener = 
		new PropertyListener<IEventBrowser>()
		{
			@Override
			public void propertyChanged(IProperty<IEventBrowser> aProperty, IEventBrowser aOldValue, IEventBrowser aNewValue)
			{
				update();
			}
		};

	public HighlighterSequenceView(IGUIManager aGUIManager, HighlighterSequenceSeed aSeed)
	{
		super(aGUIManager);
		itsSeed = aSeed;
	}

	
	public HighlighterSequenceSeed getSeed()
	{
		return itsSeed;
	}
	
	@Override
	public void addNotify()
	{
		super.addNotify();
		getSeed().pBackgroundBrowser().addHardListener(itsListener);
		getSeed().pForegroundBrowser().addHardListener(itsListener);
	}
	
	@Override
	public void removeNotify()
	{
		super.removeNotify();
		getSeed().pBackgroundBrowser().removeListener(itsListener);
		getSeed().pForegroundBrowser().removeListener(itsListener);
	}

	@Override
	protected final List<BrowserData> getBrowsers()
	{
		List<BrowserData> theBrowsers = new ArrayList<BrowserData>();
		
		IEventBrowser theBackgroundBrowser = getSeed().pBackgroundBrowser().get();
		if(theBackgroundBrowser != null)
		{
			theBrowsers.add (new BrowserData(theBackgroundBrowser, Color.LIGHT_GRAY));
		}
		
		IEventBrowser theForegroundBrowser = getSeed().pForegroundBrowser().get();
		if (theForegroundBrowser != null) 
		{
			theBrowsers.add (new BrowserData(theForegroundBrowser, Color.BLUE.darker()));
		}
		
		return theBrowsers;
	}


	public String getTitle()
	{
		return getSeed().getTitle();
	}

}
