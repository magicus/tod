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

import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.impl.local.LocalBrowser;
import tod.impl.local.LocalBrowser;

/**
 * A filter that accepts only behavior calls (before and after).
 * @author gpothier
 */
public class BehaviorCallFilter extends AbstractStatelessFilter
{
	private IBehaviorInfo itsBehaviour;
	
	/**
	 * Creates a filter that accepts any behaviou-related event.
	 */
	public BehaviorCallFilter(LocalBrowser aBrowser)
	{
		this (aBrowser, null);
	}

	/**
	 * Creates a filter that accepts only the events related 
	 * to a particular behaviour (method/constructor).
	 */
	public BehaviorCallFilter(LocalBrowser aBrowser, IBehaviorInfo aBehavior)
	{
		super(aBrowser);
		itsBehaviour = aBehavior;
	}
	
	public boolean accept(ILogEvent aEvent)
	{
		if (aEvent instanceof IBehaviorCallEvent)
		{
			IBehaviorCallEvent theEvent = (IBehaviorCallEvent) aEvent;
			IBehaviorInfo theExecutedBehavior = theEvent.getExecutedBehavior();
			IBehaviorInfo theCalledBehavior = theEvent.getCalledBehavior();
			
			return (theExecutedBehavior != null 
					&& theExecutedBehavior.equals(itsBehaviour))
				|| (theCalledBehavior != null
					&& theCalledBehavior.equals(itsBehaviour));
		}
		else return false;
	}

}
