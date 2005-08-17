/*
 * Created on Aug 13, 2005
 */
package tod.plugin;

import java.io.IOException;
import java.net.ServerSocket;

import reflex.lib.logging.core.api.collector.ILogCollector;
import reflex.lib.logging.core.impl.transport.LogReceiver;
import reflex.lib.logging.miner.api.IBrowsableLog;

/**
 * Describes a debugger session
 * @author gpothier
 */
public class TODSession extends LogReceiver
{
	private final IBrowsableLog itsLog;
	private final int itsPort;

	public TODSession(int aPort, IBrowsableLog aLog, ILogCollector aCollector) throws IOException
	{
		super (new ServerSocket(aPort), aCollector);
		itsPort = aPort;
		itsLog = aLog;	
	}
	
	public int getPort()
	{
		return itsPort;
	}

	public IBrowsableLog getLog()
	{
		return itsLog;
	}
	
}
