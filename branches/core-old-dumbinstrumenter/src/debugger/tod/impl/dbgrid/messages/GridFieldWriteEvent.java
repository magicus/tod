/*
 * Created on Jul 23, 2006
 */
package tod.impl.dbgrid.messages;

import tod.impl.common.event.Event;
import tod.impl.dbgrid.DebuggerGridConfig;
import tod.impl.dbgrid.dbnode.Indexes;
import tod.impl.dbgrid.dbnode.RoleIndexSet;
import tod.impl.dbgrid.dbnode.StdIndexSet;
import zz.utils.bit.BitStruct;
import static tod.impl.dbgrid.messages.ObjectCodec.*;

public class GridFieldWriteEvent extends GridEvent
{
	private int itsFieldId;
	private Object itsTarget;
	private Object itsValue;

	public GridFieldWriteEvent(
			int aHost, 
			int aThread, 
			int aDepth,
			long aTimestamp, 
			int aOperationBytecodeIndex, 
			long aParentTimestamp,
			int aFieldId, 
			Object aTarget, 
			Object aValue)
	{
		super(aHost, aThread, aDepth, aTimestamp, aOperationBytecodeIndex, aParentTimestamp);
		itsFieldId = aFieldId;
		itsTarget = aTarget;
		itsValue = aValue;
	}

	public GridFieldWriteEvent(
			Event aEvent, 
			int aFieldId, 
			Object aTarget, 
			Object aValue)
	{
		super(aEvent);
		itsFieldId = aFieldId;
		itsTarget = aTarget;
		itsValue = aValue;
	}

	public GridFieldWriteEvent(BitStruct aBitStruct)
	{
		super(aBitStruct);

		itsFieldId = aBitStruct.readInt(DebuggerGridConfig.EVENT_FIELD_BITS);
		itsTarget = readObject(aBitStruct);
		itsValue = readObject(aBitStruct);
	}

	@Override
	public void writeTo(BitStruct aBitStruct)
	{
		super.writeTo(aBitStruct);
		aBitStruct.writeInt(getFieldId(), DebuggerGridConfig.EVENT_FIELD_BITS);
		writeObject(aBitStruct, getTarget());
		writeObject(aBitStruct, getValue());
	}

	@Override
	public int getBitCount()
	{
		int theCount = super.getBitCount();
		
		theCount += DebuggerGridConfig.EVENT_FIELD_BITS;
		theCount += getObjectBits(getTarget());
		theCount += getObjectBits(getValue());
		
		return theCount;
	}

	@Override
	public MessageType getEventType()
	{
		return MessageType.FIELD_WRITE;
	}

	public int getFieldId()
	{
		return itsFieldId;
	}

	public Object getTarget()
	{
		return itsTarget;
	}

	public Object getValue()
	{
		return itsValue;
	}

	@Override
	public void index(Indexes aIndexes, long aPointer)
	{
		super.index(aIndexes, aPointer);
		StdIndexSet.StdTuple theStdTuple = new StdIndexSet.StdTuple(getTimestamp(), aPointer);
	
		aIndexes.fieldIndex.addTuple(getFieldId(), theStdTuple);
		
		aIndexes.objectIndex.addTuple(
				getTarget(), 
				new RoleIndexSet.RoleTuple(getTimestamp(), aPointer, RoleIndexSet.ROLE_OBJECT_TARGET));
		
		aIndexes.objectIndex.addTuple(
				getValue(), 
				new RoleIndexSet.RoleTuple(getTimestamp(), aPointer, RoleIndexSet.ROLE_OBJECT_VALUE));
	}
	
	@Override
	public boolean matchFieldCondition(int aFieldId)
	{
		return aFieldId == getFieldId();
	}
	
	@Override
	public boolean matchObjectCondition(int aObjectId, byte aRole)
	{
		assert aObjectId != 0;
		return (aRole == RoleIndexSet.ROLE_OBJECT_VALUE && aObjectId == getObjectId(getValue(), false))
			|| (aRole == RoleIndexSet.ROLE_OBJECT_TARGET && aObjectId == getObjectId(getTarget(), false));
	}

	@Override
	public String toString()
	{
		return String.format(
				"%s (f: %d, tg: %s, v: %s, %s)",
				getEventType(),
				itsFieldId,
				itsTarget,
				itsValue,
				toString0());
	}

}
