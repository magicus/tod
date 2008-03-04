/*
TOD - Trace Oriented Debugger.
Copyright (c) 2006-2008, Guillaume Pothier
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this 
      list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, 
      this list of conditions and the following disclaimer in the documentation 
      and/or other materials provided with the distribution.
    * Neither the name of the University of Chile nor the names of its contributors 
      may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
POSSIBILITY OF SUCH DAMAGE.

Parts of this work rely on the MD5 algorithm "derived from the RSA Data Security, 
Inc. MD5 Message-Digest Algorithm".
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
