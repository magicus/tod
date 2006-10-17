/*
 * Created on Jul 23, 2006
 */
package tod.impl.dbgrid.messages;

import static tod.impl.dbgrid.messages.ObjectCodec.getObjectBits;
import static tod.impl.dbgrid.messages.ObjectCodec.getObjectId;
import static tod.impl.dbgrid.messages.ObjectCodec.readObject;
import static tod.impl.dbgrid.messages.ObjectCodec.writeObject;
import tod.core.database.event.ILogEvent;
import tod.impl.common.event.ExceptionGeneratedEvent;
import tod.impl.dbgrid.DebuggerGridConfig;
import tod.impl.dbgrid.GridLogBrowser;
import tod.impl.dbgrid.dbnode.Indexes;
import tod.impl.dbgrid.dbnode.RoleIndexSet;
import zz.utils.bit.BitStruct;

public class GridExceptionGeneratedEvent extends GridEvent
{
	private static final long serialVersionUID = 7070448347537157710L;
	
	private Object itsException;
	private int itsThrowingBehaviorId;
	
	public GridExceptionGeneratedEvent()
	{
	}

	public GridExceptionGeneratedEvent(
			int aHost, 
			int aThread, 
			int aDepth,
			long aTimestamp, 
			int aOperationBytecodeIndex, 
			long aParentTimestamp,
			Object aException, 
			int aThrowingBehaviorId)
	{
		set(aHost, aThread, aDepth, aTimestamp, aOperationBytecodeIndex, aParentTimestamp, aException, aThrowingBehaviorId);
	}

	public GridExceptionGeneratedEvent(BitStruct aBitStruct)
	{
		super(aBitStruct);

		itsThrowingBehaviorId = aBitStruct.readInt(DebuggerGridConfig.EVENT_BEHAVIOR_BITS);
		itsException = readObject(aBitStruct);
	}
	
	public void set(
			int aHost, 
			int aThread, 
			int aDepth,
			long aTimestamp, 
			int aOperationBytecodeIndex, 
			long aParentTimestamp,
			Object aException, 
			int aThrowingBehaviorId)
	{
		super.set(aHost, aThread, aDepth, aTimestamp, aOperationBytecodeIndex, aParentTimestamp);
		itsException = aException;
		itsThrowingBehaviorId = aThrowingBehaviorId;
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
	public ILogEvent toLogEvent(GridLogBrowser aBrowser)
	{
		ExceptionGeneratedEvent theEvent = new ExceptionGeneratedEvent();
		initEvent(aBrowser, theEvent);
		theEvent.setException(getException());
		theEvent.setThrowingBehavior(aBrowser.getLocationsRepository().getBehavior(getThrowingBehaviorId()));
		return theEvent;
	}
	
	@Override
	public MessageType getEventType()
	{
		return MessageType.EXCEPTION_GENERATED;
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
				getException(), 
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
		assert aObjectId != 0;
		return (aRole == RoleIndexSet.ROLE_OBJECT_EXCEPTION && aObjectId == getObjectId(getException(), false));
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
