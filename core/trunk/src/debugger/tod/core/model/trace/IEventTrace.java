/*
 * Created on Nov 8, 2004
 */
package tod.core.model.trace;

import tod.core.model.event.IBehaviorCallEvent;
import tod.core.model.structure.BehaviorInfo;
import tod.core.model.structure.FieldInfo;
import tod.core.model.structure.ObjectId;
import tod.core.model.structure.ThreadInfo;
import tod.core.model.structure.TypeInfo;

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
public interface IEventTrace
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
	 * Returns the registrer that maintains all location and
	 * thread info.
	 */
	public ILocationTrace getLocationTrace ();
	
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
	public IEventFilter createBehaviorCallFilter (BehaviorInfo aBehaviorInfo);
	
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
	public IEventFilter createInstantiationsFilter (TypeInfo aType);
	
	/**
	 * Creates a filter that accepts only instantiations events
	 */
	public IEventFilter createInstantiationsFilter ();
	
	/**
	 * Creates a filter that accepts only events related to a specific
	 * field.
	 */
	public IEventFilter createFieldFilter (FieldInfo aField);

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
	 * Creates a filter that accepts only the events that occurr
	 * in a specific thread.
	 */
	public IEventFilter createThreadFilter (ThreadInfo aThread);
	
	/**
	 * Creates a control flow browser.
	 */
	public ICFlowBrowser createCFlowBrowser (ThreadInfo aThread);
	
	/**
	 * Creates a filter that accepts only events that occured at a specific
	 * location in source code. 
	 */
	public IEventFilter createLocationFilter (TypeInfo aType, int aLineNumber);
	
	/**
	 * Creates an inspector that permits to evaluate the state of the specified
	 * object at any point in time.
	 */
	public IObjectInspector createObjectInspector (ObjectId aObjectId);
	
	/**
	 * Creates an inspector that permits to determine the value of a behavior's
	 * local variables at any point in time.
	 */
	public IVariablesInspector createVariablesInspector (IBehaviorCallEvent aEvent);
	
}
