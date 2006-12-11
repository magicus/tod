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
package tod.impl.local.filter;

import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.IEventFilter;
import tod.core.database.event.ILogEvent;
import tod.impl.local.LocalBrowser;
import tod.impl.local.LocalBrowser;

/**
 * Abstract base class for stateless filters. They only implement 
 * the reset method, which does nothing.
 * @author gpothier
 */
public abstract class AbstractFilter implements IEventFilter
{
	private LocalBrowser itsBrowser;
	
	public AbstractFilter(LocalBrowser aBrowser)
	{
		itsBrowser = aBrowser;
	}
	
	
	protected LocalBrowser getBrowser()
	{
		return itsBrowser;
	}

	/**
	 * Whether the specified event is accepted by this filter.
	 */
	public abstract boolean accept (ILogEvent aEvent);
	
	public abstract IEventBrowser createBrowser ();

}
