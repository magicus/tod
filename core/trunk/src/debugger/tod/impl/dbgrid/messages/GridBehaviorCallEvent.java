/*
 * Created on Jul 23, 2006
 */
package tod.impl.dbgrid.messages;

import java.util.Iterator;

import tod.impl.common.event.Event;
import tod.impl.dbgrid.DebuggerGridConfig;
import tod.impl.dbgrid.dbnode.Indexes;
import tod.impl.dbgrid.dbnode.RoleIndexSet;
import zz.utils.bit.BitStruct;
import zz.utils.bit.IntBitStruct;

public class GridBehaviorCallEvent extends GridEvent
{
	/**
	 * We don't have separate classes for method call,
	 * constructor chaining and instantiation.
	 */
	private byte itsType;
	
	private boolean itsDirectParent;
	private Object[] itsArguments;
	private int itsCalledBehaviorId;
	private int itsExecutedBehaviorId;
	private Object itsTarget;

	
	public GridBehaviorCallEvent(
			int aHost, 
			int aThread,
			long aTimestamp, 
			int aOperationBytecodeIndex,
			byte[] aParentPointer,
			EventType aType, 
			boolean aDirectParent, 
			Object[] aArguments, 
			int aCalledBehaviorId, 
			int aExecutedBehaviorId, 
			Object aTarget)
	{
		super(aHost, aThread, aTimestamp, aOperationBytecodeIndex, aParentPointer);
		itsType = (byte) aType.ordinal();
		itsDirectParent = aDirectParent;
		itsArguments = aArguments;
		itsCalledBehaviorId = aCalledBehaviorId;
		itsExecutedBehaviorId = aExecutedBehaviorId;
		itsTarget = aTarget;
	}

	public GridBehaviorCallEvent(
			Event aEvent,
			EventType aType,
			boolean aDirectParent,
			Object[] aArguments, 
			int aCalledBehaviorId,
			int aExecutedBehaviorId, 
			Object aTarget)
	{
		super(aEvent);
		itsType = (byte) aType.ordinal();
		itsDirectParent = aDirectParent;
		itsArguments = aArguments;
		itsCalledBehaviorId = aCalledBehaviorId;
		itsExecutedBehaviorId = aExecutedBehaviorId;
		itsTarget = aTarget;
	}

	public GridBehaviorCallEvent(IntBitStruct aBitStruct, EventType aType)
	{
		super(aBitStruct);
		itsType = (byte) aType.ordinal();
		
		int theArgsCount = aBitStruct.readInt(DebuggerGridConfig.EVENT_ARGS_COUNT_BITS);
		itsArguments = new Object[theArgsCount];
		for (int i = 0; i < itsArguments.length; i++) itsArguments[i] = readObject(aBitStruct);
		
		itsCalledBehaviorId = aBitStruct.readInt(DebuggerGridConfig.EVENT_BEHAVIOR_BITS);
		itsExecutedBehaviorId = aBitStruct.readInt(DebuggerGridConfig.EVENT_BEHAVIOR_BITS);
		itsDirectParent = aBitStruct.readBoolean();
		itsTarget = readObject(aBitStruct);
	}
	
	@Override
	public void writeTo(IntBitStruct aBitStruct)
	{
		super.writeTo(aBitStruct);
		
		aBitStruct.writeInt(itsArguments.length, DebuggerGridConfig.EVENT_ARGS_COUNT_BITS);
		for (Object theArgument : itsArguments) writeObject(aBitStruct, theArgument);
		
		aBitStruct.writeInt(getCalledBehaviorId(), DebuggerGridConfig.EVENT_BEHAVIOR_BITS);
		aBitStruct.writeInt(getExecutedBehaviorId(), DebuggerGridConfig.EVENT_BEHAVIOR_BITS);
		aBitStruct.writeBoolean(isDirectParent());
		writeObject(aBitStruct, getTarget());
	}
	
	@Override
	public int getBitCount()
	{
		int theCount = super.getBitCount();
		
		theCount += DebuggerGridConfig.EVENT_ARGS_COUNT_BITS;
		for (Object theArgument : itsArguments) theCount += getObjectBits(theArgument);
		
		theCount += DebuggerGridConfig.EVENT_BEHAVIOR_BITS;
		theCount += DebuggerGridConfig.EVENT_BEHAVIOR_BITS;
		theCount += 1;
		theCount += getObjectBits(getTarget());
		
		return theCount;
	}
	
	@Override
	public EventType getEventType()
	{
		return EventType.values()[itsType];
	}

	public Object[] getArguments()
	{
		return itsArguments;
	}

	public int getCalledBehaviorId()
	{
		return itsCalledBehaviorId;
	}

	public boolean isDirectParent()
	{
		return itsDirectParent;
	}

	public int getExecutedBehaviorId()
	{
		return itsExecutedBehaviorId;
	}

	public Object getTarget()
	{
		return itsTarget;
	}

	public byte getType()
	{
		return itsType;
	}
	
	@Override
	public void index(Indexes aIndexes, long aPointer)
	{
		super.index(aIndexes, aPointer);
		
		if (getCalledBehaviorId() != -1)
		{
			aIndexes.behaviorIndex.addTuple(
					getCalledBehaviorId(), 
					new RoleIndexSet.Tuple(getTimestamp(), aPointer, RoleIndexSet.ROLE_BEHAVIOR_CALLED));
		}
		
		if (getExecutedBehaviorId() != -1)
		{
			aIndexes.behaviorIndex.addTuple(
					getExecutedBehaviorId(), 
					new RoleIndexSet.Tuple(getTimestamp(), aPointer, RoleIndexSet.ROLE_BEHAVIOR_EXECUTED));
		}
		
		for (int i = 0; i < itsArguments.length; i++)
		{
			Object theArgument = itsArguments[i];

			aIndexes.objectIndex.addTuple(
					getObjectId(theArgument),
					new RoleIndexSet.Tuple(getTimestamp(), aPointer, (byte) i));
		}
		
		aIndexes.objectIndex.addTuple(
				getObjectId(getTarget()), 
				new RoleIndexSet.Tuple(getTimestamp(), aPointer, RoleIndexSet.ROLE_OBJECT_TARGET));
	}
	
	@Override
	public boolean matchBehaviorCondition(int aBehaviorId, byte aRole)
	{
		return (aRole == RoleIndexSet.ROLE_BEHAVIOR_CALLED && aBehaviorId == getCalledBehaviorId())
			|| (aRole == RoleIndexSet.ROLE_BEHAVIOR_EXECUTED && aBehaviorId == getExecutedBehaviorId())
			|| (aRole == RoleIndexSet.ROLE_BEHAVIOR_ANY 
					&& (aBehaviorId == getExecutedBehaviorId() || aBehaviorId == getCalledBehaviorId()));
	
	}
	
	@Override
	public boolean matchObjectCondition(int aObjectId, byte aRole)
	{
		return (aRole == RoleIndexSet.ROLE_OBJECT_TARGET && aObjectId == getObjectId(getTarget()))
			|| (aRole >= 0 && aRole < getArguments().length && aObjectId == getObjectId(getArguments()[aRole]));
	}
	
	@Override
	public String toString()
	{
		return String.format(
				"%s (cb: %d, dp: %b, eb: %d, tg: %s, %s)",
				getEventType(),
				itsCalledBehaviorId,
				itsDirectParent,
				itsExecutedBehaviorId,
				itsTarget,
				toString0());
	}
}
