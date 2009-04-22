/*
 * Created on Nov 10, 2005
 */
package tod.core.database.event;

import tod.core.database.structure.IBehaviorInfo;

/**
 * This event corresponds to the call and execution of 
 * any behavior (method, constructor, ...).
 * <br/> 
 * Available information will vary depending on the instrumentation 
 * at the caller and callee sites.
 */
public interface IBehaviorCallEvent extends IParentEvent, ICallerSideEvent
{
	/**
	 * The arguments passed to the behavior.
	 * <br/>
	 * This information is always available.
	 */
	public Object[] getArguments();
	
	/**
	 * The behavior that is actually executed. 
	 * It might be different than {@link #getCalledBehavior() },
	 * for instance if the caller calls an interface or overridden method.
	 * <br/>
	 * This information is always available.
	 */
	public IBehaviorInfo getExecutedBehavior();
	
	/**
	 * The called behavior.
	 * <br/>
	 * This information is available only if the caller behavior
	 * was instrumented.
	 */
	public IBehaviorInfo getCalledBehavior();
	
	/**
	 * The object on which the behavior was called, or
	 * null if static.
	 * <br/>
	 * This information is always available.
	 */
	public Object getTarget();
	
	/**
	 * The behavior that requested the call.
	 * <br/>
	 * This information is available only if the caller behavior
	 * was instrumented.
	 * @return Calling behavior, or null if not available
	 */
	public IBehaviorInfo getCallingBehavior();
	
	/**
	 * Returns the event that corresponds to the end of this behavior.
	 */
	public IBehaviorExitEvent getExitEvent();
	

	
}
