/*
 * Created on Oct 25, 2005
 */
package tod.agent;

import java.io.IOException;

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

	private static ILogCollector COLLECTOR;
	
	static
	{
		int theCollectorPort = ConfigUtils.readInt(PARAM_COLLECTOR_PORT, 8058);
		String theCollectorHost = ConfigUtils.readString(PARAM_COLLECTOR_HOST, "localhost");
		if (theCollectorPort != -1)
		{
			try
			{
				COLLECTOR = new SocketCollector(theCollectorHost, theCollectorPort);
			}
			catch (IOException e)
			{
				throw new RuntimeException("Cannot initialize collector", e);
			}
		}
	}
	
	public static ILogCollector getCollector()
	{
		return COLLECTOR;
	}
}
