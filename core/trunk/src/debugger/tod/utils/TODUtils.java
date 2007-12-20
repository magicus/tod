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
package tod.utils;

import tod.core.database.browser.IEventFilter;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.structure.IBehaviorInfo;
import tod.impl.database.structure.standard.ExceptionResolver.BehaviorInfo;

public class TODUtils
{
	/**
	 * Returns a filter that accepts only events that occurred at the given location
	 */
	public static IEventFilter getLocationFilter(
			ILogBrowser aLogBrowser,
			IBehaviorInfo aBehavior, 
			int aLine)
	{
		if (aBehavior == null) return null;
		int[] theLocations = aBehavior.getBytecodeLocations(aLine);
		if (theLocations != null && theLocations.length>0)
		{
			IEventFilter[] theLocationFilters = new IEventFilter[theLocations.length];
			for(int i=0;i<theLocationFilters.length;i++)
			{
				theLocationFilters[i] = 
					aLogBrowser.createLocationFilter(aBehavior, theLocations[i]);
			}
			return aLogBrowser.createUnionFilter(theLocationFilters);
		}
		else return null;
	}

	
	/**
	 * Creates an exception resolver {@link BehaviorInfo} object 
	 * describing the specified behavior.
	 */
	public static BehaviorInfo createBehaviorInfo(IBehaviorInfo aBehavior)
	{
		return new BehaviorInfo(
				aBehavior.getType().getName(),
				aBehavior.getName(),
				aBehavior.getSignature(),
				aBehavior.getId());
	}
	

}
