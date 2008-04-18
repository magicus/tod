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
import tod.core.database.structure.IStructureDatabase;
import tod.impl.common.event.InstanceOfEvent;
import tod.impl.dbgrid.DebuggerGridConfig;
import tod.impl.dbgrid.GridLogBrowser;
import tod.impl.dbgrid.SplittedConditionHandler;
import tod.impl.dbgrid.db.Indexes;
import tod.impl.dbgrid.db.RoleIndexSet;
import tod.impl.dbgrid.db.StdIndexSet;
import zz.utils.bit.BitStruct;

public class GridInstanceOfEvent extends BitGridEvent
{
	private static final long serialVersionUID = 3623816528718976105L;
	
	private Object itsObject;
	private int itsTypeId;
	private boolean itsResult;

	public GridInstanceOfEvent(IStructureDatabase aStructureDatabase)
	{
		super(aStructureDatabase);
	}

	public GridInstanceOfEvent(
			IStructureDatabase aStructureDatabase,
			int aThread, 
			int aDepth,
			long aTimestamp, 
			int[] aAdviceCFlow,
			int aProbeId,
			long aParentTimestamp,
			Object aObject,
			int aTypeId,
			boolean aResult)
	{
		super(aStructureDatabase);
		set(aThread, aDepth, aTimestamp, aAdviceCFlow, aProbeId, aParentTimestamp, aObject, aTypeId, aResult);
	}

	public GridInstanceOfEvent(IStructureDatabase aStructureDatabase, BitStruct aBitStruct)
	{
		super(aStructureDatabase, aBitStruct);
		itsObject = readObject(aBitStruct);
		itsTypeId = aBitStruct.readInt(DebuggerGridConfig.EVENT_TYPE_BITS);
		itsResult = aBitStruct.readBoolean();
	}

	public void set(
			int aThread, 
			int aDepth,
			long aTimestamp, 
			int[] aAdviceCFlow,
			int aProbeId,
			long aParentTimestamp,
			Object aObject,
			int aTypeId,
			boolean aResult)
	{
		super.set(aThread, aDepth, aTimestamp, aAdviceCFlow, aProbeId, aParentTimestamp);
		itsObject = aObject;
		itsTypeId = aTypeId;
		itsResult = aResult;
	}
	
	@Override
	public void writeTo(BitStruct aBitStruct)
	{
		super.writeTo(aBitStruct);
		writeObject(aBitStruct, getObject());
		aBitStruct.writeInt(getTestedTypeId(), DebuggerGridConfig.EVENT_TYPE_BITS);
		aBitStruct.writeBoolean(itsResult);
	}

	@Override
	public int getBitCount()
	{
		int theCount = super.getBitCount();
		
		theCount += getObjectBits(getObject());
		theCount += DebuggerGridConfig.EVENT_TYPE_BITS;
		theCount += 1;
		
		return theCount;
	}
	
	@Override
	public ILogEvent toLogEvent(GridLogBrowser aBrowser)
	{
		InstanceOfEvent theEvent = new InstanceOfEvent(aBrowser);
		initEvent(aBrowser, theEvent);
		theEvent.setObject(getObject());
		theEvent.setTestedType(getTypeInfo(aBrowser, itsTypeId));
		theEvent.setResult(getResult());
		return theEvent;
	}

	@Override
	public MessageType getEventType()
	{
		return MessageType.INSTANCEOF;
	}

	public int getTestedTypeId()
	{
		return itsTypeId;
	}

	public Object getObject()
	{
		return itsObject;
	}
	
	public boolean getResult()
	{
		return itsResult;
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
				getObject(), 
				ROLE_TUPLE);
	}
	
	@Override
	public boolean matchObjectCondition(int aPart, int aPartialKey, byte aRole)
	{
		return ((aRole == RoleIndexSet.ROLE_OBJECT_TARGET  || aRole == RoleIndexSet.ROLE_OBJECT_ANY)
					&& SplittedConditionHandler.OBJECTS.match(
							aPart, 
							aPartialKey, 
							getObjectId(getObject(), false)));
	}

	@Override
	public String toString()
	{
		return String.format(
				"%s (obj: %s, t: %d, %s)",
				getEventType(),
				itsObject,
				itsTypeId,
				toString0());
	}

}
