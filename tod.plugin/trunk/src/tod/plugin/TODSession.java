/*
 * Created on Aug 13, 2005
 */
package tod.plugin;

import reflex.lib.logging.miner.api.IBrowsableLog;

/**
 * Describes a debugger session
 * @author gpothier
 */
public class TODSession
{
	private IBrowsableLog itsLog;

	public TODSession(IBrowsableLog aLog)
	{
		itsLog = aLog;
	}

	public IBrowsableLog getLog()
	{
		return itsLog;
	}
}
