/*
 * Created on Oct 25, 2004
 */
package reflex.lib.logging.miner.impl.common.event;

import tod.core.model.event.IInstantiationEvent;
import tod.core.model.structure.TypeInfo;

/**
 * @author gpothier
 */
public class Instantiation extends Event implements IInstantiationEvent
{
	private TypeInfo itsTypeInfo;
	private Object itsInstance;

	public Object getInstance()
	{
		return itsInstance;
	}
	
	public void setInstance(Object aInstance)
	{
		itsInstance = aInstance;
	}
	
	public TypeInfo getType()
	{
		return itsTypeInfo;
	}
	
	public void setType(TypeInfo aTypeInfo)
	{
		itsTypeInfo = aTypeInfo;
	}
}
