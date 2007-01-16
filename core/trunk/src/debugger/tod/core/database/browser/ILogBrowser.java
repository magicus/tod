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
package tod.core.database.browser;

import tod.core.database.event.ExternalPointer;
import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IBehaviorInfo;
import tod.core.database.structure.IClassInfo;
import tod.core.database.structure.IFieldInfo;
import tod.core.database.structure.IHostInfo;
import tod.core.database.structure.IThreadInfo;
import tod.core.database.structure.ITypeInfo;
import tod.core.database.structure.ObjectId;

/**
 * Interface to the trace database.
 * This is the entry point to the log browser. It permits to
 * obtain event browsers and filters.
 * <li>An event browser is an iterator-like object that 
 * permits to navigate backwards and forwards in the stream of
 * logged events, reporting only events that pass a specific filter.
 * <li>The filter creation methods provide basic filters which can
 * be combined in order to create higher-level filters.
 * See {@link #createIntersectionFilter()} and {@link #createUnionFilter()}.
 * @author gpothier
 */
public interface ILogBrowser
{
	/**
	 * Clears all the events and other information from this log.
	 */
	public void clear();
	
	/**
	 * Returns the timestamp of the first event recorded in this log.
	 */
	public long getFirstTimestamp();
	
	/**
	 * Returns the timestamp of the last event recorded in this log.
	 */
	public long getLastTimestamp();
	
	/**
	 * Returns the total number of logged events.
	 */
	public long getEventsCount();
	
	/**
	 * Returns the event pointed to by the specified pointer.
	 */
	public ILogEvent getEvent(ExternalPointer aPointer);
	
	/**
	 * Returns the registrer that maintains all location and
	 * thread info.
	 */
	public ILocationsRepository getLocationsRepository ();
	
	/**
	 * Returns all registered threads.
	 */
	public Iterable<IThreadInfo> getThreads();
	
	/**
	 * Returns all registered hosts.
	 * The list can be indexed by host id, which also means that the first element
	 * (index 0) is null.
	 */
	public Iterable<IHostInfo> getHosts();
	
	/**
	 * Returns the registered object (eg. string) that corresponds to the
	 * given object id.
	 */
	public Object getRegistered(long aId);
	
	/**
	 * Creates a browser that only reports events that pass a specific
	 * filter.
	 */
	public IEventBrowser createBrowser (IEventFilter aFilter);
	
	/**
	 * Creates an empty union filter.
	 */
	public ICompoundFilter createUnionFilter (IEventFilter... aFilters);
	
	/**
	 * Creates an empty intersection filter.
	 */
	public ICompoundFilter createIntersectionFilter (IEventFilter... aFilters);

	/**
	 * Creates a filter that accepts only behavior call events
	 * related to a specific behavior.
	 */
	public IEventFilter createBehaviorCallFilter (IBehaviorInfo aBehavior);
	
	/**
	 * Creates a filter that accepts only behavior call events (before call and after call).
	 */
	public IEventFilter createBehaviorCallFilter ();
	
	/**
	 * Creates a filter that accepts only the instantiation of the 
	 * given object. Note that in the case of ambiguous object id,
	 * the filter can accept various instantiation events.
	 */
	public IEventFilter createInstantiationFilter (ObjectId aObjectId);
	
	/**
	 * Creates a filter that accepts only the instantiations of
	 * the specified type.
	 */
	public IEventFilter createInstantiationsFilter (ITypeInfo aType);
	
	/**
	 * Creates a filter that accepts only instantiations events
	 */
	public IEventFilter createInstantiationsFilter ();
	
	/**
	 * Creates a filter that accepts only events related to a specific
	 * field.
	 */
	public IEventFilter createFieldFilter (IFieldInfo aField);

	/**
	 * Creates a filter that accepts only field write events
	 */
	public IEventFilter createFieldWriteFilter ();
	
	/**
	 * Creates a filter that accepts only the events whose target
	 * is the specified object reference.
	 */
	public IEventFilter createTargetFilter (ObjectId aId);
	
	/**
	 * Creates a filter that accepts only the events 
	 * (behaviour calls and field writes) whose argument
	 * is the specified object reference.
	 */
	public IEventFilter createArgumentFilter (ObjectId aId);
	
	/**
	 * Creates a filter that accepts any event that refers to
	 * the specified object.
	 */
	public IEventFilter createObjectFilter(ObjectId aId);

	
	/**
	 * Creates a filter that accepts only events on the given host.
	 */
	public IEventFilter createHostFilter (IHostInfo aHost);
	
	/**
	 * Creates a filter that accepts only the events that occurr
	 * in a specific thread.
	 */
	public IEventFilter createThreadFilter (IThreadInfo aThread);
	
	/**
	 * Creates a filter that accepts only events that have the
	 * specified call depth.
	 */
	public IEventFilter createDepthFilter(int aDepth);
	
	/**
	 * Creates a control flow browser.
	 */
	public ICFlowBrowser createCFlowBrowser (IThreadInfo aThread);
	
	/**
	 * Creates a filter that accepts only events that occured at a specific
	 * location in source code. 
	 */
	public IEventFilter createLocationFilter (ITypeInfo aType, int aLineNumber);
	
	/**
	 * Creates a filter that accepts only exception generated events.
	 */
	public IEventFilter createExceptionGeneratedFilter();
	
	/**
	 * Creates an inspector that permits to evaluate the state of the specified
	 * object at any point in time.
	 */
	public IObjectInspector createObjectInspector (ObjectId aObjectId);
	
	/**
	 * Creates an inspector that permits to evaluate the state of the specified
	 * type's static fields at any point in time.
	 */
	public IObjectInspector createClassInspector (IClassInfo aClass);
	
	/**
	 * Creates an inspector that permits to determine the value of a behavior's
	 * local variables at any point in time.
	 */
	public IVariablesInspector createVariablesInspector (IBehaviorCallEvent aEvent);
	
}
