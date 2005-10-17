/*
 * Created on Aug 16, 2005
 */
package tod.plugin;

import reflex.lib.logging.miner.api.IBrowsableLog;
import reflex.lib.logging.miner.impl.local.LocalCollector;
import reflex.lib.logging.miner.impl.sql.Queries;
import reflex.lib.logging.miner.impl.sql.SQLBrowsableLog;
import reflex.lib.logging.miner.impl.sql.backend.PostgreSQLBackend;

/**
 * Manages a pool of debugging sessions.
 * @author gpothier
 */
public class TODSessionManager
{
	private static TODSessionManager INSTANCE = new TODSessionManager();
	
	private TODSession itsSession;

	public static TODSessionManager getInstance()
	{
		return INSTANCE;
	}

	private TODSessionManager()
	{
		try
		{
			LocalCollector theLog = new LocalCollector();
			itsSession = new TODSession(4012, theLog, theLog);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Obtains a free, clean collector session.
	 */
	public TODSession getCleanSession()
	{
		return itsSession;
	}
	
}
