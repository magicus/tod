/*
 * Created on Nov 9, 2004
 */
package tod.impl.common.event;

import tod.core.ILocationRegistrer;
import tod.core.database.event.ILocalVariableWriteEvent;

/**
 * @author gpothier
 */
public class LocalVariableWriteEvent extends Event implements ILocalVariableWriteEvent
{
	private ILocationRegistrer.LocalVariableInfo itsVariable;
	private Object itsValue;
	
	public ILocationRegistrer.LocalVariableInfo getVariable()
	{
		return itsVariable;
	}

	public void setVariable(ILocationRegistrer.LocalVariableInfo aVariable)
	{
		itsVariable = aVariable;
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
