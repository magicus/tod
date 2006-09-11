/*
 * Created on Oct 25, 2004
 */
package tod.impl.local;

import tod.core.Output;
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
import tod.impl.common.EventCollector;
import tod.impl.common.ObjectInspector;
import tod.impl.common.VariablesInspector;
import tod.impl.local.filter.AbstractFilter;
import tod.impl.local.filter.BehaviorCallFilter;
import tod.impl.local.filter.FieldWriteFilter;
import tod.impl.local.filter.HostFilter;
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
implements ILogBrowser
{
	private EventList itsEvents = new EventList();
	
	public LocalCollector(IHostInfo aHost, ILocationsRepository aLocationsRepository)
	{
		super(aHost, aLocationsRepository);
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
		throw new UnsupportedOperationException();
//		return new CFlowBrowser(this, aThread, ((DefaultThreadInfo) aThread).getRootEvent());
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

	@Override
	protected void exception(int aThreadId, long aParentTimestamp, short aDepth, long aTimestamp, int aBehaviorId,
			int aOperationBytecodeIndex, Object aException)
	{
	}

	public void behaviorExit(int aThreadId, long aParentTimestamp, short aDepth, long aTimestamp,
			int aOperationBytecodeIndex, int aBehaviorId, boolean aHasThrown, Object aResult)
	{
	}

	public void fieldWrite(int aThreadId, long aParentTimestamp, short aDepth, long aTimestamp,
			int aOperationBytecodeIndex, int aFieldId, Object aTarget, Object aValue)
	{
	}

	public void instantiation(int aThreadId, long aParentTimestamp, short aDepth, long aTimestamp,
			int aOperationBytecodeIndex, boolean aDirectParent, int aCalledBehaviorId, int aExecutedBehaviorId,
			Object aTarget, Object[] aArguments)
	{
	}

	public void localWrite(int aThreadId, long aParentTimestamp, short aDepth, long aTimestamp,
			int aOperationBytecodeIndex, int aVariableId, Object aValue)
	{
	}

	public void methodCall(int aThreadId, long aParentTimestamp, short aDepth, long aTimestamp,
			int aOperationBytecodeIndex, boolean aDirectParent, int aCalledBehaviorId, int aExecutedBehaviorId,
			Object aTarget, Object[] aArguments)
	{
	}

	public void output(int aThreadId, long aParentTimestamp, short aDepth, long aTimestamp, Output aOutput, byte[] aData)
	{
	}

	public void superCall(int aThreadId, long aParentTimestamp, short aDepth, long aTimestamp,
			int aOperationBytecodeIndex, boolean aDirectParent, int aCalledBehaviorid, int aExecutedBehaviorId,
			Object aTarget, Object[] aArguments)
	{
	}
	
	
	
}
