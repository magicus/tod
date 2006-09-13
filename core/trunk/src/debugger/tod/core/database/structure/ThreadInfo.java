/*
 * Created on Oct 25, 2004
 */
package tod.core.database.structure;

import java.io.Serializable;


/**
 * Aggregates the information a {@link ILogCollector collector}
 * receives about a thread.
 * @author gpothier
 */
public class ThreadInfo implements IThreadInfo, Serializable
{
	private IHostInfo itsHost;
	private int itsId;
	private long itsJVMId;
	private String itsName;
	
	public ThreadInfo(IHostInfo aHost, int aId, long aId2, String aName)
	{
		itsHost = aHost;
		itsId = aId;
		itsJVMId = aId2;
		itsName = aName;
	}

	public int getId()
	{
		return itsId;
	}
	
	public long getJVMId()
	{
		return itsJVMId;
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
	
	@Override
	public String toString()
	{
		return "Thread ("+getId()+", "+getJVMId()+", "+getName()+")";
	}

}
