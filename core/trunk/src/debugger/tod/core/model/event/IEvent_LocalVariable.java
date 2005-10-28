/*
 * Created on Nov 15, 2004
 */
package tod.core.model.event;

import tod.core.ILocationRegistrer;


/**
 * @author gpothier
 */
public interface IEvent_LocalVariable extends ILogEvent
{
	public ILocationRegistrer.LocalVariableInfo getVariable();
}
