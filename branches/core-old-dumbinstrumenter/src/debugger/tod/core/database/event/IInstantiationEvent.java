/*
 * Created on Nov 15, 2004
 */
package tod.core.database.event;

import tod.core.database.structure.ITypeInfo;

/**
 * @author gpothier
 */
public interface IInstantiationEvent extends IBehaviorCallEvent
{
	/**
	 * The instanciated type
	 */
	public ITypeInfo getType();

	/**
	 * The resulting instance.
	 * Same as {@link ICallerSideEvent#getTarget()}
	 */
	public Object getInstance();
}
