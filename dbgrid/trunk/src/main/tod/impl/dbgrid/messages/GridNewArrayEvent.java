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

import static tod.utils.ObjectCodec.getObjectBits;
import static tod.utils.ObjectCodec.getObjectId;
import static tod.utils.ObjectCodec.readObject;
import static tod.utils.ObjectCodec.writeObject;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IArrayTypeInfo;
import tod.core.database.structure.ITypeInfo;
import tod.impl.dbgrid.DebuggerGridConfig;
import tod.impl.dbgrid.GridLogBrowser;
import tod.impl.dbgrid.SplittedConditionHandler;
import tod.impl.dbgrid.db.Indexes;
import tod.impl.dbgrid.db.RoleIndexSet;
import tod.impl.dbgrid.db.StdIndexSet;
import tod.impl.dbgrid.event.InstantiationEvent;
import zz.utils.bit.BitStruct;

public class GridNewArrayEvent extends GridEvent
{
	private static final long serialVersionUID = 6021435584407687823L;

	private Object itsTarget;
	private int itsBaseTypeId;
	private int itsSize;

	public GridNewArrayEvent()
	{
	}

	public GridNewArrayEvent(
			int aThread, 
			int aDepth,
			long aTimestamp, 
			int aOperationBehaviorId,
			int aOperationBytecodeIndex, 
			long aParentTimestamp,
			Object aTarget,
			int aBaseTypeId,
			int aSize)
	{
		set(aThread, aDepth, aTimestamp, aOperationBehaviorId, aOperationBytecodeIndex, aParentTimestamp, aTarget, aBaseTypeId, aSize);
	}

	public GridNewArrayEvent(BitStruct aBitStruct)
	{
		super(aBitStruct);

		itsTarget = readObject(aBitStruct);
		itsBaseTypeId = aBitStruct.readInt(DebuggerGridConfig.EVENT_TYPE_BITS);
		itsSize = aBitStruct.readInt(8);
	}

	public void set(
			int aThread, 
			int aDepth,
			long aTimestamp, 
			int aOperationBehaviorId,
			int aOperationBytecodeIndex, 
			long aParentTimestamp,
			Object aTarget,
			int aBaseTypeId,
			int aSize)
	{
		super.set(aThread, aDepth, aTimestamp, aOperationBehaviorId, aOperationBytecodeIndex, aParentTimestamp);
		itsTarget = aTarget;
		itsBaseTypeId = aBaseTypeId;
		itsSize = aSize;
	}
	
	@Override
	public void writeTo(BitStruct aBitStruct)
	{
		super.writeTo(aBitStruct);
		writeObject(aBitStruct, getTarget());
		aBitStruct.writeInt(getBaseTypeId(), DebuggerGridConfig.EVENT_TYPE_BITS);
		aBitStruct.writeInt(getSize(), 8);
	}

	@Override
	public int getBitCount()
	{
		int theCount = super.getBitCount();
		
		theCount += getObjectBits(getTarget());
		theCount += DebuggerGridConfig.EVENT_TYPE_BITS;
		theCount += 8;
		
		return theCount;
	}
	
	@Override
	public ILogEvent toLogEvent(GridLogBrowser aBrowser)
	{
		InstantiationEvent theEvent = new InstantiationEvent(aBrowser);
		initEvent(aBrowser, theEvent);
		theEvent.setTarget(getTarget());
		
		ITypeInfo theBaseType = aBrowser.getStructureDatabase().getType(getBaseTypeId(), false);
		IArrayTypeInfo theType = aBrowser.getStructureDatabase().getArrayType(theBaseType, 1);

		theEvent.setType(theType);
		theEvent.setArguments(new Object[] { getSize() });
		
		return theEvent;
	}

	@Override
	public MessageType getEventType()
	{
		return MessageType.NEW_ARRAY;
	}

	public int getBaseTypeId()
	{
		return itsBaseTypeId;
	}

	public Object getTarget()
	{
		return itsTarget;
	}

	public int getSize()
	{
		return itsSize;
	}

	private static StdIndexSet.StdTuple STD_TUPLE = new StdIndexSet.StdTuple(-1, -1);
	private static RoleIndexSet.RoleTuple ROLE_TUPLE = new RoleIndexSet.RoleTuple(-1, -1, -1);
	
	@Override
	public void index(Indexes aIndexes, long aPointer)
	{
		super.index(aIndexes, aPointer);
		STD_TUPLE.set(getTimestamp(), aPointer);
	
		ROLE_TUPLE.set(getTimestamp(), aPointer, RoleIndexSet.ROLE_OBJECT_TARGET);
		aIndexes.indexObject(
				getTarget(), 
				ROLE_TUPLE);
	}
	
	@Override
	public boolean matchObjectCondition(int aPart, int aPartialKey, byte aRole)
	{
		return ((aRole == RoleIndexSet.ROLE_OBJECT_TARGET  || aRole == RoleIndexSet.ROLE_OBJECT_ANY)
					&& SplittedConditionHandler.OBJECTS.match(
							aPart, 
							aPartialKey, 
							getObjectId(getTarget(), false)));
	}

	@Override
	public String toString()
	{
		return String.format(
				"%s (tg: %s, bt: %d, d: %d, %s)",
				getEventType(),
				itsTarget,
				itsBaseTypeId,
				itsSize,
				toString0());
	}

}