/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier (gpothier@dcc.uchile.cl)

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

Parts of this work rely on the MD5 algorithm "derived from the 
RSA Data Security, Inc. MD5 Message-Digest Algorithm".
*/
package tod.impl.dbgrid.messages;

import static tod.impl.dbgrid.ObjectCodec.getObjectBits;
import static tod.impl.dbgrid.ObjectCodec.getObjectId;
import static tod.impl.dbgrid.ObjectCodec.readObject;
import static tod.impl.dbgrid.ObjectCodec.writeObject;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IStructureDatabase.LocalVariableInfo;
import tod.impl.common.event.LocalVariableWriteEvent;
import tod.impl.dbgrid.DebuggerGridConfig;
import tod.impl.dbgrid.GridLogBrowser;
import tod.impl.dbgrid.SplittedConditionHandler;
import tod.impl.dbgrid.db.Indexes;
import tod.impl.dbgrid.db.RoleIndexSet;
import tod.impl.dbgrid.db.StdIndexSet;
import zz.utils.bit.BitStruct;

public class GridVariableWriteEvent extends GridEvent
{
	private static final long serialVersionUID = 5600466618091824186L;
	
	private int itsVariableId;
	private Object itsValue;
	
	public GridVariableWriteEvent()
	{
	}

	public GridVariableWriteEvent(
			int aThread, 
			int aDepth,
			long aTimestamp, 
			int aOperationBehaviorId,
			int aOperationBytecodeIndex, 
			int aAdviceSourceId,
			long aParentTimestamp,
			int aVariableId, 
			Object aValue)
	{
		set(aThread, aDepth, aTimestamp, aOperationBehaviorId, aOperationBytecodeIndex, aAdviceSourceId, aParentTimestamp, aVariableId, aValue);
	}


	public GridVariableWriteEvent(BitStruct aBitStruct)
	{
		super(aBitStruct);

		itsVariableId = aBitStruct.readInt(DebuggerGridConfig.EVENT_VARIABLE_BITS);
		
		// TODO: this is a hack. We should not allow negative values.
		if (itsVariableId == 0xffff) itsVariableId = -1;
		
		itsValue = readObject(aBitStruct);
	}

	public void set(
			int aThread, 
			int aDepth,
			long aTimestamp, 
			int aOperationBehaviorId,
			int aOperationBytecodeIndex, 
			int aAdviceSourceId,
			long aParentTimestamp,
			int aVariableId, 
			Object aValue)
	{
		super.set(aThread, aDepth, aTimestamp, aOperationBehaviorId, aOperationBytecodeIndex, aAdviceSourceId, aParentTimestamp);
		itsVariableId = aVariableId;
		itsValue = aValue;
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
	public ILogEvent toLogEvent(GridLogBrowser aBrowser)
	{
		LocalVariableWriteEvent theEvent = new LocalVariableWriteEvent(aBrowser);
		initEvent(aBrowser, theEvent);
		theEvent.setValue(getValue());
		
		IBehaviorInfo theBehavior = theEvent.getOperationBehavior();
		
		LocalVariableInfo theInfo = theBehavior.getLocalVariableInfo(
				getOperationBytecodeIndex(), 
				getVariableId());
		
       	if (theInfo == null) theInfo = new LocalVariableInfo(
       			(short)-1, 
       			(short)-1, 
       			"$"+getVariableId(), 
       			"", 
       			(short)-1);

		theEvent.setVariable(theInfo); 
		
		return theEvent;
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
	
	private static StdIndexSet.StdTuple STD_TUPLE = new StdIndexSet.StdTuple(-1, -1);
	private static RoleIndexSet.RoleTuple ROLE_TUPLE = new RoleIndexSet.RoleTuple(-1, -1, -1);
	
	@Override
	public void index(Indexes aIndexes, long aPointer)
	{
		super.index(aIndexes, aPointer);
		STD_TUPLE.set(getTimestamp(), aPointer);
		
		// TODO: this should not be necessary, we should not have negative values.
		if (getVariableId() >= 0)
		{
			aIndexes.indexVariable(getVariableId(), STD_TUPLE);
		}

		ROLE_TUPLE.set(getTimestamp(), aPointer, RoleIndexSet.ROLE_OBJECT_VALUE);
		aIndexes.indexObject(
				getValue(), 
				ROLE_TUPLE);
	}
	
	@Override
	public boolean matchVariableCondition(int aVariableId)
	{
		return aVariableId == getVariableId();
	}
	
	@Override
	public boolean matchObjectCondition(int aPart, int aPartialKey, byte aRole)
	{
		return ((aRole == RoleIndexSet.ROLE_OBJECT_VALUE || aRole == RoleIndexSet.ROLE_OBJECT_ANY)
					&& SplittedConditionHandler.OBJECTS.match(
							aPart, 
							aPartialKey, 
							getObjectId(getValue(), false)));
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
