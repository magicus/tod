/*
 * Created on Jul 23, 2006
 */
package tod.impl.dbgrid.messages;

import tod.impl.common.event.Event;
import tod.impl.dbgrid.DebuggerGridConfig;
import tod.impl.dbgrid.dbnode.Indexes;
import tod.impl.dbgrid.dbnode.RoleIndexSet;
import zz.utils.bit.BitStruct;
import zz.utils.bit.IntBitStruct;

public class GridBehaviorExitEvent extends GridEvent
{
	private boolean itsHasThrown;
	private Object itsResult;
	
	/**
	 * We need to specify the behavior id for indexing.
	 * This will be the called behavior if the executed behavior is not
	 * available.
	 */
	private int itsBehaviorId;

	public GridBehaviorExitEvent(
			Event aEvent,
			boolean aHasThrown, 
			Object aResult, 
			int aBehaviorId)
	{
		super(aEvent);
		itsHasThrown = aHasThrown;
		itsResult = aResult;
		itsBehaviorId = aBehaviorId;
	}

	public GridBehaviorExitEvent(IntBitStruct aBitStruct)
	{
		super(aBitStruct);
		itsBehaviorId = aBitStruct.readInt(DebuggerGridConfig.EVENT_BEHAVIOR_BITS);
		itsHasThrown = aBitStruct.readBoolean();
		itsResult = readObject(aBitStruct);
	}

	@Override
	public void writeTo(IntBitStruct aBitStruct)
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
	public EventType getEventType()
	{
		return EventType.BEHAVIOR_EXIT;
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
	public void index(Indexes aIndexes, long aPointer)
	{
		super.index(aIndexes, aPointer);
		
		aIndexes.behaviorIndex.addTuple(
				getBehaviorId(), 
				new RoleIndexSet.Tuple(getTimestamp(), aPointer, RoleIndexSet.ROLE_BEHAVIOR_ANY));
		
		aIndexes.objectIndex.addTuple(
				getResult(), 
				new RoleIndexSet.Tuple(getTimestamp(), aPointer, RoleIndexSet.ROLE_OBJECT_RESULT));
	}
	
}
