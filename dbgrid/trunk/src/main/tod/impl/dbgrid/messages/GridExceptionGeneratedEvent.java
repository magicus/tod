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
import tod.impl.common.event.ExceptionGeneratedEvent;
import tod.impl.dbgrid.DebuggerGridConfig;
import tod.impl.dbgrid.GridLogBrowser;
import tod.impl.dbgrid.SplittedConditionHandler;
import tod.impl.dbgrid.db.Indexes;
import tod.impl.dbgrid.db.RoleIndexSet;
import zz.utils.bit.BitStruct;

public class GridExceptionGeneratedEvent extends GridEvent
{
	private static final long serialVersionUID = 7070448347537157710L;
	
	private Object itsException;
	
	public GridExceptionGeneratedEvent()
	{
	}

	public GridExceptionGeneratedEvent(
			int aThread, 
			int aDepth,
			long aTimestamp, 
			int aOperationBehaviorId,
			int aOperationBytecodeIndex, 
			long aParentTimestamp,
			Object aException)
	{
		set(aThread, aDepth, aTimestamp, aOperationBehaviorId, aOperationBytecodeIndex, aParentTimestamp, aException);
	}

	public GridExceptionGeneratedEvent(BitStruct aBitStruct)
	{
		super(aBitStruct);

		itsException = readObject(aBitStruct);
	}
	
	public void set(
			int aThread, 
			int aDepth,
			long aTimestamp, 
			int aOperationBehaviorId,
			int aOperationBytecodeIndex, 
			long aParentTimestamp,
			Object aException)
	{
		super.set(aThread, aDepth, aTimestamp, aOperationBehaviorId, aOperationBytecodeIndex, aParentTimestamp);
		itsException = aException;
	}

	@Override
	public void writeTo(BitStruct aBitStruct)
	{
		super.writeTo(aBitStruct);
		writeObject(aBitStruct, getException());
	}

	@Override
	public int getBitCount()
	{
		int theCount = super.getBitCount();
		
		theCount += getObjectBits(getException());

		return theCount;
	}
	
	@Override
	public ILogEvent toLogEvent(GridLogBrowser aBrowser)
	{
		ExceptionGeneratedEvent theEvent = new ExceptionGeneratedEvent(aBrowser);
		initEvent(aBrowser, theEvent);
		theEvent.setException(getException());
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
	
	private static RoleIndexSet.RoleTuple TUPLE = new RoleIndexSet.RoleTuple(-1, -1, -1);
	
	@Override
	public void index(Indexes aIndexes, long aPointer)
	{
		super.index(aIndexes, aPointer);
				
		TUPLE.set(getTimestamp(), aPointer, RoleIndexSet.ROLE_OBJECT_EXCEPTION);
		aIndexes.indexObject(
				getException(), 
				TUPLE);
	}
	
	@Override
	public boolean matchBehaviorCondition(int aBehaviorId, byte aRole)
	{
		return (aRole == RoleIndexSet.ROLE_BEHAVIOR_OPERATION) 
			&& (aBehaviorId == getOperationBehaviorId());			
	}
	
	@Override
	public boolean matchObjectCondition(int aPart, int aPartialKey, byte aRole)
	{
		return ((aRole == RoleIndexSet.ROLE_OBJECT_EXCEPTION || aRole == RoleIndexSet.ROLE_OBJECT_ANY) 
				&& SplittedConditionHandler.OBJECTS.match(
						aPart, 
						aPartialKey, 
						getObjectId(getException(), false)));
	}

	@Override
	public String toString()
	{
		return String.format(
				"%s (ex: %s, b: %d, %s)",
				getEventType(),
				itsException,
				getOperationBehaviorId(),
				toString0());
	}
}
