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
import tod.impl.common.event.ExceptionGeneratedEvent;
import tod.impl.dbgrid.GridLogBrowser;
import tod.impl.dbgrid.messages.MessageType;
import tod.impl.evdbng.SplittedConditionHandler;
import tod.impl.evdbng.db.Indexes;
import tod.impl.evdbng.db.RoleIndexSet;
import tod.impl.evdbng.db.file.PagedFile.PageIOStream;

public class GridExceptionGeneratedEvent extends GridEventNG
{
	private static final long serialVersionUID = 7070448347537157710L;
	
	private Object itsException;
	
	public GridExceptionGeneratedEvent(IStructureDatabase aStructureDatabase)
	{
		super(aStructureDatabase);
	}

	public GridExceptionGeneratedEvent(
			IStructureDatabase aStructureDatabase,
			int aThread, 
			int aDepth,
			long aTimestamp, 
			int[] aAdviceCFlow,
			int aProbeId,
			long aParentTimestamp,
			Object aException)
	{
		super(aStructureDatabase);
		set(aThread, aDepth, aTimestamp, aAdviceCFlow, aProbeId, aParentTimestamp, aException);
	}

	public GridExceptionGeneratedEvent(IStructureDatabase aStructureDatabase, PageIOStream aStream)
	{
		super(aStructureDatabase, aStream);

		itsException = readObject(aStream);
	}
	
	public void set(
			int aThread, 
			int aDepth,
			long aTimestamp, 
			int[] aAdviceCFlow,
			int aProbeId,
			long aParentTimestamp,
			Object aException)
	{
		super.set(aThread, aDepth, aTimestamp, aAdviceCFlow, aProbeId, aParentTimestamp);
		itsException = aException;
	}

	@Override
	public void writeTo(PageIOStream aBitStruct)
	{
		super.writeTo(aBitStruct);
		writeObject(aBitStruct, getException());
	}

	@Override
	public int getMessageSize()
	{
		int theCount = super.getMessageSize();
		
		theCount += getObjectSize(getException());

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
	
	@Override
	public void index(Indexes aIndexes, int aId)
	{
		super.index(aIndexes, aId);
				
		aIndexes.indexObject(getException(), aId, RoleIndexSet.ROLE_OBJECT_EXCEPTION); 
	}
	
	@Override
	public boolean matchBehaviorCondition(int aBehaviorId, byte aRole)
	{
		return (aRole == RoleIndexSet.ROLE_BEHAVIOR_OPERATION) 
			&& (aBehaviorId == getProbeInfo().behaviorId);			
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
				getProbeInfo().behaviorId,
				toString0());
	}
}
