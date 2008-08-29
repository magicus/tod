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
package tod.impl.evdb1;

import java.rmi.RemoteException;

import tod.core.database.browser.ICompoundFilter;
import tod.core.database.browser.IEventFilter;
import tod.core.database.browser.IEventPredicate;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IArraySlotFieldInfo;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IFieldInfo;
import tod.core.database.structure.IHostInfo;
import tod.core.database.structure.IStructureDatabase;
import tod.core.database.structure.IThreadInfo;
import tod.core.database.structure.ITypeInfo;
import tod.core.database.structure.ObjectId;
import tod.core.database.structure.IBehaviorInfo.BytecodeRole;
import tod.core.database.structure.IStructureDatabase.LocalVariableInfo;
import tod.core.session.ISession;
import tod.impl.dbgrid.GridLogBrowser;
import tod.impl.dbgrid.GridMaster;
import tod.impl.dbgrid.RIGridMaster;
import tod.impl.dbgrid.aggregator.GridEventBrowser;
import tod.impl.dbgrid.messages.MessageType;
import tod.impl.dbgrid.queries.EventIdCondition;
import tod.impl.evdb1.db.RoleIndexSet;
import tod.impl.evdb1.queries.AdviceCFlowCondition;
import tod.impl.evdb1.queries.AdviceSourceIdCondition;
import tod.impl.evdb1.queries.BehaviorCondition;
import tod.impl.evdb1.queries.BytecodeLocationCondition;
import tod.impl.evdb1.queries.CompoundCondition;
import tod.impl.evdb1.queries.Conjunction;
import tod.impl.evdb1.queries.DepthCondition;
import tod.impl.evdb1.queries.Disjunction;
import tod.impl.evdb1.queries.EventCondition;
import tod.impl.evdb1.queries.FieldCondition;
import tod.impl.evdb1.queries.PredicateCondition;
import tod.impl.evdb1.queries.RoleCondition;
import tod.impl.evdb1.queries.ThreadCondition;
import tod.impl.evdb1.queries.TypeCondition;
import tod.impl.evdb1.queries.VariableCondition;
import tod.utils.remote.RemoteStructureDatabase;

/**
 * Implementation of {@link ILogBrowser} for the grid backend.
 * This is the client-side object that interfaces with the {@link GridMaster}
 * for executing queries.
 * Note: it is remote because it must be accessed by the master.
 * @author gpothier
 */
public class GridLogBrowser1 extends GridLogBrowser
{
	private static final long serialVersionUID = 9081038733784411102L;

	
	private GridLogBrowser1(
			ISession aSession,
			RIGridMaster aMaster,
			IStructureDatabase aStructureDatabase) throws RemoteException
	{
		super(aSession, aMaster, aStructureDatabase);
	}
	
	public static GridLogBrowser createLocal(ISession aSession, GridMaster aMaster) 
	{
		try
		{
			return new GridLogBrowser1(aSession, aMaster, aMaster.getStructureDatabase());
		}
		catch (RemoteException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public static GridLogBrowser createRemote(ISession aSession, RIGridMaster aMaster) throws RemoteException
	{
		return new GridLogBrowser1(
				aSession,
				aMaster, 
				RemoteStructureDatabase.createDatabase(aMaster.getRemoteStructureDatabase()));
	}

	
	
	public IEventFilter createArgumentFilter(ObjectId aId)
	{
		return SplittedConditionHandler.OBJECTS.createCondition(
				ObjectCodec1.getObjectId(aId, true),
				RoleIndexSet.ROLE_OBJECT_ANYARG);
	}

	public IEventFilter createArgumentFilter(ObjectId aId, int aPosition)
	{
		return SplittedConditionHandler.OBJECTS.createCondition(
				ObjectCodec1.getObjectId(aId, true),
				(byte) aPosition);
	}
	
	public IEventFilter createValueFilter(ObjectId aId)
	{
		return SplittedConditionHandler.OBJECTS.createCondition(
				ObjectCodec1.getObjectId(aId, true),
				RoleIndexSet.ROLE_OBJECT_VALUE);
	}
	
	public IEventFilter createResultFilter(ObjectId aId)
	{
		return SplittedConditionHandler.OBJECTS.createCondition(
				ObjectCodec1.getObjectId(aId, true),
				RoleIndexSet.ROLE_OBJECT_RESULT);
	}
	
	public IEventFilter createLocationFilter(IBehaviorInfo aBehavior, int aBytecodeIndex)
	{
		return createIntersectionFilter(
				new BehaviorCondition(aBehavior.getId(), RoleIndexSet.ROLE_BEHAVIOR_OPERATION),
				new BytecodeLocationCondition(aBytecodeIndex));
	}

	public IEventFilter createBehaviorCallFilter()
	{
		return createUnionFilter(
				new TypeCondition(MessageType.METHOD_CALL),
				new TypeCondition(MessageType.INSTANTIATION),
				new TypeCondition(MessageType.SUPER_CALL));
	}

	public IEventFilter createBehaviorCallFilter(IBehaviorInfo aBehavior)
	{
		return new BehaviorCondition(aBehavior.getId(), RoleIndexSet.ROLE_BEHAVIOR_ANY_ENTER);
	}

	public IEventFilter createExceptionGeneratedFilter()
	{
		return new TypeCondition(MessageType.EXCEPTION_GENERATED);
	}

	public IEventFilter createFieldFilter(IFieldInfo aField)
	{
		if (aField instanceof IArraySlotFieldInfo)
		{
			IArraySlotFieldInfo theField = (IArraySlotFieldInfo) aField;
			return SplittedConditionHandler.INDEXES.createCondition(theField.getIndex(), (byte) 0);
		}
		else
		{
			return new FieldCondition(aField.getId());
		}
	}

	public IEventFilter createFieldWriteFilter()
	{
		return new TypeCondition(MessageType.FIELD_WRITE);
	}

	public IEventFilter createVariableWriteFilter()
	{
		return new TypeCondition(MessageType.LOCAL_VARIABLE_WRITE);
	}
	
	public IEventFilter createAdviceSourceIdFilter(int aAdviceSourceId)
	{
		return new AdviceSourceIdCondition(aAdviceSourceId);
	}

	public IEventFilter createAdviceCFlowFilter(int aAdviceSourceId)
	{
		return new AdviceCFlowCondition(aAdviceSourceId);
	}

	public IEventFilter createRoleFilter(BytecodeRole aRole)
	{
		return new RoleCondition(aRole);
	}

	public IEventFilter createArrayWriteFilter()
	{
		return new TypeCondition(MessageType.ARRAY_WRITE);
	}
	
	public IEventFilter createVariableWriteFilter(LocalVariableInfo aVariable)
	{
		return new VariableCondition(aVariable.getIndex());
	}

	public IEventFilter createInstantiationFilter(ObjectId aId)
	{
		Conjunction theObjectCondition = SplittedConditionHandler.OBJECTS.createCondition(
				ObjectCodec1.getObjectId(aId, true),
				RoleIndexSet.ROLE_OBJECT_TARGET);
		
		return createIntersectionFilter(
				theObjectCondition,
				new TypeCondition(MessageType.INSTANTIATION));
	}

	public IEventFilter createInstantiationsFilter()
	{
		return new TypeCondition(MessageType.INSTANTIATION);
	}

	public IEventFilter createInstantiationsFilter(ITypeInfo aType)
	{
		throw new UnsupportedOperationException();
	}

	public IEventFilter createTargetFilter(ObjectId aId)
	{
		return SplittedConditionHandler.OBJECTS.createCondition(
				ObjectCodec1.getObjectId(aId, true),
				RoleIndexSet.ROLE_OBJECT_TARGET);	
	}

	public IEventFilter createObjectFilter(ObjectId aId)
	{
		return SplittedConditionHandler.OBJECTS.createCondition(
				ObjectCodec1.getObjectId(aId, true),
				RoleIndexSet.ROLE_OBJECT_ANY);
	}
	
	public IEventFilter createHostFilter(IHostInfo aHost)
	{
		Iterable<IThreadInfo> theThreads = aHost.getThreads();
		CompoundCondition theCompound = new Disjunction();
		
		for (IThreadInfo theThread : theThreads) 
		{
			theCompound.add(createThreadFilter(theThread));
		}
		
		return theCompound;
	}
	
	public IEventFilter createEventFilter(ILogEvent aEvent)
	{
		return new EventIdCondition(this, aEvent);
	}

	public IEventFilter createThreadFilter(IThreadInfo aThread)
	{
		return new ThreadCondition(aThread.getId());
	}

	public IEventFilter createDepthFilter(int aDepth)
	{
		return new DepthCondition(aDepth);
	}

	
	@Override
	protected ICompoundFilter createIntersectionFilter0(IEventFilter... aFilters)
	{
		CompoundCondition theCompound = new Conjunction(false, false);
		for (IEventFilter theFilter : aFilters)
		{
			theCompound.add(theFilter);
		}
		
		return theCompound;			
	}

	@Override
	protected ICompoundFilter createUnionFilter0(IEventFilter... aFilters)
	{
		CompoundCondition theCompound = new Disjunction();
		for (IEventFilter theFilter : aFilters)
		{
			theCompound.add(theFilter);
		}
		
		return theCompound;
	}
	
	public IEventFilter createPredicateFilter(IEventPredicate aPredicate, IEventFilter aBaseFilter)
	{
		return new PredicateCondition((EventCondition) aBaseFilter, aPredicate);
	}

	@Override
	protected GridEventBrowser createBrowser0(IEventFilter aFilter)
	{
		if (aFilter instanceof EventCondition)
		{
			EventCondition theCondition = (EventCondition) aFilter;
			try
			{
				return new GridEventBrowser(this, theCondition);
			}
			catch (RemoteException e)
			{
				throw new RuntimeException(e);
			}
		}
		else throw new IllegalArgumentException("Not handled: "+aFilter);
	}
}