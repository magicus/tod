/*
 * Created on Oct 25, 2004
 */
package tod.impl.local;

import tod.core.model.event.IBehaviorCallEvent;
import tod.core.model.structure.IBehaviorInfo;
import tod.core.model.structure.IClassInfo;
import tod.core.model.structure.IFieldInfo;
import tod.core.model.structure.IThreadInfo;
import tod.core.model.structure.ITypeInfo;
import tod.core.model.structure.ObjectId;
import tod.core.model.trace.ICFlowBrowser;
import tod.core.model.trace.ICompoundFilter;
import tod.core.model.trace.IEventBrowser;
import tod.core.model.trace.IEventFilter;
import tod.core.model.trace.IEventTrace;
import tod.core.model.trace.ILocationTrace;
import tod.core.model.trace.IObjectInspector;
import tod.core.model.trace.IVariablesInspector;
import tod.impl.common.EventCollector;
import tod.impl.common.event.BehaviorCallEvent;
import tod.impl.common.event.Event;
import tod.impl.local.filter.AbstractFilter;
import tod.impl.local.filter.BehaviorCallFilter;
import tod.impl.local.filter.FieldWriteFilter;
import tod.impl.local.filter.InstantiationFilter;
import tod.impl.local.filter.IntersectionFilter;
import tod.impl.local.filter.TargetFilter;
import tod.impl.local.filter.ThreadFilter;
import tod.impl.local.filter.UnionFilter;

/**
 * This log collector stores all the events it receives,
 * and provides an API for accessing the recorded information
 * in a convenient way.
 * @author gpothier
 */
public class LocalCollector extends EventCollector
implements IEventTrace
{
	private EventList itsEvents = new EventList();
	
	public ILocationTrace getLocationTrace()
	{
		return this;
	}
	
	public void clear()
	{
		itsEvents.clear();
	}

	public long getEventsCount()
	{
		return itsEvents.size();
	}
	
	
	@Override
	protected void processEvent(BehaviorCallEvent aParent, Event aEvent)
	{
		aParent.addChild(aEvent);
		itsEvents.add(aEvent);
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
	
	public IEventFilter createThreadFilter(IThreadInfo aThreadInfo)
	{
		return new ThreadFilter(this, aThreadInfo.getId());
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
		return new CFlowBrowser(this, aThread, ((MyThreadInfo) aThread).getRootEvent());
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
	
}
