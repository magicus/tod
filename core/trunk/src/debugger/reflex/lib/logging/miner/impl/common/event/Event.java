/*
 * Created on Oct 25, 2004
 */
package reflex.lib.logging.miner.impl.common.event;

import tod.core.model.event.IBehaviorCallEvent;
import tod.core.model.event.ICallerSideEvent;
import tod.core.model.structure.BehaviorInfo;
import tod.core.model.structure.ThreadInfo;

/**
 * Base class of all logged events.
 * @author gpothier
 */
public abstract class Event implements ICallerSideEvent
{
	private long itsTimestamp;
	
	private ThreadInfo itsThreadInfo;
	private long itsSerial;
	
	private int itsOperationBytecodeIndex;
	
	private IBehaviorCallEvent itsParent;
	
	public IBehaviorCallEvent getParent()
	{
		return itsParent;
	}

	public void setParent(IBehaviorCallEvent aParent)
	{
		itsParent = aParent;
	}

	public ThreadInfo getThread()
	{
		return itsThreadInfo;
	}
	
	public void setThread(ThreadInfo aThreadInfo)
	{
		itsThreadInfo = aThreadInfo;
	}
	
	public long getTimestamp()
	{
		return itsTimestamp;
	}
	
	public void setTimestamp(long aTimestamp)
	{
		itsTimestamp = aTimestamp;
	}
	
	public long getSerial()
	{
		return itsSerial;
	}
	
	public void setSerial(long aSerial)
	{
		itsSerial = aSerial;
	}
	
	public int getOperationBytecodeIndex()
	{
		return itsOperationBytecodeIndex;
	}

	public void setOperationBytecodeIndex(int aOperationBytecodeIndex)
	{
		itsOperationBytecodeIndex = aOperationBytecodeIndex;
	}

}
