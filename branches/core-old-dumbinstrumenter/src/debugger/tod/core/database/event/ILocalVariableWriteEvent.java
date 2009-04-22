/*
 * Created on Nov 15, 2004
 */
package tod.core.database.event;

import tod.core.ILocationRegistrer;

/**
 * @author gpothier
 */
public interface ILocalVariableWriteEvent extends ICallerSideEvent
{
	/**
	 * The written variable
	 */
	public ILocationRegistrer.LocalVariableInfo getVariable();
	
	/**
	 * Returns the value written to the local variable.
	 */
	public Object getValue();
	
}
