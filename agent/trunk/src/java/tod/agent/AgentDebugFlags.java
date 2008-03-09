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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;


/**
 * This class groups several flags that are used to
 * disable certain features for testing purposes.
 * 
 * @author gpothier
 */
public class AgentDebugFlags
{
	/**
	 * Causes the {@link EventInterpreter} to ignore all events
	 */
	public static final boolean DISABLE_INTERPRETER = false;

	/**
	 * If true, the {@link EventInterpreter} prints all the events it receives
	 */
	public static final boolean EVENT_INTERPRETER_LOG = false;
	
	/**
	 * Stream to which the {@link EventInterpreter} sends debug info.
	 * Default is System.out
	 */
	public static final PrintStream EVENT_INTERPRETER_PRINT_STREAM =
		System.out;
//		createStream("eventInterpreter-" + AgentConfig.getHostName()+".log");

	/**
	 * Causes the socket collector to not send events
	 */
	public static final boolean DISABLE_EVENT_SEND = false;

	/**
	 * Causes the high level collectors to ignore all events
	 */
	public static final boolean COLLECTOR_IGNORE_ALL = false;



	private static PrintStream createStream(String aName)
	{
		try
		{
			File theFile = new File(aName);
			theFile.delete();
			File theParentFile = theFile.getParentFile();
			if (theParentFile != null) theParentFile.mkdirs();
//			System.out.println(theFile.getAbsolutePath());
			return new PrintStream(new FileOutputStream(theFile));
		}
		catch (FileNotFoundException e)
		{
			throw new RuntimeException(e);
		}
	}
	

	static
	{
		if (DISABLE_INTERPRETER == true) System.err.println("******* Warning: DISABLE_INTERPRETER (DebugFlags)");
		if (DISABLE_EVENT_SEND == true) System.err.println("******* Warning: DISABLE_EVENT_SEND (DebugFlags)");
		if (COLLECTOR_IGNORE_ALL == true) System.err.println("******* Warning: COLLECTOR_IGNORE_ALL (DebugFlags)");
	}
}