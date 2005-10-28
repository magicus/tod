/*
 * Created on Nov 15, 2004
 */
package tod.core.model.event;

/**
 * @author gpothier
 */
public interface ILocalVariableWriteEvent extends IEvent_LocalVariable, IEvent_Target, IEvent_Location
{
	/**
	 * Returns the value written to the local variable.
	 */
	public Object getValue();
}
