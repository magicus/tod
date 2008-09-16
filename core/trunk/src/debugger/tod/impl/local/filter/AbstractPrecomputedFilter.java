/*
TOD - Trace Oriented Debugger.
Copyright (c) 2006-2008, Guillaume Pothier
All rights reserved.

This program is free software; you can redistribute it and/or 
modify it under the terms of the GNU General Public License 
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful, 
but WITHOUT ANY WARRANTY; without even the implied warranty of 
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
General Public License for more details.

You should have received a copy of the GNU General Public License 
along with this program; if not, write to the Free Software 
Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
MA 02111-1307 USA

Parts of this work rely on the MD5 algorithm "derived from the 
RSA Data Security, Inc. MD5 Message-Digest Algorithm".
*/
package tod.impl.local.filter;

import java.util.ArrayList;
import java.util.List;

import tod.core.database.browser.IEventBrowser;
import tod.core.database.event.ILogEvent;
import tod.impl.common.event.Event;
import tod.impl.local.EventBrowser;
import tod.impl.local.LocalBrowser;

/**
 * Base class for filters that are precomputed.
 * @author gpothier
 */
public abstract class AbstractPrecomputedFilter extends AbstractFilter
{
	private List<ILogEvent> itsEvents = new ArrayList<ILogEvent>();

	public AbstractPrecomputedFilter(LocalBrowser aBrowser)
	{
		super(aBrowser);
		
	}
	
	protected void addEvent (Event aEvent)
	{
		itsEvents.add (aEvent);
	}
	
	public IEventBrowser createBrowser()
	{
		return new EventBrowser(getBrowser(), itsEvents, this);
	}
	
	public boolean accept(ILogEvent aEvent)
	{
		return itsEvents.contains(aEvent);
	}
}
