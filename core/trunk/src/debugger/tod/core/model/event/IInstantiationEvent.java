/*
 * Created on Nov 15, 2004
 */
package tod.core.model.event;

import tod.core.model.structure.TypeInfo;

/**
 * @author gpothier
 */
public interface IInstantiationEvent extends IBehaviorCallEvent
{
	/**
	 * The instanciated type
	 */
	public TypeInfo getType();

	/**
	 * The resulting instance.
	 * Same as {@link ICallerSideEvent#getTarget()}
	 */
	public Object getInstance();
}
