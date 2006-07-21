/*
 * Created on Nov 9, 2004
 */
package tod.impl.common.event;

import tod.core.model.event.IFieldWriteEvent;
import tod.core.model.structure.IFieldInfo;

/**
 * @author gpothier
 */
public class FieldWriteEvent extends Event implements IFieldWriteEvent
{
	private IFieldInfo itsFieldInfo;
	private Object itsTarget;
	private Object itsValue;
	
	public IFieldInfo getField()
	{
		return itsFieldInfo;
	}
	
	public void setField(IFieldInfo aFieldInfo)
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
