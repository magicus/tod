/*
 * Created on Oct 25, 2005
 */
package tod.agent;

import java.io.IOException;

import tod.core.DummyCollector;
import tod.core.ILogCollector;
import tod.core.transport.SocketCollector;
import tod.utils.ConfigUtils;

/**
 * Configuration of the agent in the target VM. 
 * @author gpothier
 */
public class AgentConfig
{
	public static final String PARAM_COLLECTOR_PORT = "collector-port";
	public static final String PARAM_COLLECTOR_HOST = "collector-host";
	public static final String PARAM_COLLECTOR_TYPE = "collector-type";
	public static final String PARAM_COLLECTOR_TYPE_SOCKET = "socket";
	public static final String PARAM_COLLECTOR_TYPE_DUMMY = "dummy";

	private static ILogCollector COLLECTOR;
	
	static
	{
		String theCollectorType = ConfigUtils.readString(PARAM_COLLECTOR_TYPE, PARAM_COLLECTOR_TYPE_SOCKET);
		if (PARAM_COLLECTOR_TYPE_DUMMY.equals(theCollectorType)) COLLECTOR = createDummyCollector();
		else if (PARAM_COLLECTOR_TYPE_SOCKET.equals(theCollectorType)) COLLECTOR = createSocketCollector();
		else throw new RuntimeException("Unknown collector type: "+theCollectorType);
		
	}
	
	private static ILogCollector createDummyCollector()
	{
		System.out.println("Using dummy collector.");
		return new DummyCollector();
	}
	
	private static ILogCollector createSocketCollector()
	{
		int thePort = ConfigUtils.readInt(PARAM_COLLECTOR_PORT, 8058);
		String theHost = ConfigUtils.readString(PARAM_COLLECTOR_HOST, "localhost");
		
		System.out.println("Using socket collector ("+theHost+":"+thePort);
		try
		{
			return new SocketCollector(theHost, thePort);
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
		return COLLECTOR;
	}
}
