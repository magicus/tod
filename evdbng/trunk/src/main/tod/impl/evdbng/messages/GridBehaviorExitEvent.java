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
import tod.impl.common.event.BehaviorExitEvent;
import tod.impl.dbgrid.GridLogBrowser;
import tod.impl.dbgrid.messages.MessageType;
import tod.impl.evdbng.SplittedConditionHandler;
import tod.impl.evdbng.db.Indexes;
import tod.impl.evdbng.db.RoleIndexSet;
import tod.impl.evdbng.db.file.PagedFile.PageIOStream;

public class GridBehaviorExitEvent extends GridEventNG
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

	
	
	public GridBehaviorExitEvent(IStructureDatabase aStructureDatabase)
	{
		super(aStructureDatabase);
	}

	public GridBehaviorExitEvent(
			IStructureDatabase aStructureDatabase,
			int aThread, 
			int aDepth,
			long aTimestamp, 
			int[] aAdviceCFlow,
			int aProbeId,
			long aParentTimestamp,
			boolean aHasThrown, 
			Object aResult, 
			int aBehaviorId)
	{
		super(aStructureDatabase);
		set(aThread, aDepth, aTimestamp, aAdviceCFlow, aProbeId, aParentTimestamp, aHasThrown, aResult, aBehaviorId);
	}

	public GridBehaviorExitEvent(IStructureDatabase aStructureDatabase, PageIOStream aStream)
	{
		super(aStructureDatabase, aStream);
		itsBehaviorId = aStream.readBehaviorId();
		itsHasThrown = aStream.readBoolean();
		itsResult = readObject(aStream);
	}

	public void set(
			int aThread, 
			int aDepth,
			long aTimestamp, 
			int[] aAdviceCFlow,
			int aProbeId,
			long aParentTimestamp,
			boolean aHasThrown, 
			Object aResult, 
			int aBehaviorId)
	{
		super.set(aThread, aDepth, aTimestamp, aAdviceCFlow, aProbeId, aParentTimestamp);
		itsHasThrown = aHasThrown;
		itsResult = aResult;
		itsBehaviorId = aBehaviorId;
	}
	
	@Override
	public void writeTo(PageIOStream aBitStruct)
	{
		super.writeTo(aBitStruct);
		aBitStruct.writeBehaviorId(getBehaviorId());
		aBitStruct.writeBoolean(hasThrown());
		writeObject(aBitStruct, getResult());
	}
	
	@Override
	public int getMessageSize()
	{
		int theCount = super.getMessageSize();
		
		theCount += PageIOStream.behaviorIdSize();
		theCount += PageIOStream.booleanSize();
		theCount += getObjectSize(getResult());
		
		return theCount;
	}

	@Override
	public ILogEvent toLogEvent(GridLogBrowser aBrowser)
	{
		BehaviorExitEvent theEvent = new BehaviorExitEvent(aBrowser);
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
	
	@Override
	public void index(Indexes aIndexes, int aId)
	{
		super.index(aIndexes, aId);
		
		aIndexes.indexBehavior(getBehaviorId(), aId, RoleIndexSet.ROLE_BEHAVIOR_EXIT);
		aIndexes.indexObject(getResult(), aId, RoleIndexSet.ROLE_OBJECT_RESULT); 
	}

	@Override
	public boolean matchBehaviorCondition(int aBehaviorId, byte aRole)
	{
		return (aRole == RoleIndexSet.ROLE_BEHAVIOR_EXIT || aRole == RoleIndexSet.ROLE_BEHAVIOR_ANY)
			&& (aBehaviorId == getBehaviorId());
	}
	
	@Override
	public boolean matchObjectCondition(int aPart, int aPartialKey, byte aRole)
	{
		return ((aRole == RoleIndexSet.ROLE_OBJECT_RESULT || aRole == RoleIndexSet.ROLE_OBJECT_ANY)
					&& SplittedConditionHandler.OBJECTS.match(
							aPart, 
							aPartialKey, 
							getObjectId(getResult(), false)));
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
