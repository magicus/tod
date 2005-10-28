/*
 * Created on Nov 9, 2004
 */
package reflex.lib.logging.miner.impl.common.event;

import tod.core.ILocationRegistrer;
import tod.core.model.event.ILocalVariableWriteEvent;

/**
 * @author gpothier
 */
public class LocalVariableWriteEvent extends Event implements ILocalVariableWriteEvent
{
	private ILocationRegistrer.LocalVariableInfo itsVariable;
	private Object itsTarget;
	private Object itsValue;
	
	public ILocationRegistrer.LocalVariableInfo getVariable()
	{
		return itsVariable;
	}

	public void setVariable(ILocationRegistrer.LocalVariableInfo aVariable)
	{
		itsVariable = aVariable;
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
