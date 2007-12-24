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
package tod.impl.local.filter;

import tod.core.database.browser.IEventBrowser;
import tod.impl.local.EventBrowser;
import tod.impl.local.LocalBrowser;

/**
 * A stateless filter is determined only by a condition.
 * @author gpothier
 */
public abstract class AbstractStatelessFilter extends AbstractFilter
{
	public AbstractStatelessFilter(LocalBrowser aBrowser)
	{
		super(aBrowser);
	}

	public IEventBrowser createBrowser ()
	{
		return new EventBrowser (getBrowser(), getBrowser().getEvents(), this);
	}
}
