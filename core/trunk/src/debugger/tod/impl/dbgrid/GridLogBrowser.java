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

import tod.core.ILocationRegisterer.LocalVariableInfo;
import tod.core.database.browser.ICompoundFilter;
import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.IEventFilter;
import tod.core.database.browser.ILocationsRepository;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.browser.IObjectInspector;
import tod.core.database.browser.IVariablesInspector;
import tod.core.database.event.ExternalPointer;
import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.IBehaviorExitEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.event.IParentEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IClassInfo;
import tod.core.database.structure.IFieldInfo;
import tod.core.database.structure.IHostInfo;
import tod.core.database.structure.IThreadInfo;
import tod.core.database.structure.ITypeInfo;
import tod.core.database.structure.ObjectId;
import tod.impl.common.LogBrowserUtils;
import tod.impl.common.VariablesInspector;
import tod.impl.dbgrid.aggregator.GridEventBrowser;
import tod.impl.dbgrid.db.RoleIndexSet;
import tod.impl.dbgrid.messages.MessageType;
import tod.impl.dbgrid.messages.ObjectCodec;
import tod.impl.dbgrid.monitoring.Monitor.MonitorData;
import tod.impl.dbgrid.queries.BehaviorCondition;
import tod.impl.dbgrid.queries.BytecodeLocationCondition;
import tod.impl.dbgrid.queries.CompoundCondition;
import tod.impl.dbgrid.queries.Conjunction;
import tod.impl.dbgrid.queries.DepthCondition;
import tod.impl.dbgrid.queries.Disjunction;
import tod.impl.dbgrid.queries.EventCondition;
import tod.impl.dbgrid.queries.FieldCondition;
import tod.impl.dbgrid.queries.HostCondition;
import tod.impl.dbgrid.queries.ObjectCondition;
import tod.impl.dbgrid.queries.ThreadCondition;
import tod.impl.dbgrid.queries.TypeCondition;
import tod.impl.dbgrid.queries.VariableCondition;
import tod.utils.remote.RemoteLocationsRepository;
import zz.utils.ITask;
import zz.utils.Utils;
import zz.utils.cache.SyncMRUBuffer;

/**
 * Implementation of {@link ILogBrowser} for the grid backend.
 * This is the client-side object that interfaces with the {@link GridMaster}
 * for executing queries.
 * Note: it is remote because it must be accessed by the master.
 * @author gpothier
 */
public class GridLogBrowser extends UnicastRemoteObject
implements ILogBrowser, RIGridMasterListener
{
	private RIGridMaster itsMaster;
	private ILocationsRepository itsLocationsRepository;
	
	private long itsEventsCount;
	private long itsFirstTimestamp;
	private long itsLastTimestamp;
	private List<IThreadInfo> itsThreads;
	private List<IHostInfo> itsHosts;
	private Map<String, IHostInfo> itsHostsMap = new HashMap<String, IHostInfo>();
	private Map<Integer, HostThreadsList> itsHostThreadsLists = new HashMap<Integer, HostThreadsList>();
	
	private List<IGridBrowserListener> itsListeners = new ArrayList<IGridBrowserListener>();
	
	private TypeCache itsTypeCache = new TypeCache();

	public GridLogBrowser(GridMaster aMaster) throws RemoteException
	{
		itsMaster = aMaster;
		itsMaster.addListener(this);
		
		itsLocationsRepository = aMaster.getLocationStore();
	}
	
	public GridLogBrowser(RIGridMaster aMaster) throws RemoteException
	{
		itsMaster = aMaster;
		itsMaster.addListener(this);
		
		itsLocationsRepository = RemoteLocationsRepository.createRepository(
				itsMaster.getLocationsRepository());
	}

	public void clear()
	{
		throw new UnsupportedOperationException();
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
		return new FieldCondition(aField.getId());
	}

	public IEventFilter createFieldWriteFilter()
	{
		return new TypeCondition(MessageType.FIELD_WRITE);
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
		return new HostCondition(aHost.getId());
	}

	public IEventFilter createThreadFilter(IThreadInfo aThread)
	{
		return createIntersectionFilter(
				new ThreadCondition(aThread.getId()),
				new HostCondition(aThread.getHost().getId()));
	}

	public IEventFilter createDepthFilter(int aDepth)
	{
		return new DepthCondition(aDepth);
	}

	public ICompoundFilter createIntersectionFilter(IEventFilter... aFilters)
	{
		CompoundCondition theCompound = new Conjunction();
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
	
	public Object getRegistered(long aId)
	{
		try
		{
			return itsMaster.getRegisteredObject(aId);
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
	
	private List<IThreadInfo> getThreads0()
	{
		try
		{
			if (itsThreads == null)
			{
				itsThreads = itsMaster.getThreads();
				
				// Update per-host threads list
				itsHostThreadsLists.clear();
				for (IThreadInfo theThread : itsThreads)
				{
					IHostInfo theHost = theThread.getHost();
					HostThreadsList theList = itsHostThreadsLists.get(theHost.getId());
					if (theList == null)
					{
						theList = new HostThreadsList(theHost);
						itsHostThreadsLists.put(theHost.getId(), theList);
					}
					
					theList.add(theThread);
				}
			}
		}
		catch (RemoteException e)
		{
			throw new RuntimeException(e);
		}
		return itsThreads;
	}
	
	private List<IHostInfo> getHosts0()
	{
		try
		{
			if (itsHosts == null) 
			{
				itsHosts = new ArrayList<IHostInfo>();
				itsHostsMap = new HashMap<String, IHostInfo>();
				List<IHostInfo> theHosts = itsMaster.getHosts();
				for (IHostInfo theHost : theHosts)
				{
					Utils.listSet(itsHosts, theHost.getId(), theHost);
					itsHostsMap.put(theHost.getName(), theHost);
				}
			}
		}
		catch (RemoteException e)
		{
			throw new RuntimeException(e);
		}
		return itsHosts;
	}
	
	public Iterable<IThreadInfo> getThreads()
	{
		return getThreads0();
	}
	
	public Iterable<IHostInfo> getHosts()
	{
		return getHosts0();
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

	public IThreadInfo getThread(int aHostId, int aThreadId)
	{
		getThreads(); // Lazy init of thread lists
		HostThreadsList theList = itsHostThreadsLists.get(aHostId);
		if (theList == null) return null;
		
		return theList.get(aThreadId);
	}

	public ILogEvent getEvent(ExternalPointer aPointer)
	{
		return LogBrowserUtils.getEvent(this, aPointer);
	}

	public ILocationsRepository getLocationsRepository()
	{
		return itsLocationsRepository;
	}
	
	public RIGridMaster getMaster()
	{
		return itsMaster;
	}

	public GridEventBrowser createBrowser(IEventFilter aFilter)
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
	
	private void updateStats()
	{
		try
		{
			itsEventsCount = itsMaster.getEventsCount();
			itsFirstTimestamp = itsMaster.getFirstTimestamp();
			itsLastTimestamp = itsMaster.getLastTimestamp();
			itsThreads = null; // lazy
			itsHosts = null; // lazy		
			itsHostsMap = null; // lazy
		}
		catch (RemoteException e)
		{
			throw new RuntimeException(e);
		}
	}

	public void eventsReceived() 
	{
		updateStats();
	}

	public void exception(Throwable aThrowable) 
	{
		aThrowable.printStackTrace();
	}
	
	public void monitorData(String aNodeId, MonitorData aData) 
	{
		fireMonitorData(aNodeId, aData);
	}

	public <O> O exec(ITask<ILogBrowser, O> aTask)
	{
		try
		{
			return itsMaster.exec(aTask);
		}
		catch (RemoteException e)
		{
			throw new RuntimeException(e);
		}
	}



	private static class HostThreadsList
	{
		private IHostInfo itsHost;
		private List<IThreadInfo> itsThreads = new ArrayList<IThreadInfo>();
		
		public HostThreadsList(IHostInfo aHost)
		{
			itsHost = aHost;
		}
		
		public void add(IThreadInfo aThread)
		{
			Utils.listSet(itsThreads, aThread.getId(), aThread);
		}
		
		public IThreadInfo get(int aIndex)
		{
			return itsThreads.get(aIndex);
		}
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
	
}
