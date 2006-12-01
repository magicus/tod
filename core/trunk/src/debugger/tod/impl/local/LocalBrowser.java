/*
 * Created on Oct 11, 2006
 */
package tod.impl.local;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import tod.core.database.structure.IHostInfo;
import tod.core.database.structure.IThreadInfo;
import tod.core.database.structure.ITypeInfo;
import tod.core.database.structure.ObjectId;
import tod.impl.common.CFlowBrowser;
import tod.impl.common.ObjectInspector;
import tod.impl.common.VariablesInspector;
import tod.impl.common.event.Event;
import tod.impl.local.filter.AbstractFilter;
import tod.impl.local.filter.BehaviorCallFilter;
import tod.impl.local.filter.DepthFilter;
import tod.impl.local.filter.FieldWriteFilter;
import tod.impl.local.filter.HostFilter;
import tod.impl.local.filter.InstantiationFilter;
import tod.impl.local.filter.IntersectionFilter;
import tod.impl.local.filter.TargetFilter;
import tod.impl.local.filter.ThreadFilter;
import tod.impl.local.filter.UnionFilter;

public class LocalBrowser implements ILogBrowser
{
	private EventList itsEvents = new EventList();
	private List<IHostInfo> itsHosts = new ArrayList<IHostInfo>();
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
	
	public Object getRegistered(long aId)
	{
		return itsRegisteredObjects.get(aId);
	}

	public IEventBrowser createBrowser (IEventFilter aFilter)
	{
		AbstractFilter theFilter = (AbstractFilter) aFilter;
		return theFilter.createBrowser();
	}
	
	
	public IEventFilter createArgumentFilter(ObjectId aId)
	{
		return null;
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
		throw new UnsupportedOperationException();
	}

	public IEventFilter createThreadFilter(IThreadInfo aThreadInfo)
	{
		return new ThreadFilter(this, aThreadInfo.getId());
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
	
	public IEventFilter createLocationFilter(ITypeInfo aTypeInfo, int aLineNumber)
	{
		throw new UnsupportedOperationException();
	}
	
	public ICFlowBrowser createCFlowBrowser(IThreadInfo aThread)
	{
		return new CFlowBrowser(this, aThread);
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
	
}
