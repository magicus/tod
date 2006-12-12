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

import java.io.Serializable;

import tod.core.database.event.ILogEvent;
import tod.impl.common.event.BehaviorCallEvent;
import tod.impl.common.event.ConstructorChainingEvent;
import tod.impl.common.event.InstantiationEvent;
import tod.impl.common.event.MethodCallEvent;
import tod.impl.dbgrid.DebuggerGridConfig;
import tod.impl.dbgrid.GridLogBrowser;
import tod.impl.dbgrid.dbnode.Indexes;
import tod.impl.dbgrid.dbnode.RoleIndexSet;
import zz.utils.bit.BitStruct;

public class GridBehaviorCallEvent extends GridEvent
{
	private static final long serialVersionUID = -6294318569339045898L;

	/**
	 * We don't have separate classes for method call,
	 * constructor chaining and instantiation.
	 */
	private byte itsType;
	
	private boolean itsDirectParent;
	private Object[] itsArguments;
	private int itsCalledBehaviorId;
	private int itsExecutedBehaviorId;
	private Object itsTarget;

	
	public GridBehaviorCallEvent()
	{
	}

	public GridBehaviorCallEvent(
			int aHost, 
			int aThread,
			int aDepth,
			long aTimestamp, 
			int aOperationBytecodeIndex,
			long aParentTimestamp,
			MessageType aType, 
			boolean aDirectParent, 
			Object[] aArguments, 
			int aCalledBehaviorId, 
			int aExecutedBehaviorId, 
			Object aTarget)
	{
		set(aHost, aThread, aDepth, aTimestamp, aOperationBytecodeIndex, aParentTimestamp, aType, aDirectParent, aArguments, aCalledBehaviorId, aExecutedBehaviorId, aTarget);
	}

	public GridBehaviorCallEvent(BitStruct aBitStruct, MessageType aType)
	{
		super(aBitStruct);
		itsType = (byte) aType.ordinal();
		
		int theArgsCount = aBitStruct.readInt(DebuggerGridConfig.EVENT_ARGS_COUNT_BITS);
		itsArguments = new Object[theArgsCount];
		for (int i = 0; i < itsArguments.length; i++) itsArguments[i] = readObject(aBitStruct);
		
		itsCalledBehaviorId = aBitStruct.readInt(DebuggerGridConfig.EVENT_BEHAVIOR_BITS);
		itsExecutedBehaviorId = aBitStruct.readInt(DebuggerGridConfig.EVENT_BEHAVIOR_BITS);
		itsDirectParent = aBitStruct.readBoolean();
		itsTarget = readObject(aBitStruct);
	}
	
	public void set(
			int aHost, 
			int aThread,
			int aDepth,
			long aTimestamp, 
			int aOperationBytecodeIndex,
			long aParentTimestamp,
			MessageType aType, 
			boolean aDirectParent, 
			Object[] aArguments, 
			int aCalledBehaviorId, 
			int aExecutedBehaviorId, 
			Object aTarget)
	{
		super.set(aHost, aThread, aDepth, aTimestamp, aOperationBytecodeIndex, aParentTimestamp);
		itsType = (byte) aType.ordinal();
		itsDirectParent = aDirectParent;
		itsArguments = aArguments;
		itsCalledBehaviorId = aCalledBehaviorId;
		itsExecutedBehaviorId = aExecutedBehaviorId;
		itsTarget = aTarget;
	}
	
	@Override
	public void writeTo(BitStruct aBitStruct)
	{
		super.writeTo(aBitStruct);
		
		aBitStruct.writeInt(itsArguments.length, DebuggerGridConfig.EVENT_ARGS_COUNT_BITS);
		for (Object theArgument : itsArguments) writeObject(aBitStruct, theArgument);
		
		aBitStruct.writeInt(getCalledBehaviorId(), DebuggerGridConfig.EVENT_BEHAVIOR_BITS);
		aBitStruct.writeInt(getExecutedBehaviorId(), DebuggerGridConfig.EVENT_BEHAVIOR_BITS);
		aBitStruct.writeBoolean(isDirectParent());
		writeObject(aBitStruct, getTarget());
	}
	
	@Override
	public int getBitCount()
	{
		int theCount = super.getBitCount();
		
		theCount += DebuggerGridConfig.EVENT_ARGS_COUNT_BITS;
		for (Object theArgument : itsArguments) theCount += getObjectBits(theArgument);
		
		theCount += DebuggerGridConfig.EVENT_BEHAVIOR_BITS;
		theCount += DebuggerGridConfig.EVENT_BEHAVIOR_BITS;
		theCount += 1;
		theCount += getObjectBits(getTarget());
		
		return theCount;
	}
	
	@Override
	public ILogEvent toLogEvent(GridLogBrowser aBrowser)
	{
		BehaviorCallEvent theEvent;
		
		switch(getEventType()) 
		{
		case METHOD_CALL:
			theEvent = new MethodCallEvent();
			break;
			
		case INSTANTIATION:
			theEvent = new InstantiationEvent();
			break;
			
		case SUPER_CALL:
			theEvent = new ConstructorChainingEvent();
			break;
			
		default:
			throw new RuntimeException("Not handled: "+this);
		}
		
		initEvent(aBrowser, theEvent);
		theEvent.setArguments(getArguments());
		theEvent.setCalledBehavior(aBrowser.getLocationsRepository().getBehavior(getCalledBehaviorId()));
		theEvent.setExecutedBehavior(aBrowser.getLocationsRepository().getBehavior(getExecutedBehaviorId()));
		theEvent.setDirectParent(isDirectParent());
		theEvent.setTarget(getTarget());
		
		return theEvent;
	}
	
	@Override
	public MessageType getEventType()
	{
		return MessageType.values()[itsType];
	}

	public Object[] getArguments()
	{
		return itsArguments;
	}

	public int getCalledBehaviorId()
	{
		return itsCalledBehaviorId;
	}

	public boolean isDirectParent()
	{
		return itsDirectParent;
	}

	public int getExecutedBehaviorId()
	{
		return itsExecutedBehaviorId;
	}

	public Object getTarget()
	{
		return itsTarget;
	}

	public byte getType()
	{
		return itsType;
	}
	
	private static RoleIndexSet.RoleTuple TUPLE = new RoleIndexSet.RoleTuple(-1, -1, -1);
	
	@Override
	public void index(Indexes aIndexes, long aPointer)
	{
		super.index(aIndexes, aPointer);
		
		if (getCalledBehaviorId() != -1)
		{
			TUPLE.set(getTimestamp(), aPointer, RoleIndexSet.ROLE_BEHAVIOR_CALLED);
			aIndexes.behaviorIndex.addTuple(
					getCalledBehaviorId(), 
					TUPLE);
		}
		
		if (getExecutedBehaviorId() != -1)
		{
			TUPLE.set(getTimestamp(), aPointer, RoleIndexSet.ROLE_BEHAVIOR_EXECUTED);
			aIndexes.behaviorIndex.addTuple(
					getExecutedBehaviorId(), 
					TUPLE);
		}
		
		for (int i = 0; i < itsArguments.length; i++)
		{
			Object theArgument = itsArguments[i];

			TUPLE.set(getTimestamp(), aPointer, (byte) i);
			aIndexes.objectIndex.addTuple(
					theArgument,
					TUPLE);
		}
		
		TUPLE.set(getTimestamp(), aPointer, RoleIndexSet.ROLE_OBJECT_TARGET);
		aIndexes.objectIndex.addTuple(
				getTarget(), 
				TUPLE);
	}
	
	@Override
	public boolean matchBehaviorCondition(int aBehaviorId, byte aRole)
	{
		return (aRole == RoleIndexSet.ROLE_BEHAVIOR_CALLED && aBehaviorId == getCalledBehaviorId())
			|| (aRole == RoleIndexSet.ROLE_BEHAVIOR_EXECUTED && aBehaviorId == getExecutedBehaviorId())
			|| (aRole == RoleIndexSet.ROLE_BEHAVIOR_ANY 
					&& (aBehaviorId == getExecutedBehaviorId() || aBehaviorId == getCalledBehaviorId()));
	
	}
	
	@Override
	public boolean matchObjectCondition(int aObjectId, byte aRole)
	{
		assert aObjectId != 0;
		return (aRole == RoleIndexSet.ROLE_OBJECT_TARGET && aObjectId == getObjectId(getTarget(), false))
			|| (aRole >= 0 && aRole < getArguments().length && aObjectId == getObjectId(getArguments()[aRole], false));
	}
	
	@Override
	public String toString()
	{
		return String.format(
				"%s (cb: %d, dp: %b, eb: %d, tg: %s, %s)",
				getEventType(),
				itsCalledBehaviorId,
				itsDirectParent,
				itsExecutedBehaviorId,
				itsTarget,
				toString0());
	}
}
