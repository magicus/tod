/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier

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

Contact: gpothier -at- dcc . uchile . cl
*/
package tod.gui;

import java.awt.Color;

import tod.core.database.browser.IEventBrowser;

/**
 * Data agregate for browsers that are used in an {@link tod.gui.eventsequences.EventMural}
 * or a {@link tod.gui.TimeScale}. Apart from an {@link tod.core.database.browser.IEventBrowser}
 * it contains a color that indicates how the events of the broswser should be rendered.
 * @author gpothier
 */
public class BrowserData
{
	private IEventBrowser itsBrowser;
	private Color itsColor;
	
	public BrowserData(IEventBrowser aBrowser, Color aColor)
	{
		itsBrowser = aBrowser;
		itsColor = aColor;
	}

	public IEventBrowser getBrowser()
	{
		return itsBrowser;
	}

	public Color getColor()
	{
		return itsColor;
	}
}
