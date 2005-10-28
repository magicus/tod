/*
 * Created on Nov 9, 2004
 */
package reflex.lib.logging.miner.impl.common.event;

import tod.core.model.event.IFieldWriteEvent;
import tod.core.model.structure.FieldInfo;

/**
 * @author gpothier
 */
public class FieldWriteEvent extends Event implements IFieldWriteEvent
{
	private FieldInfo itsFieldInfo;
	private Object itsTarget;
	private Object itsValue;
	
	public FieldInfo getField()
	{
		return itsFieldInfo;
	}
	
	public void setField(FieldInfo aFieldInfo)
	{
		itsFieldInfo = aFieldInfo;
	}
	
	public Object getTarget()
	{
		return itsTarget;
	}

	public void setTarget(Object aTarget)
	{
		itsTarget = aTarget;
	}

	public Object getValue()
	{
		return itsValue;
	}
	
	public void setValue(Object aValue)
	{
		itsValue = aValue;
	}
}
