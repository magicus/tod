/*
 * Created on Jul 23, 2006
 */
package tod.impl.dbgrid.messages;

import tod.core.database.event.ILogEvent;
import tod.impl.common.event.BehaviorCallEvent;
import tod.impl.common.event.Event;
import tod.impl.dbgrid.DebugFlags;
import tod.impl.dbgrid.DebuggerGridConfig;
import tod.impl.dbgrid.GridLogBrowser;
import tod.impl.dbgrid.dbnode.Indexes;
import tod.impl.dbgrid.dbnode.StdIndexSet;
import tod.impl.dbgrid.queries.BehaviorCondition;
import tod.impl.dbgrid.queries.FieldCondition;
import tod.impl.dbgrid.queries.ObjectCondition;
import tod.impl.dbgrid.queries.VariableCondition;
import zz.utils.bit.BitStruct;

public abstract class GridEvent extends GridMessage
{
	/**
	 * We can find the parent event using only its timestamp,
	 * as it necessarily belongs to the same (host, thread) as this
	 * event
	 */
	private long itsParentTimestamp;
	private int itsHost;
	private int itsThread;
	private int itsDepth;
	private long itsTimestamp;
	
	private int itsOperationBytecodeIndex;
	
	public GridEvent()
	{
	}

	public GridEvent(
			int aHost, 
			int aThread, 
			int aDepth,
			long aTimestamp, 
			int aOperationBytecodeIndex, 
			long aParentTimestamp)
	{
		set(aHost, aThread, aDepth, aTimestamp, aOperationBytecodeIndex, aParentTimestamp);
	}

	public GridEvent(BitStruct aBitStruct)
	{
		itsHost = aBitStruct.readInt(DebuggerGridConfig.EVENT_HOST_BITS);
		itsThread = aBitStruct.readInt(DebuggerGridConfig.EVENT_THREAD_BITS);
		itsDepth = aBitStruct.readInt(DebuggerGridConfig.EVENT_DEPTH_BITS);
		itsTimestamp = aBitStruct.readLong(DebuggerGridConfig.EVENT_TIMESTAMP_BITS);
		itsOperationBytecodeIndex = aBitStruct.readInt(DebuggerGridConfig.EVENT_BYTECODE_LOCATION_BITS);
		
		// TODO: this is a hack. We should not allow negative values.
		if (itsOperationBytecodeIndex == 0xffff) itsOperationBytecodeIndex = -1;
		
		itsParentTimestamp = aBitStruct.readLong(DebuggerGridConfig.EVENT_TIMESTAMP_BITS);
	}
	
	protected void set(
			int aHost, 
			int aThread, 
			int aDepth,
			long aTimestamp, 
			int aOperationBytecodeIndex, 
			long aParentTimestamp)
	{
		itsHost = aHost;
		itsThread = aThread;
		itsDepth = aDepth;
		itsTimestamp = aTimestamp;
		itsOperationBytecodeIndex = aOperationBytecodeIndex;
		itsParentTimestamp = aParentTimestamp;
	}

	/**
	 * Writes out a representation of this event to a {@link BitStruct}.
	 * Subclasses must override this method to serialize their attributes, and
	 * must call super first.
	 */
	public void writeTo(BitStruct aBitStruct)
	{
		super.writeTo(aBitStruct);
		aBitStruct.writeInt(getHost(), DebuggerGridConfig.EVENT_HOST_BITS);
		aBitStruct.writeInt(getThread(), DebuggerGridConfig.EVENT_THREAD_BITS);
		aBitStruct.writeInt(getDepth(), DebuggerGridConfig.EVENT_DEPTH_BITS);
		aBitStruct.writeLong(getTimestamp(), DebuggerGridConfig.EVENT_TIMESTAMP_BITS);
		aBitStruct.writeInt(getOperationBytecodeIndex(), DebuggerGridConfig.EVENT_BYTECODE_LOCATION_BITS);
		
		aBitStruct.writeLong(getParentTimestamp(), DebuggerGridConfig.EVENT_TIMESTAMP_BITS);
	}
	
	/**
	 * Returns the number of bits necessary to serialize this event. Subclasses
	 * should override this method and sum the number of bits they need to the
	 * number of bits returned by super.
	 */
	public int getBitCount()
	{
		int theCount = super.getBitCount();
		theCount += DebuggerGridConfig.EVENT_HOST_BITS;
		theCount += DebuggerGridConfig.EVENT_THREAD_BITS;
		theCount += DebuggerGridConfig.EVENT_DEPTH_BITS;
		theCount += DebuggerGridConfig.EVENT_TIMESTAMP_BITS;
		theCount += DebuggerGridConfig.EVENT_BYTECODE_LOCATION_BITS;
		theCount += DebuggerGridConfig.EVENT_TIMESTAMP_BITS;
		
		return theCount;
	}
	
	/**
	 * Transforms this event into a {@link ILogEvent}
	 */
	public abstract ILogEvent toLogEvent(GridLogBrowser aBrowser);
	
	/**
	 * Initializes common event fields. Subclasses can use this method in the
	 * implementation of {@link #toLogEvent(GridLogBrowser)}
	 */
	protected void initEvent(GridLogBrowser aBrowser, Event aEvent)
	{
		aEvent.setHost(aBrowser.getHost(getHost()));
		aEvent.setThread(aBrowser.getThread(getHost(), getThread()));
		aEvent.setTimestamp(getTimestamp());
		aEvent.setDepth(getDepth());
		aEvent.setOperationBytecodeIndex(getOperationBytecodeIndex());
		
		aEvent.setParent((BehaviorCallEvent) aBrowser.getEvent(getHost(), getThread(), getParentTimestamp()));
	}
	
	/**
	 * Returns the type of this event.
	 */
	public abstract MessageType getEventType();
	
	/**
	 * Returns the type of this event.
	 */
	public final MessageType getMessageType()
	{
		return getEventType();
	}
	
	public int getHost()
	{
		return itsHost;
	}

	public int getOperationBytecodeIndex()
	{
		return itsOperationBytecodeIndex;
	}

	public long getParentTimestamp()
	{
		return itsParentTimestamp;
	}

	public int getThread()
	{
		return itsThread;
	}

	public int getDepth()
	{
		return itsDepth;
	}

	public long getTimestamp()
	{
		return itsTimestamp;
	}
	
	private static StdIndexSet.StdTuple TUPLE = new StdIndexSet.StdTuple(-1, -1);
	
	/**
	 * Instructs this event to add relevant data to the indexes. The base
	 * version handles all common data; subclasses should override this method
	 * to index specific data.
	 * 
	 * @param aPointer
	 *            The internal pointer to this event.
	 */
	public void index(Indexes aIndexes, long aPointer)
	{
		// We add the same tuple to all standard indexes so we create it now.
		TUPLE.set(getTimestamp(), aPointer);
		
		aIndexes.typeIndex.addTuple((byte) getEventType().ordinal(), TUPLE);
		
		if (! DebugFlags.DISABLE_LOCATION_INDEX)
		{
			if (getOperationBytecodeIndex() >= 0)
				aIndexes.bytecodeLocationIndex.addTuple(getOperationBytecodeIndex(), TUPLE);
		}
		
		if (getHost() > 0) 
			aIndexes.hostIndex.addTuple(getHost(), TUPLE);
		
		if (getThread() > 0) 
			aIndexes.threadIndex.addTuple(getThread(), TUPLE);
		
		aIndexes.depthIndex.addTuple(getDepth(), TUPLE);
	}

	/**
	 * Whether this event matches a {@link BehaviorCondition}
	 */
	public boolean matchBehaviorCondition(int aBehaviorId, byte aRole)
	{
		return false;
	}
	
	/**
	 * Whether this event matches a {@link FieldCondition}
	 */
	public boolean matchFieldCondition(int aFieldId)
	{
		return false;
	}
	
	/**
	 * Whether this event matches a {@link VariableCondition}
	 */
	public boolean matchVariableCondition(int aVariableId)
	{
		return false;
	}
	
	/**
	 * Whether this event matches a {@link ObjectCondition}
	 */
	public boolean matchObjectCondition(int aObjectId, byte aRole)
	{
		return false;
	}
	
	/**
	 * Internal version of toString, used by subclasses.
	 */
	protected String toString0()
	{
		return String.format(
				"h: %d, bc: %d, th: %d, t: %d",
				itsHost,
				itsOperationBytecodeIndex,
				itsThread,
				itsTimestamp); 
	}
}
