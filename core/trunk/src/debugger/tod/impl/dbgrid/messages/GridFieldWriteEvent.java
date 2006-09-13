/*
 * Created on Jul 23, 2006
 */
package tod.impl.dbgrid.messages;

import static tod.impl.dbgrid.messages.ObjectCodec.getObjectBits;
import static tod.impl.dbgrid.messages.ObjectCodec.getObjectId;
import static tod.impl.dbgrid.messages.ObjectCodec.readObject;
import static tod.impl.dbgrid.messages.ObjectCodec.writeObject;
import tod.core.database.event.ILogEvent;
import tod.impl.common.event.FieldWriteEvent;
import tod.impl.dbgrid.DebuggerGridConfig;
import tod.impl.dbgrid.GridLogBrowser;
import tod.impl.dbgrid.dbnode.Indexes;
import tod.impl.dbgrid.dbnode.RoleIndexSet;
import tod.impl.dbgrid.dbnode.StdIndexSet;
import zz.utils.bit.BitStruct;

public class GridFieldWriteEvent extends GridEvent
{
	private int itsFieldId;
	private Object itsTarget;
	private Object itsValue;

	public GridFieldWriteEvent()
	{
	}

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
		set(aHost, aThread, aDepth, aTimestamp, aOperationBytecodeIndex, aParentTimestamp, aFieldId, aTarget, aValue);
	}

	public GridFieldWriteEvent(BitStruct aBitStruct)
	{
		super(aBitStruct);

		itsFieldId = aBitStruct.readInt(DebuggerGridConfig.EVENT_FIELD_BITS);
		itsTarget = readObject(aBitStruct);
		itsValue = readObject(aBitStruct);
	}

	public void set(
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
		super.set(aHost, aThread, aDepth, aTimestamp, aOperationBytecodeIndex, aParentTimestamp);
		itsFieldId = aFieldId;
		itsTarget = aTarget;
		itsValue = aValue;
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
	public ILogEvent toLogEvent(GridLogBrowser aBrowser)
	{
		FieldWriteEvent theEvent = new FieldWriteEvent();
		initEvent(aBrowser, theEvent);
		theEvent.setField(aBrowser.getLocationsRepository().getField(getFieldId()));
		theEvent.setTarget(getTarget());
		theEvent.setValue(getValue());
		return theEvent;
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
