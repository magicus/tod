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
package tod.gui.view;

import tod.core.database.browser.IEventBrowser;
import tod.core.database.event.ILogEvent;
import tod.gui.kit.messages.EventSelectedMsg.SelectionMethod;

/**
 * Interface for views that represent an event list in some form.
 * @author gpothier
 */
public interface IEventListView 
{
	/**
	 * Returns the event browser on which this view is based.
	 */
	public IEventBrowser getEventBrowser();
	
	/**
	 * Returns the currently selected event.
	 * @return The currently selected event, or null if no event is selected.
	 */
	public ILogEvent getSelectedEvent();
	
	/**
	 * Selects (and makes visible) the specified event.
	 */
	public void selectEvent(ILogEvent aEvent, SelectionMethod aMethod);
}
