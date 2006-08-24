/*
 * Created on Jul 23, 2006
 */
package tod.impl.dbgrid.messages;

import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.ObjectId;
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
import tod.impl.dbgrid.dbnode.Indexes;
import tod.impl.dbgrid.dbnode.StdIndexSet;
import tod.impl.dbgrid.dispatcher.EventDispatcher;
import tod.impl.dbgrid.dispatcher.GridEventCollector;
import tod.impl.dbgrid.queries.BehaviorCondition;
import tod.impl.dbgrid.queries.FieldCondition;
import tod.impl.dbgrid.queries.ObjectCondition;
import tod.impl.dbgrid.queries.VariableCondition;
import zz.utils.bit.BitStruct;
import zz.utils.bit.ByteBitStruct;

public abstract class GridEvent extends GridMessage
{
	private int itsHost;
	private int itsThread;
	private long itsTimestamp;
	
	private int itsOperationBytecodeIndex;
	private byte[] itsParentPointer;
	
	public GridEvent(
			int aHost, 
			int aThread, 
			long aTimestamp, 
			int aOperationBytecodeIndex, 
			byte[] aParentPointer)
	{
		itsHost = aHost;
		itsThread = aThread;
		itsTimestamp = aTimestamp;
		itsOperationBytecodeIndex = aOperationBytecodeIndex;
		itsParentPointer = aParentPointer;
	}

	public GridEvent(Event aEvent)
	{
		itsHost = aEvent.getHost().getId();
		itsThread = GridEventCollector.getThreadNumber(aEvent);
		itsTimestamp = aEvent.getTimestamp();
		itsOperationBytecodeIndex = aEvent.getOperationBytecodeIndex();
		itsParentPointer = (byte[]) aEvent.getParent().getAttribute(EventDispatcher.EVENT_ATTR_ID);
	}
	
	public GridEvent(BitStruct aBitStruct)
	{
		itsHost = aBitStruct.readInt(DebuggerGridConfig.EVENT_HOST_BITS);
		itsThread = aBitStruct.readInt(DebuggerGridConfig.EVENT_THREAD_BITS);
		itsTimestamp = aBitStruct.readLong(DebuggerGridConfig.EVENT_TIMESTAMP_BITS);
		itsOperationBytecodeIndex = aBitStruct.readInt(DebuggerGridConfig.EVENT_BYTECODE_LOCATION_BITS);
		itsParentPointer = aBitStruct.readBytes(DebuggerGridConfig.EVENTID_POINTER_SIZE);
	}

	/**
	 * Writes out a representation of this event to a {@link ByteBitStruct}.
	 * Subclasses must override this method to serialize their attributes, and
	 * must call super first.
	 */
	public void writeTo(BitStruct aBitStruct)
	{
		aBitStruct.writeInt(getEventType().ordinal(), DebuggerGridConfig.EVENT_TYPE_BITS);
		aBitStruct.writeInt(getHost(), DebuggerGridConfig.EVENT_HOST_BITS);
		aBitStruct.writeInt(getThread(), DebuggerGridConfig.EVENT_THREAD_BITS);
		aBitStruct.writeLong(getTimestamp(), DebuggerGridConfig.EVENT_TIMESTAMP_BITS);
		aBitStruct.writeInt(getOperationBytecodeIndex(), DebuggerGridConfig.EVENT_BYTECODE_LOCATION_BITS);
		aBitStruct.writeBytes(getParentPointer(), DebuggerGridConfig.EVENTID_POINTER_SIZE);
	}
	
	/**
	 * Returns the number of bits necessary to serialize this event. Subclasses
	 * should override this method and sum the number of bits they need to the
	 * number of bits returned by super.
	 */
	public int getBitCount()
	{
		int theCount = 0;
		theCount += DebuggerGridConfig.EVENT_TYPE_BITS;
		theCount += DebuggerGridConfig.EVENT_HOST_BITS;
		theCount += DebuggerGridConfig.EVENT_THREAD_BITS;
		theCount += DebuggerGridConfig.EVENT_TIMESTAMP_BITS;
		theCount += DebuggerGridConfig.EVENT_BYTECODE_LOCATION_BITS;
		theCount += DebuggerGridConfig.EVENTID_POINTER_SIZE;
		
		return theCount;
	}
	
	/**
	 * Writes an object to the specified struct. This method should be used by
	 * subclasses to serialize values.
	 */
	protected void writeObject(BitStruct aBitStruct, Object aObject)
	{
		if (aObject instanceof ObjectId.ObjectUID)
		{
			ObjectId.ObjectUID theId = (ObjectId.ObjectUID) aObject;
			aBitStruct.writeLong(theId.getId(), 64);
		}
		else throw new RuntimeException("Not handled: "+aObject);
	}
	
	/**
	 * Returns the number of bits necessary to serialize the given object.
	 */
	protected int getObjectBits(Object aObject)
	{
		if (aObject instanceof ObjectId.ObjectUID)
		{
			return 64;
		}
		else throw new RuntimeException("Not handled: "+aObject);		
	}
	
	/**
	 * Reads an object from the specified struct. This method should be used by
	 * subclasses to deserialize values.
	 */
	protected Object readObject(BitStruct aBitStruct)
	{
		long theId = aBitStruct.readLong(64);
		return new ObjectId.ObjectUID(theId);
	}
	
	/**
	 * Returns the type of this event.
	 */
	public abstract EventType getEventType();
	
	public int getHost()
	{
		return itsHost;
	}

	public int getOperationBytecodeIndex()
	{
		return itsOperationBytecodeIndex;
	}

	public byte[] getParentPointer()
	{
		return itsParentPointer;
	}

	public int getThread()
	{
		return itsThread;
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
		aIndexes.bytecodeLocationIndex.addTuple(getOperationBytecodeIndex(), theStdTuple);
		aIndexes.hostIndex.addTuple(getHost(), theStdTuple);
		aIndexes.threadIndex.addTuple(getThread(), theStdTuple);
	}

	/**
	 * Creates an event from a serialized representation, symmetric to
	 * {@link #writeTo(BitStruct)}.
	 */
	public static GridEvent create(BitStruct aBitStruct)
	{
		EventType theType = EventType.values()[aBitStruct.readInt(DebuggerGridConfig.EVENT_TYPE_BITS)];
		switch (theType)
		{
		case BEHAVIOR_EXIT:
			return new GridBehaviorExitEvent(aBitStruct);
		case CONSTRUCTOR_CHAINING:
			return new GridBehaviorCallEvent(aBitStruct, EventType.CONSTRUCTOR_CHAINING);
		case EXCEPTION_GENERATED:
			return new GridExceptionGeneratedEvent(aBitStruct);
		case FIELD_WRITE:
			return new GridFieldWriteEvent(aBitStruct);
		case INSTANTIATION:
			return new GridBehaviorCallEvent(aBitStruct, EventType.INSTANTIATION);
		case LOCAL_VARIABLE_WRITE:
			return new GridVariableWriteEvent(aBitStruct);
		case METHOD_CALL:
			return new GridBehaviorCallEvent(aBitStruct, EventType.METHOD_CALL);
		default: throw new RuntimeException("Not handled: "+theType); 
		}
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
			return createBehaviorCall(theEvent, EventType.CONSTRUCTOR_CHAINING);
		}
		else if (aEvent instanceof MethodCallEvent)
		{
			MethodCallEvent theEvent = (MethodCallEvent) aEvent;
			return createBehaviorCall(theEvent, EventType.METHOD_CALL);
		}
		else if (aEvent instanceof InstantiationEvent)
		{
			InstantiationEvent theEvent = (InstantiationEvent) aEvent;
			return createBehaviorCall(theEvent, EventType.INSTANTIATION);			
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

	private static GridBehaviorCallEvent createBehaviorCall(BehaviorCallEvent aEvent, EventType aType)
	{
		return new GridBehaviorCallEvent(
				aEvent,
				aType,
				aEvent.isDirectParent(),
				aEvent.getArguments(),
				aEvent.getCalledBehavior() != null ? aEvent.getCalledBehavior().getId() : -1,
				aEvent.getExecutedBehavior() != null ? aEvent.getExecutedBehavior().getId() : -1,
				aEvent.getTarget());
	}
	
	protected static int getObjectId(Object aObject)
	{
		if (aObject instanceof ObjectId.ObjectUID)
		{
			ObjectId.ObjectUID theUid = (ObjectId.ObjectUID) aObject;
			long theId = theUid.getId();
			if ((theId & ~0xffffffffL) != 0) throw new RuntimeException("Object id overflow");
			return (int) theId;
		}
		else throw new RuntimeException("Not handled: "+aObject);
		
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
