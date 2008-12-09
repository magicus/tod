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
package java.tod;

import tod.agent.AgentConfig;
import tod.agent.AgentUtils;


/**
 * Entry point for debugged applications that need to control
 * the activity of TOD.
 * @author gpothier
 */
public class TOD
{
	static
	{
		if (AgentReady.isNativeAgentLoaded()) System.out.println("[TOD] Native agent detected.");
		else System.out.println("[TOD] Native agent not detected.");
	}
	
	/**
	 * If > 0, trace capture is activated
	 */
	private static int CAPTURE_ENABLED;
	
	static
	{
		CAPTURE_ENABLED = AgentUtils.readBoolean(AgentConfig.PARAM_CAPTURE_AT_START, true) ? 1 : 0;
		AgentReady.CAPTURE_ENABLED = CAPTURE_ENABLED > 0;
	}
	
	/**
	 * Clears all previously recorded events.
	 */
	public static void clearDatabase()
	{
		if (AgentReady.isNativeAgentLoaded()) 
		{
			System.out.println("[TOD] Sending clearDatabase request...");
			EventCollector.INSTANCE.clear();
			System.out.println("[TOD] clearDatabase request done.");
		}
		else
		{
			System.out.println("[TOD] Ignoring clearDatabase request: native agent not detected.");
		}
	}
	
	/**
	 * Flushes buffered events.
	 */
	public static void flushEvents()
	{
		if (AgentReady.isNativeAgentLoaded()) 
		{
			System.out.println("[TOD] Sending flushEvents request...");
			EventCollector.INSTANCE.flush();
			System.out.println("[TOD] flushEvents request done.");
		}
		else
		{
			System.out.println("[TOD] Ignoring flushEvents request: native agent not detected.");
		}
	}
	
	public static void enableCapture()
	{
		CAPTURE_ENABLED++;
		AgentReady.CAPTURE_ENABLED = CAPTURE_ENABLED > 0;
	}
	
	public static void disableCapture()
	{
		CAPTURE_ENABLED--;
		AgentReady.CAPTURE_ENABLED = CAPTURE_ENABLED > 0;
	}
	
	static int captureEnabled()
	{
		return CAPTURE_ENABLED;
	}
	
}