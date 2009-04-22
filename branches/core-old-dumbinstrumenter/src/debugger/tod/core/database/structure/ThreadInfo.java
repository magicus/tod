/*
 * Created on Oct 25, 2004
 */
package tod.core.database.structure;

import tod.core.ILogCollector;

/**
 * Aggregates the information a {@link ILogCollector collector}
 * receives about a thread.
 * @author gpothier
 */
public class ThreadInfo implements IThreadInfo
{
	private IHostInfo itsHost;
	private long itsId;
	private String itsName;
	
	public ThreadInfo(IHostInfo aHost, long aId)
	{
		itsHost = aHost;
		itsId = aId;
	}

	public ThreadInfo(IHostInfo aHost, long aId, String aName)
	{
		itsHost = aHost;
		itsId = aId;
		itsName = aName;
	}
	
	public long getId()
	{
		return itsId;
	}
	
	public IHostInfo getHost()
	{
		return itsHost;
	}

	public String getName()
	{
		return itsName;
	}

	public void setName(String aName)
	{
		itsName = aName;
	}
}
