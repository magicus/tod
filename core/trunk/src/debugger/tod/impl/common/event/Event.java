/*
 * Created on Oct 25, 2004
 */
package tod.impl.common.event;

import java.util.HashMap;
import java.util.Map;

import tod.core.model.event.ICallerSideEvent;
import tod.core.model.structure.IHostInfo;
import tod.core.model.structure.IThreadInfo;

/**
 * Base class of all logged events.
 * @author gpothier
 */
public abstract class Event implements ICallerSideEvent
{
	private long itsTimestamp;
	
	private IHostInfo itsHost;
	private IThreadInfo itsThread;
	private long itsSerial;
	
	private int itsOperationBytecodeIndex;
	
	private BehaviorCallEvent itsParent;
	
	/**
	 * A map of additional attributes that can be attached to this event.
	 */
	private Map<Object, Object> itsAttributes;
	
	public BehaviorCallEvent getParent()
	{
		return itsParent;
	}

	public void setParent(BehaviorCallEvent aParent)
	{
		itsParent = aParent;
	}

	public IThreadInfo getThread()
	{
		return itsThread;
	}
	
	public void setThread(IThreadInfo aThreadInfo)
	{
		itsThread = aThreadInfo;
	}
	
	public IHostInfo getHost()
	{
		return itsHost;
	}

	public void setHost(IHostInfo aHost)
	{
		itsHost = aHost;
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

	/**
	 * Sets an additional attribute of this event.
	 */
	public Object putAttribute(Object aKey, Object aValue)
	{
		if (itsAttributes == null) itsAttributes = new HashMap<Object, Object>();
		return itsAttributes.put(aKey, aValue);
	}
	
	/**
	 * Returns the value of an additional attribute. 
	 */
	public Object getAttribute(Object aKey)
	{
		return itsAttributes != null ?
				itsAttributes.get(aKey)
				: null;
	}
}
