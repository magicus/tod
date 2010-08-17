/*
 * Created on Aug 24, 2006
 */
package tod.impl.dbgrid;

import java.rmi.RemoteException;
import java.util.List;

import tod.core.database.browser.ICFlowBrowser;
import tod.core.database.browser.ICompoundFilter;
import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.IEventFilter;
import tod.core.database.browser.ILocationsRepository;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.browser.IObjectInspector;
import tod.core.database.browser.IVariablesInspector;
import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IClassInfo;
import tod.core.database.structure.IFieldInfo;
import tod.core.database.structure.IThreadInfo;
import tod.core.database.structure.ITypeInfo;
import tod.core.database.structure.ObjectId;
import tod.impl.dbgrid.dbnode.RoleIndexSet;
import tod.impl.dbgrid.messages.MessageType;
import tod.impl.dbgrid.messages.GridEvent;
import tod.impl.dbgrid.messages.ObjectCodec;
import tod.impl.dbgrid.queries.BehaviorCondition;
import tod.impl.dbgrid.queries.CompoundCondition;
import tod.impl.dbgrid.queries.Disjunction;
import tod.impl.dbgrid.queries.FieldCondition;
import tod.impl.dbgrid.queries.ObjectCondition;
import tod.impl.dbgrid.queries.ThreadCondition;
import tod.impl.dbgrid.queries.TypeCondition;

/**
 * Implementation of {@link ILogBrowser} for the grid backend.
 * This is the client-side object that interfaces with the {@link GridMaster}
 * for executing queries.
 * @author gpothier
 */
public class GridLogBrowser implements ILogBrowser, RIGridMasterListener
{
	private RIGridMaster itsMaster;
	
	private long itsEventsCount;
	private long itsFirstTimestamp;
	private long itsLastTimestamp;
	private List<IThreadInfo> itsThreads;

	public GridLogBrowser(RIGridMaster aMaster)
	{
		itsMaster = aMaster;
	}

	public void clear()
	{
		throw new UnsupportedOperationException();
	}

	public IEventFilter createArgumentFilter(ObjectId aId)
	{
		int theId = ObjectCodec.getObjectId(aId, true);
		return new ObjectCondition(theId, RoleIndexSet.ROLE_OBJECT_ANYARG);
	}

	public IEventFilter createBehaviorCallFilter()
	{
		return CompoundCondition.or(
				new TypeCondition(MessageType.METHOD_CALL));
	}

	public IEventFilter createBehaviorCallFilter(IBehaviorInfo aBehavior)
	{
		return new BehaviorCondition(aBehavior.getId(), RoleIndexSet.ROLE_BEHAVIOR_ANY_ENTER);
	}

	public IEventFilter createFieldFilter(IFieldInfo aField)
	{
		return new FieldCondition(aField.getId());
	}

	public IEventFilter createFieldWriteFilter()
	{
		return new TypeCondition(MessageType.FIELD_WRITE);
	}

	public IEventFilter createInstantiationFilter(ObjectId aId)
	{
		int theId = ObjectCodec.getObjectId(aId, true);
		return CompoundCondition.and(
				new ObjectCondition(theId, RoleIndexSet.ROLE_OBJECT_ANYARG),
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

	public IEventFilter createLocationFilter(ITypeInfo aType, int aLineNumber)
	{
		throw new UnsupportedOperationException();
	}

	public IEventFilter createTargetFilter(ObjectId aId)
	{
		int theId = ObjectCodec.getObjectId(aId, true);
		return new ObjectCondition(theId, RoleIndexSet.ROLE_OBJECT_TARGET);
	}

	public IEventFilter createThreadFilter(IThreadInfo aThread)
	{
		try
		{
			int theThreadNumber = itsMaster.getThreadNumber(aThread.getHost().getId(), aThread.getId());
			return new ThreadCondition(theThreadNumber);
		}
		catch (RemoteException e)
		{
			throw new RuntimeException(e);
		}
	}

	public ICompoundFilter createIntersectionFilter(IEventFilter... aFilters)
	{
		CompoundCondition theCompound = new Disjunction();
		for (IEventFilter theFilter : aFilters)
		{
			theCompound.add(theFilter);
		}
		
		return theCompound;
	}

	public ICompoundFilter createUnionFilter(IEventFilter... aFilters)
	{
		CompoundCondition theCompound = new Disjunction();
		for (IEventFilter theFilter : aFilters)
		{
			theCompound.add(theFilter);
		}
		
		return theCompound;
	}
	
	public long getEventsCount()
	{
		return itsEventsCount;
	}

	public long getFirstTimestamp()
	{
		return itsFirstTimestamp;
	}

	public long getLastTimestamp()
	{
		return itsLastTimestamp;
	}
	
	public Iterable<IThreadInfo> getThreads()
	{
		return itsThreads;
	}
	
	public ILocationsRepository getLocationsRepository()
	{
		throw new UnsupportedOperationException();
	}
	
	public IEventBrowser createBrowser(IEventFilter aFilter)
	{
		throw new UnsupportedOperationException();
	}

	public ICFlowBrowser createCFlowBrowser(IThreadInfo aThread)
	{
		throw new UnsupportedOperationException();
	}

	public IObjectInspector createClassInspector(IClassInfo aClass)
	{
		throw new UnsupportedOperationException();
	}

	public IObjectInspector createObjectInspector(ObjectId aObjectId)
	{
		throw new UnsupportedOperationException();
	}

	public IVariablesInspector createVariablesInspector(IBehaviorCallEvent aEvent)
	{
		throw new UnsupportedOperationException();
	}

	public void eventsReceived() throws RemoteException
	{
		itsEventsCount = itsMaster.getEventsCount();
		itsFirstTimestamp = itsMaster.getFirstTimestamp();
		itsLastTimestamp = itsMaster.getLastTimestamp();
		itsThreads = itsMaster.getThreads();
	}

	public void exception(Throwable aThrowable) 
	{
		aThrowable.printStackTrace();
	}

	
}