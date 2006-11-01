/*
 * Created on Nov 9, 2004
 */
package tod.impl.common.event;

import tod.core.database.event.IArrayWriteEvent;

/**
 * @author gpothier
 */
public class ArrayWriteEvent extends Event implements IArrayWriteEvent
{
	private Object itsTarget;
	private int itsIndex;
	private Object itsValue;
	
	
	public int getIndex()
	{
		return itsIndex;
	}

	public void setIndex(int aIndex)
	{
		itsIndex = aIndex;
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
