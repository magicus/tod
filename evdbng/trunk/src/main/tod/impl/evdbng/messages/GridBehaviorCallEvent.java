/*
evdbng - Fast database for TOD
Copyright (C) 2007 Sylapse
Proprietary and confidential
*/
package tod.impl.evdbng.messages;

import static tod.impl.evdbng.ObjectCodecNG.getObjectId;
import static tod.impl.evdbng.ObjectCodecNG.getObjectSize;
import static tod.impl.evdbng.ObjectCodecNG.readObject;
import static tod.impl.evdbng.ObjectCodecNG.writeObject;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IStructureDatabase;
import tod.impl.dbgrid.GridLogBrowser;
import tod.impl.dbgrid.event.BehaviorCallEvent;
import tod.impl.dbgrid.event.ConstructorChainingEvent;
import tod.impl.dbgrid.event.InstantiationEvent;
import tod.impl.dbgrid.event.MethodCallEvent;
import tod.impl.dbgrid.messages.MessageType;
import tod.impl.evdbng.SplittedConditionHandler;
import tod.impl.evdbng.db.Indexes;
import tod.impl.evdbng.db.RoleIndexSet;
import tod.impl.evdbng.db.file.PagedFile.PageIOStream;

public class GridBehaviorCallEvent extends GridEventNG
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

	
	public GridBehaviorCallEvent(IStructureDatabase aStructureDatabase)
	{
		super(aStructureDatabase);
	}

	public GridBehaviorCallEvent(
			IStructureDatabase aStructureDatabase,
			int aThread,
			int aDepth,
			long aTimestamp, 
			int[] aAdviceCFlow,
			int aProbeId,
			long aParentTimestamp,
			MessageType aType, 
			boolean aDirectParent, 
			Object[] aArguments, 
			int aCalledBehaviorId, 
			int aExecutedBehaviorId, 
			Object aTarget)
	{
		super(aStructureDatabase);
		set(aThread, aDepth, aTimestamp, aAdviceCFlow, aProbeId, aParentTimestamp, aType, aDirectParent, aArguments, aCalledBehaviorId, aExecutedBehaviorId, aTarget);
	}

	public GridBehaviorCallEvent(IStructureDatabase aStructureDatabase, PageIOStream aStream, MessageType aType)
	{
		super(aStructureDatabase, aStream);
		itsType = (byte) aType.ordinal();
		
		int theArgsCount = aStream.readByte();
		itsArguments = new Object[theArgsCount];
		for (int i = 0; i < itsArguments.length; i++) itsArguments[i] = readObject(aStream);
		
		itsCalledBehaviorId = aStream.readBehaviorId();
		itsExecutedBehaviorId = aStream.readBehaviorId();
		itsDirectParent = aStream.readBoolean();
		itsTarget = readObject(aStream);
	}
	
	public void set(
			int aThread,
			int aDepth,
			long aTimestamp, 
			int[] aAdviceCFlow,
			int aProbeId,
			long aParentTimestamp,
			MessageType aType, 
			boolean aDirectParent, 
			Object[] aArguments, 
			int aCalledBehaviorId, 
			int aExecutedBehaviorId, 
			Object aTarget)
	{
		super.set(aThread, aDepth, aTimestamp, aAdviceCFlow, aProbeId, aParentTimestamp);
		itsType = (byte) aType.ordinal();
		itsDirectParent = aDirectParent;
		itsArguments = aArguments;
		itsCalledBehaviorId = aCalledBehaviorId;
		itsExecutedBehaviorId = aExecutedBehaviorId;
		itsTarget = aTarget;
	}
	
	@Override
	public void writeTo(PageIOStream aBitStruct)
	{
		super.writeTo(aBitStruct);
		
		aBitStruct.writeByte(itsArguments.length);
		for (Object theArgument : itsArguments) writeObject(aBitStruct, theArgument);
		
		aBitStruct.writeBehaviorId(getCalledBehaviorId());
		aBitStruct.writeBehaviorId(getExecutedBehaviorId());
		aBitStruct.writeBoolean(isDirectParent());
		writeObject(aBitStruct, getTarget());
	}
	
	@Override
	public int getMessageSize()
	{
		int theCount = super.getMessageSize();
		
		theCount += PageIOStream.byteSize();
		for (Object theArgument : itsArguments) theCount += getObjectSize(theArgument);
		
		theCount += PageIOStream.behaviorIdSize();
		theCount += PageIOStream.behaviorIdSize();
		theCount += PageIOStream.booleanSize();
		theCount += getObjectSize(getTarget());
		
		return theCount;
	}
	
	@Override
	public ILogEvent toLogEvent(GridLogBrowser aBrowser)
	{
		BehaviorCallEvent theEvent;
		
		switch(getEventType()) 
		{
		case METHOD_CALL:
			theEvent = new MethodCallEvent(aBrowser);
			break;
			
		case INSTANTIATION:
			theEvent = new InstantiationEvent(aBrowser);
			break;
			
		case SUPER_CALL:
			theEvent = new ConstructorChainingEvent(aBrowser);
			break;
			
		default:
			throw new RuntimeException("Not handled: "+this);
		}
		
		initEvent(aBrowser, theEvent);
		theEvent.setArguments(getArguments());
		theEvent.setCalledBehavior(aBrowser.getStructureDatabase().getBehavior(getCalledBehaviorId(), false));
		theEvent.setExecutedBehavior(aBrowser.getStructureDatabase().getBehavior(getExecutedBehaviorId(), false));
		theEvent.setDirectParent(isDirectParent());
		theEvent.setTarget(getTarget());
		
		return theEvent;
	}
	
	@Override
	public MessageType getEventType()
	{
		return MessageType.VALUES[itsType];
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
	
	
	@Override
	public void index(Indexes aIndexes, int aId)
	{
		super.index(aIndexes, aId);
		
		if (getCalledBehaviorId() != -1)
		{
			aIndexes.indexBehavior(getCalledBehaviorId(), aId, RoleIndexSet.ROLE_BEHAVIOR_CALLED);
		}
		
		if (getExecutedBehaviorId() != -1)
		{
			aIndexes.indexBehavior(getExecutedBehaviorId(), aId, RoleIndexSet.ROLE_BEHAVIOR_EXECUTED);
		}
		
		for (int i = 0; i < itsArguments.length; i++)
		{
			Object theArgument = itsArguments[i];
			aIndexes.indexObject(theArgument, aId, (byte) (i+1));
		}
		
		aIndexes.indexObject(getTarget(), aId, RoleIndexSet.ROLE_OBJECT_TARGET);
	}
	
	@Override
	public boolean matchBehaviorCondition(int aBehaviorId, byte aRole)
	{
		return (aRole == RoleIndexSet.ROLE_BEHAVIOR_CALLED && aBehaviorId == getCalledBehaviorId())
			|| (aRole == RoleIndexSet.ROLE_BEHAVIOR_EXECUTED && aBehaviorId == getExecutedBehaviorId())
			|| ((aRole == RoleIndexSet.ROLE_BEHAVIOR_ANY || aRole == RoleIndexSet.ROLE_BEHAVIOR_ANY_ENTER) 
					&& (aBehaviorId == getExecutedBehaviorId() || aBehaviorId == getCalledBehaviorId()));
	
	}
	
	@Override
	public boolean matchObjectCondition(int aPart, int aPartialKey, byte aRole)
	{
		if ((aRole == RoleIndexSet.ROLE_OBJECT_TARGET || aRole == RoleIndexSet.ROLE_OBJECT_ANY)
					&& SplittedConditionHandler.OBJECTS.match(
							aPart, 
							aPartialKey, 
							getObjectId(getTarget(), false)))
		{
			return true;
		}
							
		if (aRole > 0 && aRole <= getArguments().length 
					&& SplittedConditionHandler.OBJECTS.match(
							aPart, 
							aPartialKey, 
							getObjectId(getArguments()[aRole-1], false)))
		{
			return true;
		}
		
		if (aRole == RoleIndexSet.ROLE_OBJECT_ANY || aRole == RoleIndexSet.ROLE_OBJECT_ANYARG)
		{
			for (int i=0;i<getArguments().length;i++) 
			{
				if (SplittedConditionHandler.OBJECTS.match(
						aPart, 
						aPartialKey, 
						getObjectId(getArguments()[i], false)))
				{
					return true;
				}
			}
		}
		
		return false;
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
