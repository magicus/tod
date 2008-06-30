/*
evdbng - Fast database for TOD
Copyright (C) 2007 Sylapse
Proprietary and confidential
*/
package tod.impl.evdbng.messages;

import static tod.impl.evdbng.ObjectCodec.getObjectId;
import static tod.impl.evdbng.ObjectCodec.getObjectSize;
import static tod.impl.evdbng.ObjectCodec.readObject;
import static tod.impl.evdbng.ObjectCodec.writeObject;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IStructureDatabase;
import tod.impl.common.event.ArrayWriteEvent;
import tod.impl.dbgrid.GridLogBrowser;
import tod.impl.dbgrid.messages.MessageType;
import tod.impl.evdbng.SplittedConditionHandler;
import tod.impl.evdbng.db.Indexes;
import tod.impl.evdbng.db.RoleIndexSet;
import tod.impl.evdbng.db.file.PagedFile.PageIOStream;

public class GridArrayWriteEvent extends GridEventNG
{
	private static final long serialVersionUID = 3605816555618929935L;
	
	private Object itsTarget;
	private int itsIndex;
	private Object itsValue;

	public GridArrayWriteEvent(IStructureDatabase aStructureDatabase)
	{
		super(aStructureDatabase);
	}

	public GridArrayWriteEvent(
			IStructureDatabase aStructureDatabase,
			int aThread, 
			int aDepth,
			long aTimestamp, 
			int[] aAdviceCFlow,
			int aProbeId,
			long aParentTimestamp,
			Object aTarget,
			int aIndex,
			Object aValue)
	{
		super(aStructureDatabase);
		set(aThread, aDepth, aTimestamp, aAdviceCFlow, aProbeId, aParentTimestamp, aTarget, aIndex, aValue);
	}

	public GridArrayWriteEvent(IStructureDatabase aStructureDatabase, PageIOStream aStream)
	{
		super(aStructureDatabase, aStream);

		itsTarget = readObject(aStream);
		itsIndex = aStream.readInt();
		itsValue = readObject(aStream);
	}

	public void set(
			int aThread, 
			int aDepth,
			long aTimestamp, 
			int[] aAdviceCFlow,
			int aProbeId,
			long aParentTimestamp,
			Object aTarget,
			int aIndex,
			Object aValue)
	{
		super.set(aThread, aDepth, aTimestamp, aAdviceCFlow, aProbeId, aParentTimestamp);
		itsTarget = aTarget;
		itsIndex = aIndex;
		itsValue = aValue;
	}
	
	@Override
	public void writeTo(PageIOStream aBitStruct)
	{
		super.writeTo(aBitStruct);
		writeObject(aBitStruct, getTarget());
		aBitStruct.writeInt(getIndex());
		writeObject(aBitStruct, getValue());
	}

	@Override
	public int getMessageSize()
	{
		int theCount = super.getMessageSize();
		
		theCount += getObjectSize(getTarget());
		theCount += PageIOStream.intSize();
		theCount += getObjectSize(getValue());
		
		return theCount;
	}
	
	@Override
	public ILogEvent toLogEvent(GridLogBrowser aBrowser)
	{
		ArrayWriteEvent theEvent = new ArrayWriteEvent(aBrowser);
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

	@Override
	public void index(Indexes aIndexes, int aId)
	{
		super.index(aIndexes, aId);
		
		aIndexes.indexObject(getTarget(), aId, RoleIndexSet.ROLE_OBJECT_TARGET);
		aIndexes.indexArrayIndex(getIndex(), aId);
		
		aIndexes.indexObject(getValue(), aId, RoleIndexSet.ROLE_OBJECT_VALUE);
	}
	
	@Override
	public boolean matchIndexCondition(int aPart, int aPartialKey)
	{
		return SplittedConditionHandler.INDEXES.match(aPart, aPartialKey, itsIndex);
	}
	
	@Override
	public boolean matchObjectCondition(int aPart, int aPartialKey, byte aRole)
	{
		return ((aRole == RoleIndexSet.ROLE_OBJECT_VALUE  || aRole == RoleIndexSet.ROLE_OBJECT_ANY)
					&& SplittedConditionHandler.OBJECTS.match(
							aPart, 
							aPartialKey, 
							getObjectId(getValue(), false)))
							
			|| ((aRole == RoleIndexSet.ROLE_OBJECT_TARGET  || aRole == RoleIndexSet.ROLE_OBJECT_ANY)
					&& SplittedConditionHandler.OBJECTS.match(
							aPart, 
							aPartialKey, 
							getObjectId(getTarget(), false)));
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
