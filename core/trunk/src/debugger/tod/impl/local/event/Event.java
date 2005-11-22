/*
 * Created on Oct 25, 2004
 */
package tod.impl.local.event;

import tod.core.model.event.IBehaviorCallEvent;
import tod.core.model.event.ICallerSideEvent;
import tod.core.model.structure.IThreadInfo;

/**
 * Base class of all logged events.
 * @author gpothier
 */
public abstract class Event implements ICallerSideEvent
{
	private long itsTimestamp;
	
	private IThreadInfo itsThreadInfo;
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

	public IThreadInfo getThread()
	{
		return itsThreadInfo;
	}
	
	public void setThread(IThreadInfo aThreadInfo)
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
