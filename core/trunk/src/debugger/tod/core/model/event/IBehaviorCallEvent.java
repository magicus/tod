/*
 * Created on Nov 10, 2005
 */
package tod.core.model.event;

import tod.core.model.structure.IBehaviorInfo;

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
	 * Whether the behavior returned normally or with an
	 * exception.
	 * <br/>
	 * This information is always available.
	 */
	public boolean hasThrown();

	/**
	 * Value returned by the behavior, or exception thrown by the 
	 * behavior, according to the value of {@link #hasThrown()}.
	 */
	public Object getResult();
	
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
	

	
}
