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

public class GridVariableWriteEvent extends GridEvent
{
	private int itsVariableId;
	private Object itsValue;
	
	public GridVariableWriteEvent(
			int aHost, 
			int aThread, 
			int aDepth,
			long aTimestamp, 
			int aOperationBytecodeIndex, 
			byte[] aParentPointer, 
			int aVariableId, 
			Object aValue)
	{
		super(aHost, aThread, aDepth, aTimestamp, aOperationBytecodeIndex, aParentPointer);
		itsVariableId = aVariableId;
		itsValue = aValue;
	}


	public GridVariableWriteEvent(Event aEvent, int aVariableId, Object aValue)
	{
		super(aEvent);
		itsVariableId = aVariableId;
		itsValue = aValue;
	}


	public GridVariableWriteEvent(BitStruct aBitStruct)
	{
		super(aBitStruct);

		itsVariableId = aBitStruct.readInt(DebuggerGridConfig.EVENT_VARIABLE_BITS);
		itsValue = readObject(aBitStruct);
	}

	@Override
	public void writeTo(BitStruct aBitStruct)
	{
		super.writeTo(aBitStruct);
		aBitStruct.writeInt(getVariableId(), DebuggerGridConfig.EVENT_VARIABLE_BITS);
		writeObject(aBitStruct, getValue());
	}

	@Override
	public int getBitCount()
	{
		int theCount = super.getBitCount();
		
		theCount += DebuggerGridConfig.EVENT_VARIABLE_BITS;
		theCount += getObjectBits(getValue());
		
		return theCount;
	}

	@Override
	public MessageType getEventType()
	{
		return MessageType.LOCAL_VARIABLE_WRITE;
	}


	public int getVariableId()
	{
		return itsVariableId;
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
		
		aIndexes.variableIndex.addTuple(getVariableId(), theStdTuple); 

		aIndexes.objectIndex.addTuple(
				getValue(), 
				new RoleIndexSet.RoleTuple(getTimestamp(), aPointer, RoleIndexSet.ROLE_OBJECT_VALUE));
	}
	
	@Override
	public boolean matchVariableCondition(int aVariableId)
	{
		return aVariableId == getVariableId();
	}
	
	@Override
	public boolean matchObjectCondition(int aObjectId, byte aRole)
	{
		assert aObjectId != 0;
		return (aRole == RoleIndexSet.ROLE_OBJECT_VALUE && aObjectId == getObjectId(getValue(), false));
	}

	@Override
	public String toString()
	{
		return String.format(
				"%s (val: %s, var: %d, %s)",
				getEventType(),
				itsValue,
				itsVariableId,
				toString0());
	}

}
