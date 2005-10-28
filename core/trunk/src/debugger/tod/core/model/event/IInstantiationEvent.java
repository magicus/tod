/*
 * Created on Nov 15, 2004
 */
package tod.core.model.event;

import tod.core.model.structure.TypeInfo;

/**
 * @author gpothier
 */
public interface IInstantiationEvent extends ILogEvent 
{
	public TypeInfo getType();
	public Object getInstance();
}
