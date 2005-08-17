/*
 * Created on Aug 16, 2005
 */
package tod.plugin;

import java.io.IOException;
import java.sql.SQLException;

import reflex.lib.logging.core.impl.transport.LogReceiver;
import reflex.lib.logging.miner.impl.sql.Queries;
import reflex.lib.logging.miner.impl.sql.SQLBrowsableLog;
import reflex.lib.logging.miner.impl.sql.backend.PostgreSQLBackend;
import zz.utils.Pool;

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
			PostgreSQLBackend theBackend = new PostgreSQLBackend();
			SQLBrowsableLog theLog = new SQLBrowsableLog(new Queries(theBackend));

			itsSession = new TODSession(4012, theLog, theLog.getCollector());
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
