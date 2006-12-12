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
package tod.gui.eventsequences;

import java.awt.Color;
import java.util.Collections;
import java.util.List;

import tod.core.database.browser.IEventBrowser;
import tod.gui.BrowserData;
import tod.gui.view.LogView;
import zz.csg.api.IDisplay;

/**
 * A browser sequence view for a single browser.
 * @author gpothier
 */
public abstract class AbstractSingleBrowserSequenceView extends AbstractSequenceView
{
	private final Color itsColor;

	public AbstractSingleBrowserSequenceView(IDisplay aDisplay, LogView aLogView, Color aColor)
	{
		super(aDisplay, aLogView);
		itsColor = aColor;
	}

	@Override
	protected final List<BrowserData> getBrowsers()
	{
		return Collections.singletonList(new BrowserData(getBrowser(), itsColor));
	}

	/**
	 * Subclasses must specify the browser to use by implementing this method.
	 */
	protected abstract IEventBrowser getBrowser();

}
