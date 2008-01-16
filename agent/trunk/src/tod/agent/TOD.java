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
package tod.agent;


/**
 * Entry point for debugged applications that need to control
 * the activity of TOD.
 * @author gpothier
 */
public class TOD
{
	static
	{
		if (AgentReady.isEnabled()) System.out.println("[TOD] Native agent detected.");
		else System.out.println("[TOD] Native agent not detected.");
	}
	
	/**
	 * Clears all previously recorded events.
	 */
	public static void clearDatabase()
	{
		if (AgentReady.isEnabled()) 
		{
			System.out.println("[TOD] Sending clearDatabase request...");
			AgentConfig.getInterpreter().clear();
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
		if (AgentReady.isEnabled()) 
		{
			System.out.println("[TOD] Sending flushEvents request...");
			AgentConfig.getInterpreter().flush();
			System.out.println("[TOD] flushEvents request done.");
		}
		else
		{
			System.out.println("[TOD] Ignoring flushEvents request: native agent not detected.");
		}
	}
}
