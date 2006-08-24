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
	private long itsId;
	private String itsName;
	
	public ThreadInfo(long aId)
	{
		itsId = aId;
	}

	public ThreadInfo(long aId, String aName)
	{
		itsId = aId;
		itsName = aName;
	}
	
	public long getId()
	{
		return itsId;
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
