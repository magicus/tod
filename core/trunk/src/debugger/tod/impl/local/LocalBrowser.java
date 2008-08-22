/*
TOD - Trace Oriented Debugger.
Copyright (c) 2006-2008, Guillaume Pothier
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this 
      list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, 
      this list of conditions and the following disclaimer in the documentation 
      and/or other materials provided with the distribution.
    * Neither the name of the University of Chile nor the names of its contributors 
      may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
POSSIBILITY OF SUCH DAMAGE.

Parts of this work rely on the MD5 algorithm "derived from the RSA Data Security, 
Inc. MD5 Message-Digest Algorithm".
*/
package tod.impl.local;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tod.core.database.browser.ICompoundFilter;
import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.IEventFilter;
import tod.core.database.browser.IEventPredicate;
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
import tod.core.database.structure.IMutableStructureDatabase;
import tod.core.database.structure.IStructureDatabase;
import tod.core.database.structure.IThreadInfo;
import tod.core.database.structure.ITypeInfo;
import tod.core.database.structure.ObjectId;
import tod.core.database.structure.IBehaviorInfo.BytecodeRole;
import tod.core.database.structure.IStructureDatabase.LocalVariableInfo;
import tod.core.session.ISession;
import tod.impl.common.LogBrowserUtils;
import tod.impl.common.ObjectInspector;
import tod.impl.common.VariablesInspector;
import tod.impl.common.event.Event;
import tod.impl.database.IBidiIterator;
import tod.impl.local.filter.AbstractFilter;
import tod.impl.local.filter.AdviceCFlowFilter;
import tod.impl.local.filter.AdviceSourceIdFilter;
import tod.impl.local.filter.BehaviorCallFilter;
import tod.impl.local.filter.DepthFilter;
import tod.impl.local.filter.ExceptionGeneratedFilter;
import tod.impl.local.filter.FieldWriteFilter;
import tod.impl.local.filter.HostFilter;
import tod.impl.local.filter.InstantiationFilter;
import tod.impl.local.filter.IntersectionFilter;
import tod.impl.local.filter.ObjectFilter;
import tod.impl.local.filter.OperationLocationFilter;
import tod.impl.local.filter.RoleFilter;
import tod.impl.local.filter.TargetFilter;
import tod.impl.local.filter.ThreadFilter;
import tod.impl.local.filter.UnionFilter;
import tod.impl.local.filter.VariableWriteFilter;

public class LocalBrowser implements ILogBrowser
{
	private final ISession itsSession;
	private final EventList itsEvents = new EventList();
	private final List<IHostInfo> itsHosts = new ArrayList<IHostInfo>();
	private final Map<String, IHostInfo> itsHostsMap = new HashMap<String, IHostInfo>();
	private final List<IThreadInfo> itsThreads = new ArrayList<IThreadInfo>();
	private final IMutableStructureDatabase itsStructureDatabase;
	
	/**
	 * Temporary. Holds registered objects.
	 */
	private Map<Long, Object> itsRegisteredObjects = new HashMap<Long, Object>();
	
	public LocalBrowser(ISession aSession, IMutableStructureDatabase aStructureDatabase)
	{
		itsSession = aSession;
		itsStructureDatabase = aStructureDatabase;
	}

	public ISession getSession()
	{
		return itsSession;
	}
	
	public IMutableStructureDatabase getStructureDatabase()
	{
		return itsStructureDatabase;
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
	
	public long getDroppedEventsCount()
	{
		throw new UnsupportedOperationException();
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

	public IEventFilter createPredicateFilter(IEventPredicate aPredicate, IEventFilter aBaseFilter)
	{
		throw new UnsupportedOperationException();
	}

	public IEventFilter createArgumentFilter(ObjectId aId)
	{
		throw new UnsupportedOperationException();
	}
	
	public IEventFilter createArgumentFilter(ObjectId aId, int aPosition)
	{
		throw new UnsupportedOperationException();
	}

	public IEventFilter createExceptionGeneratedFilter()
	{
		return new ExceptionGeneratedFilter(this);
	}

	public IEventFilter createLocationFilter(IBehaviorInfo aBehavior, int aBytecodeIndex)
	{
		return new OperationLocationFilter(this, aBehavior, aBytecodeIndex);
	}

	public IEventFilter createAdviceSourceIdFilter(int aAdviceSourceId)
	{
		return new AdviceSourceIdFilter(this, aAdviceSourceId);
	}

	public IEventFilter createAdviceCFlowFilter(int aAdviceSourceId)
	{
		return new AdviceCFlowFilter(this, aAdviceSourceId);
	}

	public IEventFilter createRoleFilter(BytecodeRole aRole)
	{
		return new RoleFilter(this, aRole);
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
	
	public IEventFilter createVariableWriteFilter()
	{
		return new VariableWriteFilter(this);
	}
	
	public IEventFilter createArrayWriteFilter()
	{
		throw new UnsupportedOperationException();
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

	public IEventFilter createEventFilter(ILogEvent aEvent)
	{
		throw new UnsupportedOperationException();
	}

	public IEventFilter createResultFilter(ObjectId aId)
	{
		throw new UnsupportedOperationException();
	}

	public IEventFilter createValueFilter(ObjectId aId)
	{
		throw new UnsupportedOperationException();
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
	
	public IBidiIterator<Long> searchStrings(String aSearchText)
	{
		throw new UnsupportedOperationException();
	}

	public <O> O exec(Query<O> aQuery)
	{
		return aQuery.run(this);
	}
	
	
	
}
