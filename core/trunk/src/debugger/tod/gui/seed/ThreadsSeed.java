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
package tod.gui.seed;

import tod.core.database.browser.ILogBrowser;
import tod.gui.IGUIManager;
import tod.gui.view.LogView;
import tod.gui.view.ThreadsView;

/**
 * This seed provides a view that lets the user browse all events that occured in
 * each thread.
 * @author gpothier
 */
public class ThreadsSeed extends Seed
{

	public ThreadsSeed(IGUIManager aGUIManager, ILogBrowser aLog)
	{
		super(aGUIManager, aLog);
	}

	@Override
	protected LogView requestComponent()
	{
		ThreadsView theView = new ThreadsView(getGUIManager(), getEventTrace(), this);
		theView.init();
		return theView;
	}

}
