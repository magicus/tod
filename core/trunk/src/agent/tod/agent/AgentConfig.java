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

import tod.core.EventInterpreter;
import tod.core.HighLevelCollector;
import tod.core.transport.DummyCollector;
import tod.core.transport.NativeCollector;
import tod.core.transport.SocketCollector;

/**
 * Configuration of the agent in the target VM. 
 * @author gpothier
 */
public class AgentConfig
{
//	static
//	{
//		System.out.println("AgentConfig loaded by: "+AgentConfig.class.getClassLoader());
//	}
//	
	public static final String PARAM_COLLECTOR_HOST = "collector-host";
	public static final String PARAM_COLLECTOR_PORT = "collector-port";
	public static final String PARAM_NATIVE_PEER_PORT = "native-port";
	public static final String PARAM_COLLECTOR_TYPE = "collector-type";
	public static final String PARAM_COLLECTOR_TYPE_SOCKET = "socket";
	public static final String PARAM_COLLECTOR_TYPE_DUMMY = "dummy";
	public static final String PARAM_COLLECTOR_TYPE_NATIVE= "native";
	
	/**
	 * This parameter defines the name of the host the agent runs on.
	 */
	public static final String PARAM_HOST = "client-hostname";
	
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
	 * Name of this host.
	 */
	private static String itsHostName;
	
	/**
	 * Host to connect to.
	 */
	private static String itsHost;
	
	/**
	 * Port to connect to for events.
	 */
	private static int itsEventsPort;
	
	/**
	 * Port to connect to for native agent
	 */
	private static int itsNativePort;
	
	private static String itsCollectorType;
	
	
	static
	{
		itsCollectorType = ConfigUtils.readString(PARAM_COLLECTOR_TYPE, PARAM_COLLECTOR_TYPE_SOCKET);
		itsEventsPort = ConfigUtils.readInt(PARAM_COLLECTOR_PORT, 8058);
		itsNativePort = ConfigUtils.readInt(PARAM_NATIVE_PEER_PORT, 8059);
		itsHost = ConfigUtils.readString(PARAM_COLLECTOR_HOST, "localhost");
		itsHostName = ConfigUtils.readString(PARAM_HOST, "no-name");
	}
	
	private static HighLevelCollector createDummyCollector()
	{
		System.out.println("[TOD] AgentConfig: Using dummy collector.");
		return new DummyCollector();
	}
	
	private static HighLevelCollector createSocketCollector()
	{
		System.out.println("[TOD] AgentConfig: Using socket collector ("+itsHost+":"+itsEventsPort+")");
		try
		{
			return new SocketCollector(itsHost, itsEventsPort);
		}
		catch (IOException e)
		{
			throw new RuntimeException("Cannot initialize collector", e);
		}
	}
	
	private static HighLevelCollector createNativeCollector()
	{
		System.out.println("[TOD] AgentConfig: Using native collector ("+itsHost+":"+itsEventsPort+")");
		return new NativeCollector(itsHost, itsEventsPort);
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
			else if (PARAM_COLLECTOR_TYPE_NATIVE.equals(itsCollectorType)) 
				itsCollector = createNativeCollector();
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

	public static String getHostName()
	{
		return itsHostName;
	}

	public static String getHost()
	{
		return itsHost;
	}

	public static int getEventsPort()
	{
		return itsEventsPort;
	}

	public static int getNativePort()
	{
		return itsNativePort;
	}

	
	
}
