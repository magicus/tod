/*
 * Created on Jul 23, 2006
 */
package tod.impl.dbgrid.messages;

import tod.core.database.structure.IBehaviorInfo;
import tod.impl.common.event.BehaviorCallEvent;
import tod.impl.common.event.BehaviorExitEvent;
import tod.impl.common.event.ConstructorChainingEvent;
import tod.impl.common.event.Event;
import tod.impl.common.event.ExceptionGeneratedEvent;
import tod.impl.common.event.FieldWriteEvent;
import tod.impl.common.event.InstantiationEvent;
import tod.impl.common.event.LocalVariableWriteEvent;
import tod.impl.common.event.MethodCallEvent;
import tod.impl.common.event.OutputEvent;
import tod.impl.dbgrid.DebuggerGridConfig;
import tod.impl.dbgrid.GridEventCollector;
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
	
	public GridEvent(
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

	public GridEvent(Event aEvent)
	{
		itsHost = aEvent.getHost().getId();
		itsThread = GridEventCollector.getThreadNumber(aEvent);
		itsDepth = aEvent.getDepth();
		itsTimestamp = aEvent.getTimestamp();
		itsOperationBytecodeIndex = aEvent.getOperationBytecodeIndex();
		itsParentTimestamp = aEvent.getParent().getTimestamp();
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
		StdIndexSet.StdTuple theStdTuple = new StdIndexSet.StdTuple(getTimestamp(), aPointer);
		
		aIndexes.typeIndex.addTuple((byte) getEventType().ordinal(), theStdTuple);
		
		if (getOperationBytecodeIndex() >= 0)
			aIndexes.bytecodeLocationIndex.addTuple(getOperationBytecodeIndex(), theStdTuple);
		
		if (getHost() > 0) 
			aIndexes.hostIndex.addTuple(getHost(), theStdTuple);
		
		if (getThread() > 0) 
			aIndexes.threadIndex.addTuple(getThread(), theStdTuple);
		
		aIndexes.depthIndex.addTuple(getDepth(), theStdTuple);
	}

	/**
	 * Creates a {@link GridEvent} with the information extracted from and
	 * {@link Event}.
	 */
	public static GridEvent create(Event aEvent)
	{
		if (aEvent instanceof ConstructorChainingEvent)
		{
			ConstructorChainingEvent theEvent = (ConstructorChainingEvent) aEvent;
			return createBehaviorCall(theEvent, MessageType.CONSTRUCTOR_CHAINING);
		}
		else if (aEvent instanceof MethodCallEvent)
		{
			MethodCallEvent theEvent = (MethodCallEvent) aEvent;
			return createBehaviorCall(theEvent, MessageType.METHOD_CALL);
		}
		else if (aEvent instanceof InstantiationEvent)
		{
			InstantiationEvent theEvent = (InstantiationEvent) aEvent;
			return createBehaviorCall(theEvent, MessageType.INSTANTIATION);			
		}
		else if (aEvent instanceof BehaviorExitEvent)
		{
			BehaviorExitEvent theEvent = (BehaviorExitEvent) aEvent;
			IBehaviorInfo theBehavior = theEvent.getParent().getExecutedBehavior();
			if (theBehavior == null) theBehavior = theEvent.getParent().getCalledBehavior();
			
			return new GridBehaviorExitEvent(
					theEvent, 
					theEvent.hasThrown(), 
					theEvent.getResult(),
					theBehavior.getId());
		}
		else if (aEvent instanceof ExceptionGeneratedEvent)
		{
			ExceptionGeneratedEvent theEvent = (ExceptionGeneratedEvent) aEvent;
			return new GridExceptionGeneratedEvent(
					theEvent,
					theEvent.getException(),
					theEvent.getThrowingBehavior().getId());
		}
		else if (aEvent instanceof FieldWriteEvent)
		{
			FieldWriteEvent theEvent = (FieldWriteEvent) aEvent;
			return new GridFieldWriteEvent(
					theEvent,
					theEvent.getField().getId(),
					theEvent.getTarget(),
					theEvent.getValue());
		}
		else if (aEvent instanceof LocalVariableWriteEvent)
		{
			LocalVariableWriteEvent theEvent = (LocalVariableWriteEvent) aEvent;
			return new GridVariableWriteEvent(
					theEvent,
					theEvent.getVariable().getIndex(),
					theEvent.getValue());
		}
		else if (aEvent instanceof OutputEvent)
		{
			OutputEvent theEvent = (OutputEvent) aEvent;
			return new GridOutputEvent(
					theEvent,
					theEvent.getData(),
					theEvent.getOutput());
		}
		else throw new RuntimeException("Not handled: "+aEvent);
	}

	private static GridBehaviorCallEvent createBehaviorCall(BehaviorCallEvent aEvent, MessageType aType)
	{
		return new GridBehaviorCallEvent(
				aEvent,
				aType,
				aEvent.isDirectParent(),
				aEvent.getArguments(),
				aEvent.getCalledBehavior() != null ? aEvent.getCalledBehavior().getId() : 0,
				aEvent.getExecutedBehavior() != null ? aEvent.getExecutedBehavior().getId() : 0,
				aEvent.getTarget());
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
