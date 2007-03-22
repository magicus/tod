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
package tod.impl.local;

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
import tod.impl.common.ObjectInspector;
import tod.impl.common.VariablesInspector;
import tod.impl.common.event.Event;
import tod.impl.dbgrid.BidiIterator;
import tod.impl.local.filter.AbstractFilter;
import tod.impl.local.filter.BehaviorCallFilter;
import tod.impl.local.filter.DepthFilter;
import tod.impl.local.filter.ExceptionGeneratedFilter;
import tod.impl.local.filter.FieldWriteFilter;
import tod.impl.local.filter.HostFilter;
import tod.impl.local.filter.InstantiationFilter;
import tod.impl.local.filter.IntersectionFilter;
import tod.impl.local.filter.ObjectFilter;
import tod.impl.local.filter.OperationLocationFilter;
import tod.impl.local.filter.TargetFilter;
import tod.impl.local.filter.ThreadFilter;
import tod.impl.local.filter.UnionFilter;
import tod.impl.local.filter.VariableWriteFilter;
import zz.utils.ITask;

public class LocalBrowser implements ILogBrowser
{
	private EventList itsEvents = new EventList();
	private List<IHostInfo> itsHosts = new ArrayList<IHostInfo>();
	private Map<String, IHostInfo> itsHostsMap = new HashMap<String, IHostInfo>();
	private List<IThreadInfo> itsThreads = new ArrayList<IThreadInfo>();
	private final ILocationsRepository itsLocationsRepository;
	
	/**
	 * Temporary. Holds registered objects.
	 */
	private Map<Long, Object> itsRegisteredObjects = new HashMap<Long, Object>();
	

	
	public LocalBrowser(ILocationsRepository aLocationsRepository)
	{
		itsLocationsRepository = aLocationsRepository;
	}

	public ILocationsRepository getLocationsRepository()
	{
		return itsLocationsRepository;
	}

	public Iterable<IThreadInfo> getThreads()
	{
		return itsThreads;
	}
	
	public void addEvent(Event aEvent)
	{
		itsEvents.add(aEvent);
	}
	
	public void addThread(IThreadInfo aThread)
	{
		itsThreads.add(aThread);
	}
	
	public void addHost(IHostInfo aHost)
	{
		itsHosts.add(aHost);
		itsHostsMap.put(aHost.getName(), aHost);
	}

	public void clear()
	{
		itsEvents.clear();
	}

	public long getEventsCount()
	{
		return itsEvents.size();
	}
	
	public EventList getEvents()
	{
		return itsEvents;
	}

	public ILogEvent getEvent(ExternalPointer aPointer)
	{
		return LogBrowserUtils.getEvent(this, aPointer);
	}

	public long getFirstTimestamp()
	{
		return itsEvents.getFirstTimestamp();
	}

	public long getLastTimestamp()
	{
		return itsEvents.getLastTimestamp();
	}
	
	public void register(long aObjectUID, Object aObject)
	{
		itsRegisteredObjects.put(aObjectUID, aObject);
	}
	
	public Object getRegistered(ObjectId aId)
	{
		return itsRegisteredObjects.get(aId.getId());
	}

	public IEventBrowser createBrowser (IEventFilter aFilter)
	{
		AbstractFilter theFilter = (AbstractFilter) aFilter;
		return theFilter.createBrowser();
	}
	
	public IEventBrowser createBrowser()
	{
		return createBrowser(createIntersectionFilter());
	}

	public IEventFilter createArgumentFilter(ObjectId aId)
	{
		return null;
	}
	
	public IEventFilter createExceptionGeneratedFilter()
	{
		return new ExceptionGeneratedFilter(this);
	}

	public IEventFilter createLocationFilter(IBehaviorInfo aBehavior, int aBytecodeIndex)
	{
		return new OperationLocationFilter(this, aBehavior, aBytecodeIndex);
	}

	public IEventFilter createBehaviorCallFilter()
	{
		return new BehaviorCallFilter(this);
	}

	public IEventFilter createBehaviorCallFilter(IBehaviorInfo aBehavior)
	{
		return new BehaviorCallFilter(this, aBehavior);
	}

	public IEventFilter createFieldFilter(IFieldInfo aFieldInfo)
	{
		return new FieldWriteFilter(this, aFieldInfo);
	}
	
	public IEventFilter createFieldWriteFilter()
	{
		return new FieldWriteFilter(this);
	}
	
	public IEventFilter createVariableWriteFilter(LocalVariableInfo aVariable)
	{
		return new VariableWriteFilter(this, aVariable);
	}

	public IEventFilter createInstantiationsFilter()
	{
		return new InstantiationFilter(this);
	}
	
	public IEventFilter createInstantiationsFilter(ITypeInfo aTypeInfo)
	{
		return new InstantiationFilter(this, aTypeInfo);
	}
	
	public IEventFilter createInstantiationFilter(ObjectId aObjectId)
	{
		return new InstantiationFilter(this, aObjectId);
	}
	
	public ICompoundFilter createIntersectionFilter(IEventFilter... aFilters)
	{
		return new IntersectionFilter(this, aFilters);
	}
	
	public IEventFilter createTargetFilter(ObjectId aId)
	{
		return new TargetFilter(this, aId);
	}
	
	public IEventFilter createObjectFilter(ObjectId aId)
	{
		return new ObjectFilter(this, aId);
	}

	public IEventFilter createThreadFilter(IThreadInfo aThreadInfo)
	{
		return new ThreadFilter(
				this, 
				aThreadInfo.getHost().getId(), 
				aThreadInfo.getId());
	}
	
	public IEventFilter createDepthFilter(int aDepth)
	{
		return new DepthFilter(this, aDepth);
	}

	public IEventFilter createHostFilter(IHostInfo aHostInfo)
	{
		return new HostFilter(this, aHostInfo.getId());
	}
	
	public ICompoundFilter createUnionFilter(IEventFilter... aFilters)
	{
		return new UnionFilter(this, aFilters);
	}
	
	public IParentEvent getCFlowRoot(IThreadInfo aThread)
	{
		return LogBrowserUtils.createCFlowRoot(this, aThread);
	}

	public IObjectInspector createObjectInspector(ObjectId aObjectId)
	{
		return new ObjectInspector(this, aObjectId);
	}
	
	public IObjectInspector createClassInspector(IClassInfo aClass)
	{
		return new ObjectInspector(this, aClass);
	}

	public IVariablesInspector createVariablesInspector(IBehaviorCallEvent aEvent)
	{
		return new VariablesInspector(aEvent);
	}
	
	public Iterable<IHostInfo> getHosts()
	{
		return itsHosts;
	}

	public IHostInfo getHost(String aName)
	{
		return itsHostsMap.get(aName);
	}
	
	public BidiIterator<Long> searchStrings(String aSearchText)
	{
		throw new UnsupportedOperationException();
	}

	public <O> O exec(Query<O> aQuery)
	{
		return aQuery.run(this);
	}
	
	
	
}
