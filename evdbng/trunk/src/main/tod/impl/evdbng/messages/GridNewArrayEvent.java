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
import tod.core.database.structure.IArrayTypeInfo;
import tod.core.database.structure.IStructureDatabase;
import tod.core.database.structure.ITypeInfo;
import tod.impl.dbgrid.GridLogBrowser;
import tod.impl.dbgrid.event.InstantiationEvent;
import tod.impl.dbgrid.messages.MessageType;
import tod.impl.evdbng.SplittedConditionHandler;
import tod.impl.evdbng.db.Indexes;
import tod.impl.evdbng.db.RoleIndexSet;
import tod.impl.evdbng.db.file.PagedFile.PageIOStream;

public class GridNewArrayEvent extends GridEventNG
{
	private static final long serialVersionUID = 6021435584407687823L;

	private Object itsTarget;
	private int itsBaseTypeId;
	private int itsSize;

	public GridNewArrayEvent(IStructureDatabase aStructureDatabase)
	{
		super(aStructureDatabase);
	}

	public GridNewArrayEvent(
			IStructureDatabase aStructureDatabase,
			int aThread, 
			int aDepth,
			long aTimestamp, 
			int[] aAdviceCFlow,
			int aProbeId,
			long aParentTimestamp,
			Object aTarget,
			int aBaseTypeId,
			int aSize)
	{
		super(aStructureDatabase);
		set(aThread, aDepth, aTimestamp, aAdviceCFlow, aProbeId, aParentTimestamp, aTarget, aBaseTypeId, aSize);
	}

	public GridNewArrayEvent(IStructureDatabase aStructureDatabase, PageIOStream aStream)
	{
		super(aStructureDatabase, aStream);

		itsTarget = readObject(aStream);
		itsBaseTypeId = aStream.readTypeId();
		itsSize = aStream.readInt();
	}

	public void set(
			int aThread, 
			int aDepth,
			long aTimestamp, 
			int[] aAdviceCFlow,
			int aProbeId,
			long aParentTimestamp,
			Object aTarget,
			int aBaseTypeId,
			int aSize)
	{
		super.set(aThread, aDepth, aTimestamp, aAdviceCFlow, aProbeId, aParentTimestamp);
		itsTarget = aTarget;
		itsBaseTypeId = aBaseTypeId;
		itsSize = aSize;
	}
	
	@Override
	public void writeTo(PageIOStream aBitStruct)
	{
		super.writeTo(aBitStruct);
		writeObject(aBitStruct, getTarget());
		aBitStruct.writeTypeId(getBaseTypeId());
		aBitStruct.writeInt(getSize());
	}

	@Override
	public int getMessageSize()
	{
		int theCount = super.getMessageSize();
		
		theCount += getObjectSize(getTarget());
		theCount += PageIOStream.typeIdSize();
		theCount += PageIOStream.intSize();
		
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

	@Override
	public void index(Indexes aIndexes, int aId)
	{
		super.index(aIndexes, aId);
	
		aIndexes.indexObject(getTarget(), aId, RoleIndexSet.ROLE_OBJECT_TARGET); 
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
