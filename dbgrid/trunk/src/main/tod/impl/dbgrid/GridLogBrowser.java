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
import tod.core.database.structure.IClassInfo;
import tod.core.database.structure.IHostInfo;
import tod.core.database.structure.IStructureDatabase;
import tod.core.database.structure.IThreadInfo;
import tod.core.database.structure.ITypeInfo;
import tod.core.database.structure.ObjectId;
import tod.core.session.ISession;
import tod.impl.common.LogBrowserUtils;
import tod.impl.common.VariablesInspector;
import tod.impl.database.IBidiIterator;
import tod.impl.dbgrid.aggregator.GridEventBrowser;
import tod.impl.dbgrid.aggregator.StringHitsIterator;
import tod.impl.dbgrid.queries.EventIdCondition;
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
public abstract class GridLogBrowser extends UnicastRemoteObject
implements ILogBrowser, RIGridMasterListener, IScheduled
{
	private static final long serialVersionUID = -5101014933784311102L;

	private final ISession itsSession;
		
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
	
	/**
	 * A cache of object types, see {@link GridObjectInspector#getType()}
	 */
	private TypeCache itsTypeCache = new TypeCache();
	
	private QueryResultCache itsQueryResultCache = new QueryResultCache();
	
	protected GridLogBrowser(
			ISession aSession,
			RIGridMaster aMaster,
			IStructureDatabase aStructureDatabase) throws RemoteException
	{
		itsSession = aSession;
		itsMaster = aMaster;
		itsMaster.addListener(this);		
		itsStructureDatabase = aStructureDatabase;
		System.out.println("[GridLogBrowser] Instantiated.");
	}
	
	public ISession getSession()
	{
		return itsSession;
	}
	
	public ILogBrowser getKey()
	{
		return this;
	}
	
	public void clear()
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
	
	public ITypeInfo getCachedType(ObjectId aId) 
	{
		return itsTypeCache.get(aId).type;
	}

	public void addListener(IGridBrowserListener aListener)
	{
		itsListeners.add(aListener);
	}
	
	public void removeListener(IGridBrowserListener aListener)
	{
		itsListeners.remove(aListener);
	}
	
	private void fireMonitorData(int aNodeId, MonitorData aData)
	{
		for (IGridBrowserListener theListener : itsListeners)
		{
			theListener.monitorData(aNodeId, aData);
		}
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
			IEventBrowser theRemainingBrowser = createBrowser(theRemainingFilter);
			
			if (LogBrowserUtils.hasEvent(theRemainingBrowser, theEvent)) 
			{
				return new EventIdCondition(this, theEvent);
			}
			else return new EventIdCondition(this, null);
		}
		else return createIntersectionFilter0(aFilters);
	}
	
	protected abstract ICompoundFilter createIntersectionFilter0(IEventFilter... aFilters);


	public ICompoundFilter createUnionFilter(IEventFilter... aFilters)
	{
		List<EventIdCondition> theIdConditions = getIdConditions(aFilters);
		if (theIdConditions.size() > 0) throw new UnsupportedOperationException();

		return createUnionFilter0(aFilters);
	}
	
	protected abstract ICompoundFilter createUnionFilter0(IEventFilter... aFilters);

	
	public Object getRegistered(ObjectId aId)
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
	
	private synchronized void fetchThreads()
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
	
	private synchronized List<IThreadInfo> getThreads0()
	{
		if (itsThreads == null) fetchThreads();
		return itsThreads;
	}
	
	private synchronized void fetchHosts()
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
	
	private synchronized List<IHostInfo> getHosts0()
	{
		if (itsHosts == null) fetchHosts();
		return itsHosts;
	}
	
	public synchronized Iterable<IThreadInfo> getThreads()
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

	
	public GridEventBrowser createBrowser()
	{
		ICompoundFilter theDisjunction = createUnionFilter();
		for (IThreadInfo theThread : getThreads())
		{
			theDisjunction.add(createThreadFilter(theThread));
		}
		
		try
		{
			return new GlobalEventBrowser(this, (IGridEventFilter) theDisjunction);
		}
		catch (RemoteException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public IEventBrowser createBrowser(IEventFilter aFilter)
	{
		if (aFilter instanceof EventIdCondition)
		{
			EventIdCondition theCondition = (EventIdCondition) aFilter;
			return theCondition.createBrowser();
		}
		else return createBrowser0(aFilter);
	}
	
	protected abstract GridEventBrowser createBrowser0(IEventFilter aFilter);



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
	
	public IBidiIterator<Long> searchStrings(String aSearchText)
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
	
	private void updateStats()
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
	
	public void monitorData(int aNodeId, MonitorData aData) 
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
				IGridEventFilter aFilter) throws RemoteException
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
