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

import java.util.Arrays;

import tod.core.config.TODConfig;
import tod.core.database.browser.IEventFilter;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.structure.IBehaviorInfo;

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
		logf(0, "Trying to show events for byte code locations %s", Arrays.asList(theLocations));
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
	 * Prints the specified message if the current verbosity level is >= the
	 * specified level.
	 */
	public static void log(int aLevel, String aMessage)
	{
		if (TODConfig.TOD_VERBOSE >= aLevel) System.out.println(aMessage);
	}

	public static void logf(int aLevel, String aText, Object... aArgs)
	{
		log(aLevel, String.format(aText, aArgs));
	}
	
	/**
	 * Helper method for throwing a formatted {@link RuntimeException}.
	 */
	public static void rtex(String aText, Object... aArgs)
	{
		throw new RuntimeException(String.format(aText, aArgs));
	}
	
	
}
