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

import java.io.IOException;

import tod.agent.transport.DummyCollector;
import tod.agent.transport.SocketCollector;

/**
 * Configuration of the agent in the target VM. 
 * @author gpothier
 */
public class AgentConfig
{
	static
	{
		System.out.println("AgentConfig loaded by: "+AgentConfig.class.getClassLoader());
	}
	
	/**
	 * Signature for connections from the native side.
	 */
	public static final int CNX_NATIVE = 0x3a71be0;
	
	/**
	 * Signature for connections from the java side.
	 */
	public static final int CNX_JAVA = 0xcafe0;
	
	public static final String PARAM_COLLECTOR_HOST = "collector-host";
	public static final String PARAM_COLLECTOR_PORT = "collector-port";
	public static final String PARAM_COLLECTOR_TYPE = "collector-type";
	public static final String PARAM_COLLECTOR_TYPE_SOCKET = "socket";
	public static final String PARAM_COLLECTOR_TYPE_DUMMY = "dummy";
	
	/**
	 * This parameter defines the name of the host the agent runs on.
	 */
	public static final String PARAM_CLIENT_NAME = "client-name";
	
	/**
	 * Number of bits used to represent the host of an event.
	 */
	public static final int HOST_BITS = 8;
	
	public static final long HOST_MASK = BitUtilsLite.pow2(HOST_BITS)-1;
	
	/**
	 * Number of bits to shift timestamp values.
	 */
	public static final int TIMESTAMP_ADJUST_SHIFT = TimestampCalibration.shift;
	
	/**
	 * Number of bits of original timestamp values that are considered inaccurate.
	 */
	public static final int TIMESTAMP_ADJUST_INACCURACY = TimestampCalibration.inaccuracy;
	
	/**
	 * Mask of artificial timestamp bits.
	 */
	public static final long TIMESTAMP_ADJUST_MASK = 
		BitUtilsLite.pow2(TIMESTAMP_ADJUST_INACCURACY+TIMESTAMP_ADJUST_SHIFT)-1;
	
	/**
	 * Size of {@link SocketCollector} buffer. 
	 */
	public static final int COLLECTOR_BUFFER_SIZE = 32768;


	private static HighLevelCollector itsCollector;
	
	private static EventInterpreter itsInterpreter;
	
	/**
	 * Name of this client.
	 */
	private static String itsClientName;
	
	/**
	 * Collector host to connect to.
	 */
	private static String itsHost;
	
	/**
	 * Port to connect to for events.
	 */
	private static int itsPort;
	
	private static String itsCollectorType;
	
	
	static
	{
		itsCollectorType = AgentUtils.readString(PARAM_COLLECTOR_TYPE, PARAM_COLLECTOR_TYPE_SOCKET);
		itsPort = AgentUtils.readInt(PARAM_COLLECTOR_PORT, 8058);
		itsHost = AgentUtils.readString(PARAM_COLLECTOR_HOST, "localhost");
		itsClientName = AgentUtils.readString(PARAM_CLIENT_NAME, "no-name");
	}
	
	private static HighLevelCollector createDummyCollector()
	{
		System.out.println("[TOD] AgentConfig: Using dummy collector.");
		return new DummyCollector();
	}
	
	private static HighLevelCollector createSocketCollector()
	{
		System.out.println("[TOD] AgentConfig: " +
				"Using socket collector ("+itsHost+":"+itsPort+")" +
				" - AgentConfig loaded by: "+AgentConfig.class.getClassLoader());
		
		try
		{
			return new SocketCollector(itsHost, itsPort);
		}
		catch (IOException e)
		{
			throw new RuntimeException("Cannot initialize collector", e);
		}
	}
	
	public static EventInterpreter getInterpreter()
	{
		if (itsInterpreter == null)
		{
//			System.out.println("Collector type: "+itsCollectorType);
			if (PARAM_COLLECTOR_TYPE_DUMMY.equals(itsCollectorType))
				itsCollector = createDummyCollector();
			else if (PARAM_COLLECTOR_TYPE_SOCKET.equals(itsCollectorType)) 
				itsCollector = createSocketCollector();
			else 
				throw new RuntimeException("Unknown collector type: "+itsCollectorType);
			
			itsInterpreter = new EventInterpreter(itsCollector);			
		}
		return itsInterpreter;
	}

	/**
	 * This method is called by instrumented code to obtain the current collector.
	 */
	public static HighLevelCollector getCollector()
	{
		return itsCollector;
	}

	public static String getClientName()
	{
		return itsClientName;
	}

	public static String getHost()
	{
		return itsHost;
	}

	public static int getPort()
	{
		return itsPort;
	}
}
