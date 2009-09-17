/*
 * Created on Oct 25, 2005
 */
package tod.agent;

import java.io.IOException;

import tod.core.ILogCollector;
import tod.core.transport.DummyCollector;
import tod.core.transport.SocketCollector;
import tod.utils.ConfigUtils;

/**
 * Configuration of the agent in the target VM. 
 * @author gpothier
 */
public class AgentConfig
{
	public static final String PARAM_COLLECTOR_HOST = "collector-host";
	public static final String PARAM_COLLECTOR_PORT = "collector-port";
	public static final String PARAM_NATIVE_PEER_PORT = "native-port";
	public static final String PARAM_COLLECTOR_TYPE = "collector-type";
	public static final String PARAM_COLLECTOR_TYPE_SOCKET = "socket";
	public static final String PARAM_COLLECTOR_TYPE_DUMMY = "dummy";
	
	/**
	 * This parameter defines the name of the host the agent runs on.
	 */
	public static final String PARAM_HOST = "tod-host";

	private static ILogCollector itsCollector;
	
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
	
	
	static
	{
		String theCollectorType = ConfigUtils.readString(PARAM_COLLECTOR_TYPE, PARAM_COLLECTOR_TYPE_SOCKET);
		itsEventsPort = ConfigUtils.readInt(PARAM_COLLECTOR_PORT, 8058);
		itsNativePort = ConfigUtils.readInt(PARAM_NATIVE_PEER_PORT, 8059);
		itsHost = ConfigUtils.readString(PARAM_COLLECTOR_HOST, "localhost");
		itsHostName = ConfigUtils.readString(PARAM_HOST, "noname");
		
		if (PARAM_COLLECTOR_TYPE_DUMMY.equals(theCollectorType))
			itsCollector = createDummyCollector();
		else if (PARAM_COLLECTOR_TYPE_SOCKET.equals(theCollectorType)) 
			itsCollector = createSocketCollector();
		else 
			throw new RuntimeException("Unknown collector type: "+theCollectorType);
		
	}
	
	private static ILogCollector createDummyCollector()
	{
		System.out.println("Using dummy collector.");
		return new DummyCollector();
	}
	
	private static ILogCollector createSocketCollector()
	{
		System.out.println("Using socket collector ("+itsHost+":"+itsEventsPort);
		try
		{
			return new SocketCollector(itsHost, itsEventsPort);
		}
		catch (IOException e)
		{
			throw new RuntimeException("Cannot initialize collector", e);
		}
	}
	
	/**
	 * This method is called by instrumented code to obtain the current collector.
	 */
	public static ILogCollector getCollector()
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