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
import tod.impl.common.event.BehaviorExitEvent;
import tod.impl.dbgrid.DebuggerGridConfig;
import tod.impl.dbgrid.GridLogBrowser;
import tod.impl.dbgrid.dbnode.Indexes;
import tod.impl.dbgrid.dbnode.RoleIndexSet;
import zz.utils.bit.BitStruct;

public class GridBehaviorExitEvent extends GridEvent
{
	private static final long serialVersionUID = -5809462388785867681L;
	
	private boolean itsHasThrown;
	private Object itsResult;
	
	/**
	 * We need to specify the behavior id for indexing.
	 * This will be the called behavior if the executed behavior is not
	 * available.
	 */
	private int itsBehaviorId;

	
	
	public GridBehaviorExitEvent()
	{
	}

	public GridBehaviorExitEvent(
			int aHost, 
			int aThread, 
			int aDepth,
			long aTimestamp, 
			int aOperationBytecodeIndex, 
			long aParentTimestamp,
			boolean aHasThrown, 
			Object aResult, 
			int aBehaviorId)
	{
		set(aHost, aThread, aDepth, aTimestamp, aOperationBytecodeIndex, aParentTimestamp, aHasThrown, aResult, aBehaviorId);
	}

	public GridBehaviorExitEvent(BitStruct aBitStruct)
	{
		super(aBitStruct);
		itsBehaviorId = aBitStruct.readInt(DebuggerGridConfig.EVENT_BEHAVIOR_BITS);
		itsHasThrown = aBitStruct.readBoolean();
		itsResult = readObject(aBitStruct);
	}

	public void set(
			int aHost, 
			int aThread, 
			int aDepth,
			long aTimestamp, 
			int aOperationBytecodeIndex, 
			long aParentTimestamp,
			boolean aHasThrown, 
			Object aResult, 
			int aBehaviorId)
	{
		super.set(aHost, aThread, aDepth, aTimestamp, aOperationBytecodeIndex, aParentTimestamp);
		itsHasThrown = aHasThrown;
		itsResult = aResult;
		itsBehaviorId = aBehaviorId;
	}
	
	@Override
	public void writeTo(BitStruct aBitStruct)
	{
		super.writeTo(aBitStruct);
		aBitStruct.writeInt(getBehaviorId(), DebuggerGridConfig.EVENT_BEHAVIOR_BITS);
		aBitStruct.writeBoolean(hasThrown());
		writeObject(aBitStruct, getResult());
	}
	
	@Override
	public int getBitCount()
	{
		int theCount = super.getBitCount();
		
		theCount += DebuggerGridConfig.EVENT_BEHAVIOR_BITS;
		theCount += 1;
		theCount += getObjectBits(getResult());
		
		return theCount;
	}

	@Override
	public ILogEvent toLogEvent(GridLogBrowser aBrowser)
	{
		BehaviorExitEvent theEvent = new BehaviorExitEvent();
		initEvent(aBrowser, theEvent);
		theEvent.setHasThrown(hasThrown());
		theEvent.setResult(getResult());
		return theEvent;
	}
	
	@Override
	public MessageType getEventType()
	{
		return MessageType.BEHAVIOR_EXIT;
	}

	public int getBehaviorId()
	{
		return itsBehaviorId;
	}

	public boolean hasThrown()
	{
		return itsHasThrown;
	}

	public Object getResult()
	{
		return itsResult;
	}
	
	private static RoleIndexSet.RoleTuple TUPLE = new RoleIndexSet.RoleTuple(-1, -1, -1);
	
	@Override
	public void index(Indexes aIndexes, long aPointer)
	{
		super.index(aIndexes, aPointer);
		
		TUPLE.set(getTimestamp(), aPointer, RoleIndexSet.ROLE_BEHAVIOR_EXIT);
		aIndexes.behaviorIndex.addTuple(
				getBehaviorId(), 
				TUPLE);
		
		TUPLE.set(getTimestamp(), aPointer, RoleIndexSet.ROLE_OBJECT_RESULT);
		aIndexes.objectIndex.addTuple(
				getResult(), 
				TUPLE);
	}

	@Override
	public boolean matchBehaviorCondition(int aBehaviorId, byte aRole)
	{
		return (aRole == RoleIndexSet.ROLE_BEHAVIOR_EXIT || aRole == RoleIndexSet.ROLE_BEHAVIOR_ANY)
			&& (aBehaviorId == getBehaviorId());
	}
	
	@Override
	public boolean matchObjectCondition(int aObjectId, byte aRole)
	{
		assert aObjectId != 0;
		return (aRole == RoleIndexSet.ROLE_OBJECT_RESULT && aObjectId == getObjectId(getResult(), false));
	}
	
	@Override
	public String toString()
	{
		return String.format(
				"%s (b: %d, ht: %b, r: %s, %s)",
				getEventType(),
				itsBehaviorId,
				itsHasThrown,
				itsResult,
				toString0());
	}

}
