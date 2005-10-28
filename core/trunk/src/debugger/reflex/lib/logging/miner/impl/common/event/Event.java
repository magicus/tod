/*
 * Created on Oct 25, 2004
 */
package reflex.lib.logging.miner.impl.common.event;

import tod.core.model.event.IBehaviorEnterEvent;
import tod.core.model.event.ILogEvent;
import tod.core.model.structure.BehaviorInfo;
import tod.core.model.structure.ThreadInfo;

/**
 * Base class of all logged events.
 * @author gpothier
 */
public abstract class Event implements IMinerEvent
{
	/**
	 * A unique id for the event.
	 * Currently only the sql implementation uses it.
	 */
	private long itsId;
	
	/**
	 * Depth of the event in its call stack (per thread).
	 */
	private int itsDepth;
	private long itsSerial;
	
	private long itsTimestamp;
	private ThreadInfo itsThreadInfo;
	
	private int itsOperationBytecodeIndex;
	
	private IBehaviorEnterEvent itsFather;
	
	public int getDepth()
	{
		return itsDepth;
	}
	
	public void setDepth(int aDepth)
	{
		itsDepth = aDepth;
	}
	
	public IBehaviorEnterEvent getFather()
	{
		return itsFather;
	}

	public void setFather(IBehaviorEnterEvent aFather)
	{
		itsFather = aFather;
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
	
	public long getId()
	{
		return itsId;
	}
	
	public void setId(long aId)
	{
		itsId = aId;
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
