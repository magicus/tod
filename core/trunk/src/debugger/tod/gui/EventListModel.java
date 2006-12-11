/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier (gpothier -at- dcc . uchile . cl)

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

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;

import tod.core.database.browser.IEventBrowser;

/**
 * A swing list model of an {@link tod.core.database.browser.IEventBrowser}
 * @author gpothier
 */
public class EventListModel extends AbstractListModel
{
	private IEventBrowser itsBrowser;
	
	// TODO: this is a hack, UI should be changed so that we don't use lists
	private List itsList = new ArrayList();
	
	public EventListModel (IEventBrowser aBrowser)
	{
		itsBrowser = aBrowser;
		while (aBrowser.hasNext()) itsList.add(aBrowser.next());
	}

	public int getSize()
	{
		return itsList.size();
	}

	public Object getElementAt(int aIndex)
	{
		return itsList.get(aIndex);
	}
}
