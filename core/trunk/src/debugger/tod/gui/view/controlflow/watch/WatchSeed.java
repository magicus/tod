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
package tod.gui.view.controlflow.watch;

import tod.core.database.browser.ILogBrowser;
import tod.core.database.event.ILogEvent;
import tod.gui.seed.Seed;

public abstract class WatchSeed extends Seed
{
	private String itsTitle;
	private WatchPanel itsWatchPanel;
	private ILogBrowser itsLogBrowser;
	private ILogEvent itsRefEvent;
	

	public WatchSeed(
			String aTitle,
			WatchPanel aWatchPanel, 
			ILogBrowser aLogBrowser, 
			ILogEvent aRefEvent)
	{
		itsTitle = aTitle;
		itsWatchPanel = aWatchPanel;
		itsLogBrowser = aLogBrowser;
		itsRefEvent = aRefEvent;
	}

	public String getTitle()
	{
		return itsTitle;
	}
	
	public WatchPanel getWatchPanel()
	{
		return itsWatchPanel;
	}

	public ILogBrowser getLogBrowser()
	{
		return itsLogBrowser;
	}

	public ILogEvent getRefEvent()
	{
		return itsRefEvent;
	}

	@Override
	public void open()
	{
		itsWatchPanel.showWatch(createProvider());
	}
	
	public abstract AbstractWatchProvider createProvider();
}
