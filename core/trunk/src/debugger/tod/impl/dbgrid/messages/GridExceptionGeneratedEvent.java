/*
 * Created on Jul 23, 2006
 */
package tod.impl.dbgrid.messages;

import tod.impl.common.event.Event;
import tod.impl.dbgrid.DebuggerGridConfig;
import tod.impl.dbgrid.dbnode.Indexes;
import tod.impl.dbgrid.dbnode.RoleIndexSet;
import zz.utils.bit.BitStruct;

public class GridExceptionGeneratedEvent extends GridEvent
{
	private Object itsException;
	private int itsThrowingBehaviorId;
	
	public GridExceptionGeneratedEvent(
			int aHost, 
			int aThread, 
			long aTimestamp, 
			int aOperationBytecodeIndex, 
			byte[] aParentPointer, 
			Object aException, 
			int aThrowingBehaviorId)
	{
		super(aHost, aThread, aTimestamp, aOperationBytecodeIndex, aParentPointer);
		itsException = aException;
		itsThrowingBehaviorId = aThrowingBehaviorId;
	}

	public GridExceptionGeneratedEvent(
			Event aEvent,
			Object aException, 
			int aThrowingBehaviorId)
	{
		super(aEvent);
		itsException = aException;
		itsThrowingBehaviorId = aThrowingBehaviorId;
	}
	
	public GridExceptionGeneratedEvent(BitStruct aBitStruct)
	{
		super(aBitStruct);

		itsThrowingBehaviorId = aBitStruct.readInt(DebuggerGridConfig.EVENT_BEHAVIOR_BITS);
		itsException = readObject(aBitStruct);
	}

	@Override
	public void writeTo(BitStruct aBitStruct)
	{
		super.writeTo(aBitStruct);
		aBitStruct.writeInt(getThrowingBehaviorId(), DebuggerGridConfig.EVENT_BEHAVIOR_BITS);
		writeObject(aBitStruct, getException());
	}

	@Override
	public int getBitCount()
	{
		int theCount = super.getBitCount();
		
		theCount += DebuggerGridConfig.EVENT_BEHAVIOR_BITS;
		theCount += getObjectBits(getException());

		return theCount;
	}
	
	@Override
	public EventType getEventType()
	{
		return EventType.EXCEPTION_GENERATED;
	}

	public Object getException()
	{
		return itsException;
	}
	
	public int getThrowingBehaviorId()
	{
		return itsThrowingBehaviorId;
	}
	
	@Override
	public void index(Indexes aIndexes, long aPointer)
	{
		super.index(aIndexes, aPointer);
		
		aIndexes.behaviorIndex.addTuple(
				getThrowingBehaviorId(),
				new RoleIndexSet.RoleTuple(getTimestamp(), aPointer, RoleIndexSet.ROLE_BEHAVIOR_EXECUTED));
		
		aIndexes.objectIndex.addTuple(
				getObjectId(getException()), 
				new RoleIndexSet.RoleTuple(getTimestamp(), aPointer, RoleIndexSet.ROLE_OBJECT_EXCEPTION));
	}
	
	@Override
	public boolean matchBehaviorCondition(int aBehaviorId, byte aRole)
	{
		return (aRole == RoleIndexSet.ROLE_BEHAVIOR_EXECUTED || aRole == RoleIndexSet.ROLE_BEHAVIOR_ANY)
			&& (aBehaviorId == getThrowingBehaviorId());			
	}
	
	@Override
	public boolean matchObjectCondition(int aObjectId, byte aRole)
	{
		return (aRole == RoleIndexSet.ROLE_OBJECT_EXCEPTION && aObjectId == getObjectId(getException()));
	}

	@Override
	public String toString()
	{
		return String.format(
				"%s (ex: %s, b: %d, %s)",
				getEventType(),
				itsException,
				itsThrowingBehaviorId,
				toString0());
	}
}
