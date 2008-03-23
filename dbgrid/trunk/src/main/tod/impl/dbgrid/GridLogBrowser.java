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
package tod.impl.dbgrid;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import reflex.lib.pom.POMSync;
import reflex.lib.pom.POMSyncClass;
import reflex.lib.pom.impl.POMMetaobject;
import reflex.lib.pom.impl.lock.LockPOMMetaobject;
import tod.core.database.browser.ICompoundFilter;
import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.IEventFilter;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.browser.IObjectInspector;
import tod.core.database.browser.IVariablesInspector;
import tod.core.database.event.ExternalPointer;
import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.event.IParentEvent;
import tod.core.database.structure.IArraySlotFieldInfo;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IClassInfo;
import tod.core.database.structure.IFieldInfo;
import tod.core.database.structure.IHostInfo;
import tod.core.database.structure.IStructureDatabase;
import tod.core.database.structure.IThreadInfo;
import tod.core.database.structure.ITypeInfo;
import tod.core.database.structure.ObjectId;
import tod.core.database.structure.IStructureDatabase.LocalVariableInfo;
import tod.core.session.ISession;
import tod.impl.common.LogBrowserUtils;
import tod.impl.common.VariablesInspector;
import tod.impl.database.IBidiIterator;
import tod.impl.dbgrid.aggregator.GridEventBrowser;
import tod.impl.dbgrid.aggregator.IGridEventBrowser;
import tod.impl.dbgrid.aggregator.StringHitsIterator;
import tod.impl.dbgrid.db.RoleIndexSet;
import tod.impl.dbgrid.messages.MessageType;
import tod.impl.dbgrid.queries.AdviceCFlowCondition;
import tod.impl.dbgrid.queries.AdviceSourceIdCondition;
import tod.impl.dbgrid.queries.BehaviorCondition;
import tod.impl.dbgrid.queries.BytecodeLocationCondition;
import tod.impl.dbgrid.queries.CompoundCondition;
import tod.impl.dbgrid.queries.Conjunction;
import tod.impl.dbgrid.queries.DepthCondition;
import tod.impl.dbgrid.queries.Disjunction;
import tod.impl.dbgrid.queries.EventCondition;
import tod.impl.dbgrid.queries.EventIdCondition;
import tod.impl.dbgrid.queries.FieldCondition;
import tod.impl.dbgrid.queries.ThreadCondition;
import tod.impl.dbgrid.queries.TypeCondition;
import tod.impl.dbgrid.queries.VariableCondition;
import tod.utils.remote.RemoteStructureDatabase;
import zz.utils.Utils;
import zz.utils.cache.MRUBuffer;
import zz.utils.cache.SyncMRUBuffer;
import zz.utils.monitoring.Monitor.MonitorData;

/**
 * Implementation of {@link ILogBrowser} for the grid backend.
 * This is the client-side object that interfaces with the {@link GridMaster}
 * for executing queries.
 * Note: it is remote because it must be accessed by the master.
 * @author gpothier
 */
@POMSyncClass(
		scheduler = Scheduler.class, 
		group = Scheduler.class,
		syncAll = false)
public class GridLogBrowser extends UnicastRemoteObject
implements ILogBrowser, RIGridMasterListener, IScheduled
{
	private static final long serialVersionUID = -5101014933784311102L;

	private final ISession itsSession;
	
	/**
	 * Manual override of Reflex. We want to avoid the reflexBridge hassle, so
	 * we call the MO manually.
	 */
	private Scheduler itsScheduler = 
		new Scheduler();

	/**
	 * Manual override of Reflex. We want to avoid the reflexBridge hassle, so
	 * we call the MO manually.
	 */
	private POMMetaobject itsMetaobject = new LockPOMMetaobject(itsScheduler);
	
	private RIGridMaster itsMaster;
	private IStructureDatabase itsStructureDatabase;
	
	private long itsEventsCount;
	private long itsFirstTimestamp;
	private long itsLastTimestamp;
	
	private List<IThreadInfo> itsThreads;
	private List<IThreadInfo> itsPackedThreads;
	private List<IHostInfo> itsHosts;
	private List<IHostInfo> itsPackedHosts;
	
	private Map<String, IHostInfo> itsHostsMap = new HashMap<String, IHostInfo>();
	
	private List<IGridBrowserListener> itsListeners = new ArrayList<IGridBrowserListener>();
	
	private TypeCache itsTypeCache = new TypeCache();
	private QueryResultCache itsQueryResultCache = new QueryResultCache();
	
	
	
	private GridLogBrowser(
			ISession aSession,
			RIGridMaster aMaster,
			IStructureDatabase aStructureDatabase) throws RemoteException
	{
		itsSession = aSession;
		itsMaster = aMaster;
		itsMaster.addListener(this);		
		itsStructureDatabase = aStructureDatabase;
		itsScheduler.setGroup(this);
		System.out.println("[GridLogBrowser] Instantiated.");
	}
	
	public static GridLogBrowser createLocal(ISession aSession, GridMaster aMaster) 
	{
		try
		{
			return new GridLogBrowser(aSession, aMaster, aMaster.getStructureDatabase());
		}
		catch (RemoteException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public static GridLogBrowser createRemote(ISession aSession, RIGridMaster aMaster) throws RemoteException
	{
		return new GridLogBrowser(
				aSession,
				aMaster, 
				RemoteStructureDatabase.createDatabase(aMaster.getRemoteStructureDatabase()));
	}

	public ISession getSession()
	{
		return itsSession;
	}
	
	public ILogBrowser getKey()
	{
		return this;
	}
	
	public POMMetaobject getMetaobject()
	{
		return itsMetaobject;
	}
	
	@POMSync
	public void clear()
	{
		try
		{
			getMetaobject().callTrap(null, "clear");
			clear0();
		}
		finally
		{
			getMetaobject().returnTrap();
		}
	}
	
	public void clear0()
	{
		try
		{
			itsMaster.clear();
		}
		catch (RemoteException e)
		{
			throw new RuntimeException(e);
		}
	}

	
	/**
	 * A cache of object types, see {@link GridObjectInspector#getType()}
	 */
	public TypeCache getTypeCache()
	{
		return itsTypeCache;
	}

	public void addListener(IGridBrowserListener aListener)
	{
		itsListeners.add(aListener);
	}
	
	public void removeListener(IGridBrowserListener aListener)
	{
		itsListeners.remove(aListener);
	}
	
	private void fireMonitorData(String aNodeId, MonitorData aData)
	{
		for (IGridBrowserListener theListener : itsListeners)
		{
			theListener.monitorData(aNodeId, aData);
		}
	}

	
	public IEventFilter createArgumentFilter(ObjectId aId)
	{
		return SplittedConditionHandler.OBJECTS.createCondition(
				ObjectCodec.getObjectId(aId, true),
				RoleIndexSet.ROLE_OBJECT_ANYARG);
	}

	public IEventFilter createValueFilter(ObjectId aId)
	{
		return SplittedConditionHandler.OBJECTS.createCondition(
				ObjectCodec.getObjectId(aId, true),
				RoleIndexSet.ROLE_OBJECT_VALUE);
	}
	
	public IEventFilter createResultFilter(ObjectId aId)
	{
		return SplittedConditionHandler.OBJECTS.createCondition(
				ObjectCodec.getObjectId(aId, true),
				RoleIndexSet.ROLE_OBJECT_RESULT);
	}
	
	public IEventFilter createLocationFilter(IBehaviorInfo aBehavior, int aBytecodeIndex)
	{
		return CompoundCondition.and(
				new BehaviorCondition(aBehavior.getId(), RoleIndexSet.ROLE_BEHAVIOR_OPERATION),
				new BytecodeLocationCondition(aBytecodeIndex));
	}

	public IEventFilter createBehaviorCallFilter()
	{
		return CompoundCondition.or(
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
				ObjectCodec.getObjectId(aId, true),
				RoleIndexSet.ROLE_OBJECT_TARGET);
		
		return CompoundCondition.and(
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
				ObjectCodec.getObjectId(aId, true),
				RoleIndexSet.ROLE_OBJECT_TARGET);	
	}

	public IEventFilter createObjectFilter(ObjectId aId)
	{
		return SplittedConditionHandler.OBJECTS.createCondition(
				ObjectCodec.getObjectId(aId, true),
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

	private List<EventIdCondition> getIdConditions(IEventFilter[] aFilters)
	{
		List<EventIdCondition> theIdConditions = new ArrayList<EventIdCondition>();
		for (IEventFilter theFilter : aFilters)
		{
			if (theFilter instanceof EventIdCondition)
			{
				EventIdCondition theCondition = (EventIdCondition) theFilter;
				theIdConditions.add(theCondition);
			}
		}

		return theIdConditions;
	}
	
	private List<IEventFilter> getNonIdConditions(IEventFilter[] aFilters)
	{
		List<IEventFilter> theIdConditions = new ArrayList<IEventFilter>();
		for (IEventFilter theFilter : aFilters)
		{
			if (theFilter instanceof EventIdCondition) continue;
			theIdConditions.add(theFilter);
		}
		
		return theIdConditions;
	}
	
	public ICompoundFilter createIntersectionFilter(IEventFilter... aFilters)
	{
		// If at least one filter is an EventIdCondition,
		// we create the intersection "manually"
		List<EventIdCondition> theIdConditions = getIdConditions(aFilters);
		if (theIdConditions.size() > 0)
		{
			// Check that all event id conditions point to the same event
			ILogEvent theEvent = null;
			for (EventIdCondition theCondition : theIdConditions)
			{
				// An event id condition with a null event has an empty
				// result set, so return an empty union filter.
				if (theCondition.getEvent() == null) return theCondition;
				
				if (theEvent == null) theEvent = theCondition.getEvent();
				else if (theEvent != theCondition.getEvent()) return new EventIdCondition(this, null);
			}
			
			// Check that the rest of the filter also match the event
			List<IEventFilter> theRemainingConditions = getNonIdConditions(aFilters);
			ICompoundFilter theRemainingFilter = createIntersectionFilter(theRemainingConditions.toArray(new IEventFilter[theRemainingConditions.size()]));
			IGridEventBrowser theRemainingBrowser = createBrowser(theRemainingFilter);
			
			if (LogBrowserUtils.hasEvent(theRemainingBrowser, theEvent)) 
			{
				return new EventIdCondition(this, theEvent);
			}
			else return new EventIdCondition(this, null);
		}
		else
		{
			CompoundCondition theCompound = new Conjunction();
			for (IEventFilter theFilter : aFilters)
			{
				theCompound.add(theFilter);
			}
			
			return theCompound;			
		}		
	}

	public ICompoundFilter createUnionFilter(IEventFilter... aFilters)
	{
		List<EventIdCondition> theIdConditions = getIdConditions(aFilters);
		if (theIdConditions.size() > 0) throw new UnsupportedOperationException();
			
		CompoundCondition theCompound = new Disjunction();
		for (IEventFilter theFilter : aFilters)
		{
			theCompound.add(theFilter);
		}
		
		return theCompound;
	}
	
	@POMSync
	public Object getRegistered(ObjectId aId)
	{
		try
		{
			getMetaobject().callTrap(null, "getRegistered");
			return getRegistered0(aId);
		}
		finally
		{
			getMetaobject().returnTrap();
		}		
	}
	
	public Object getRegistered0(ObjectId aId)
	{
		try
		{
			return itsMaster.getRegisteredObject(aId.getId());
		}
		catch (RemoteException e)
		{
			throw new RuntimeException(e);
		}
	}

	public long getEventsCount()
	{
		if (itsEventsCount == 0) updateStats(); 
		return itsEventsCount;
	}

	public long getFirstTimestamp()
	{
		if (itsFirstTimestamp == 0) updateStats();
		return itsFirstTimestamp;
	}

	public long getLastTimestamp()
	{
		if (itsFirstTimestamp == 0) updateStats();
		return itsLastTimestamp;
	}
	
	@POMSync
	private void fetchThreads()
	{
		try
		{
			getMetaobject().callTrap(null, "fetchThreads");
			fetchThreads0();
		}
		finally
		{
			getMetaobject().returnTrap();
		}
	}
	
	private synchronized void fetchThreads0()
	{
		try
		{
			itsThreads = new ArrayList<IThreadInfo>();
			itsPackedThreads = itsMaster.getThreads();
			for (IThreadInfo theThread : itsPackedThreads)
			{
				Utils.listSet(itsThreads, theThread.getId(), theThread);
			}				
		}
		catch (RemoteException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	private List<IThreadInfo> getThreads0()
	{
		if (itsThreads == null) fetchThreads();
		return itsThreads;
	}
	
	@POMSync
	private void fetchHosts()
	{
		try
		{
			getMetaobject().callTrap(null, "fetchHosts");
			fetchHosts0();
		}
		finally
		{
			getMetaobject().returnTrap();
		}		
	}
	
	private synchronized void fetchHosts0()
	{
		try
		{
			itsHosts = new ArrayList<IHostInfo>();
			itsPackedHosts = itsMaster.getHosts();
			for (IHostInfo theHost : itsPackedHosts)
			{
				Utils.listSet(itsHosts, theHost.getId(), theHost);
				itsHostsMap.put(theHost.getName(), theHost);
			}
		}
		catch (RemoteException e)
		{
			throw new RuntimeException(e);
		}		
	}
	
	private List<IHostInfo> getHosts0()
	{
		if (itsHosts == null) fetchHosts();
		return itsHosts;
	}
	
	public Iterable<IThreadInfo> getThreads()
	{
		getThreads0();
		return itsPackedThreads;
	}
	
	public Iterable<IHostInfo> getHosts()
	{
		getHosts0();
		return itsPackedHosts;
	}
	
	public IHostInfo getHost(int aId)
	{
		return getHosts0().get(aId);
	}
	
	public IHostInfo getHost(String aName)
	{
		getHosts0(); // lazy init
		return itsHostsMap.get(aName);
	}

	public IThreadInfo getThread(int aThreadId)
	{
		return getThreads0().get(aThreadId);
	}

	public ILogEvent getEvent(ExternalPointer aPointer)
	{
		return LogBrowserUtils.getEvent(this, aPointer);
	}

	
	public IStructureDatabase getStructureDatabase()
	{
		return itsStructureDatabase;
	}
	
	public RIGridMaster getMaster()
	{
		return itsMaster;
	}

	public IGridEventBrowser createBrowser(IEventFilter aFilter)
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
		else if (aFilter instanceof EventIdCondition)
		{
			EventIdCondition theCondition = (EventIdCondition) aFilter;
			return theCondition.createBrowser();
		}
		else throw new IllegalArgumentException("Not handled: "+aFilter);
	}
	
	public IEventBrowser createBrowser()
	{
		Disjunction theDisjunction = new Disjunction();
		for (IThreadInfo theThread : getThreads())
		{
			theDisjunction.add(createThreadFilter(theThread));
		}
		try
		{
			return new GlobalEventBrowser(this, theDisjunction);
		}
		catch (RemoteException e)
		{
			throw new RuntimeException(e);
		}
	}

	public IParentEvent getCFlowRoot(IThreadInfo aThread)
	{
		return LogBrowserUtils.createCFlowRoot(this, aThread);
	}

	public IObjectInspector createClassInspector(IClassInfo aClass)
	{
		throw new UnsupportedOperationException();
	}

	public GridObjectInspector createObjectInspector(ObjectId aObjectId)
	{
		return new GridObjectInspector(this, aObjectId);
	}

	public IVariablesInspector createVariablesInspector(IBehaviorCallEvent aEvent)
	{
		return new VariablesInspector(aEvent);
	}
	
	@POMSync
	public IBidiIterator<Long> searchStrings(String aSearchText)
	{
		try
		{
			getMetaobject().callTrap(null, "searchStrings");
			return searchStrings0(aSearchText);
		}
		finally
		{
			getMetaobject().returnTrap();
		}
	}
	
	public IBidiIterator<Long> searchStrings0(String aSearchText)
	{
		try
		{
			return new StringHitsIterator(itsMaster.searchStrings(aSearchText));
		}
		catch (RemoteException e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Clears cached information so that they are lazily retrieved.
	 */
	private synchronized void clearStats()
	{
		itsEventsCount = 0;
		itsFirstTimestamp = 0;
		itsLastTimestamp = 0;
		itsThreads = null;
		itsHosts = null;
		itsHostsMap.clear();
	}
	
	@POMSync
	private void updateStats()
	{
		try
		{
			getMetaobject().callTrap(null, "updateStats");
			updateStats0();
		}
		finally
		{
			getMetaobject().returnTrap();
		}
	}
	
	private void updateStats0()
	{
		try
		{
			itsEventsCount = itsMaster.getEventsCount();
			itsFirstTimestamp = itsMaster.getFirstTimestamp();
			itsLastTimestamp = itsMaster.getLastTimestamp();
		}
		catch (RemoteException e)
		{
			throw new RuntimeException(e);
		}
	}

	public void eventsReceived() 
	{
		clearStats();
	}

	public void exception(Throwable aThrowable) 
	{
		aThrowable.printStackTrace();
	}
	
	public void monitorData(String aNodeId, MonitorData aData) 
	{
		fireMonitorData(aNodeId, aData);
	}

	public <O> O exec(Query<O> aQuery)
	{
		return itsQueryResultCache.getResult(aQuery);
	}



	/**
	 * @see TypeCache
	 * @author gpothier
	 */
	public static class TypeCacheEntry
	{
		public final ObjectId object;
		public final ITypeInfo type;
		
		public TypeCacheEntry(final ObjectId aObject, final ITypeInfo aType)
		{
			object = aObject;
			type = aType;
		}
	}
	
	/**
	 * We maintain a cache of object types, as this is a frequent operation.
	 * see {@link GridObjectInspector#getType()}
	 * @author gpothier
	 */
	public class TypeCache extends SyncMRUBuffer<ObjectId, TypeCacheEntry>
	{
		public TypeCache()
		{
			super(100);
		}

		@Override
		protected TypeCacheEntry fetch(ObjectId aId)
		{
			return new TypeCacheEntry(aId, createObjectInspector(aId).getType0());
		}

		@Override
		protected ObjectId getKey(TypeCacheEntry aValue)
		{
			return aValue.object;
		}
		
	}
	
	/**
	 * An event browser that returns all events.
	 * @author gpothier
	 */
	private static class GlobalEventBrowser extends GridEventBrowser
	{
		public GlobalEventBrowser(
				GridLogBrowser aBrowser, 
				EventCondition aFilter) throws RemoteException
		{
			super(aBrowser, aFilter);
		}

		/**
		 * Optimization: as this browser returns all events we don't need
		 * a "real" intersection.
		 */
		@Override
		public IEventBrowser createIntersection(IEventFilter aFilter)
		{
			return getLogBrowser().createBrowser(aFilter);
		}
	}
	
	private static class QueryCacheEntry
	{
		private final Query query;
		
		private final Object result;
		
		/**
		 * Number of events in the database when the query was executed.
		 */
		private final long eventCount;

		public QueryCacheEntry(Query aQuery, Object aResult, long aEventCount)
		{
			query = aQuery;
			result = aResult;
			eventCount = aEventCount;
		}
	}
	
	private class QueryResultCache extends MRUBuffer<Query, QueryCacheEntry>
	{
		public QueryResultCache()
		{
			super(1000);
			
		}
		
		@Override
		protected Query getKey(QueryCacheEntry aValue)
		{
			return aValue.query;
		}

		@Override
		protected QueryCacheEntry fetch(Query aQuery)
		{
			try
			{
				Object theResult = itsMaster.exec(aQuery);
				return new QueryCacheEntry(aQuery, theResult, itsEventsCount);
			}
			catch (RemoteException e)
			{
				throw new RuntimeException(e);
			}
		}
		
		public <O> O getResult(Query<O> aQuery)
		{
			QueryCacheEntry theEntry = get(aQuery);
			if (aQuery.recomputeOnUpdate() && theEntry.eventCount != itsEventsCount)
			{
				drop(aQuery);
				theEntry = get(aQuery);
			}
			return (O) theEntry.result;
		}
		
	}
}
