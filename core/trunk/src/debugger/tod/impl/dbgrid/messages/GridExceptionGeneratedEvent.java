/*
 * Created on Jul 23, 2006
 */
package tod.impl.dbgrid.messages;

import tod.impl.common.event.Event;
import tod.impl.dbgrid.DebuggerGridConfig;
import tod.impl.dbgrid.dbnode.Indexes;
import tod.impl.dbgrid.dbnode.RoleIndexSet;
import zz.utils.BitStruct;

public class GridExceptionGeneratedEvent extends GridEvent
{
	private Object itsException;
	private int itsThrowingBehaviorId;
	
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
				new RoleIndexSet.Tuple(getTimestamp(), aPointer, RoleIndexSet.ROLE_BEHAVIOR_EXECUTED));
		
		aIndexes.objectIndex.addTuple(
				getException(), 
				new RoleIndexSet.Tuple(getTimestamp(), aPointer, RoleIndexSet.ROLE_OBJECT_EXCEPTION));
	}
	

}
