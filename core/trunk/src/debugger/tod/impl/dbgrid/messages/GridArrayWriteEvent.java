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

import static tod.impl.dbgrid.messages.ObjectCodec.getObjectBits;
import static tod.impl.dbgrid.messages.ObjectCodec.getObjectId;
import static tod.impl.dbgrid.messages.ObjectCodec.readObject;
import static tod.impl.dbgrid.messages.ObjectCodec.writeObject;
import tod.core.database.event.ILogEvent;
import tod.impl.common.event.ArrayWriteEvent;
import tod.impl.dbgrid.GridLogBrowser;
import tod.impl.dbgrid.dbnode.Indexes;
import tod.impl.dbgrid.dbnode.RoleIndexSet;
import tod.impl.dbgrid.dbnode.StdIndexSet;
import zz.utils.bit.BitStruct;

public class GridArrayWriteEvent extends GridEvent
{
	private static final long serialVersionUID = 3605816555618929935L;
	
	private Object itsTarget;
	private int itsIndex;
	private Object itsValue;

	public GridArrayWriteEvent()
	{
	}

	public GridArrayWriteEvent(
			int aHost, 
			int aThread, 
			int aDepth,
			long aTimestamp, 
			int aOperationBytecodeIndex, 
			long aParentTimestamp,
			Object aTarget,
			int aIndex,
			Object aValue)
	{
		set(aHost, aThread, aDepth, aTimestamp, aOperationBytecodeIndex, aParentTimestamp, aTarget, aIndex, aValue);
	}

	public GridArrayWriteEvent(BitStruct aBitStruct)
	{
		super(aBitStruct);

		itsTarget = readObject(aBitStruct);
		itsIndex = aBitStruct.readInt(32);
		itsValue = readObject(aBitStruct);
	}

	public void set(
			int aHost, 
			int aThread, 
			int aDepth,
			long aTimestamp, 
			int aOperationBytecodeIndex, 
			long aParentTimestamp,
			Object aTarget,
			int aIndex,
			Object aValue)
	{
		super.set(aHost, aThread, aDepth, aTimestamp, aOperationBytecodeIndex, aParentTimestamp);
		itsTarget = aTarget;
		itsIndex = aIndex;
		itsValue = aValue;
	}
	
	@Override
	public void writeTo(BitStruct aBitStruct)
	{
		super.writeTo(aBitStruct);
		writeObject(aBitStruct, getTarget());
		aBitStruct.writeInt(getIndex(), 32);
		writeObject(aBitStruct, getValue());
	}

	@Override
	public int getBitCount()
	{
		int theCount = super.getBitCount();
		
		theCount += getObjectBits(getTarget());
		theCount += 32;
		theCount += getObjectBits(getValue());
		
		return theCount;
	}
	
	@Override
	public ILogEvent toLogEvent(GridLogBrowser aBrowser)
	{
		ArrayWriteEvent theEvent = new ArrayWriteEvent();
		initEvent(aBrowser, theEvent);
		theEvent.setTarget(getTarget());
		theEvent.setIndex(itsIndex);
		theEvent.setValue(getValue());
		return theEvent;
	}

	@Override
	public MessageType getEventType()
	{
		return MessageType.ARRAY_WRITE;
	}

	public int getIndex()
	{
		return itsIndex;
	}

	public Object getTarget()
	{
		return itsTarget;
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
	
		ROLE_TUPLE.set(getTimestamp(), aPointer, RoleIndexSet.ROLE_OBJECT_TARGET);
		aIndexes.objectIndex.addTuple(
				getTarget(), 
				ROLE_TUPLE);
		
		aIndexes.indexIndex.addTuple(getIndex(), STD_TUPLE);
		
		ROLE_TUPLE.set(getTimestamp(), aPointer, RoleIndexSet.ROLE_OBJECT_VALUE);
		aIndexes.objectIndex.addTuple(
				getValue(), 
				ROLE_TUPLE);
	}
	
	@Override
	public boolean matchIndexCondition(int aIndex)
	{
		return aIndex == itsIndex;
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
				"%s (tg: %s, i: %d, v: %s, %s)",
				getEventType(),
				itsTarget,
				itsIndex,
				itsValue,
				toString0());
	}

}
