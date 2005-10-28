/*
 * Created on Oct 25, 2004
 */
package tod.core.model.structure;

import tod.core.ILogCollector;
import tod.core.model.event.IBehaviorEnterEvent;

/**
 * Aggregates the information a {@link ILogCollector collector}
 * receives about a thread.
 * @author gpothier
 */
public class ThreadInfo
{
	private long itsId;
	private String itsName;
	private IBehaviorEnterEvent itsCurrentBehavior;
	
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

	public IBehaviorEnterEvent getCurrentBehavior()
	{
		return itsCurrentBehavior;
	}

	public void setCurrentBehavior(IBehaviorEnterEvent aCurrentBehavior)
	{
		itsCurrentBehavior = aCurrentBehavior;
	}
}
