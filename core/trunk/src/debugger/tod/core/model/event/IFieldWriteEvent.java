/*
 * Created on Nov 15, 2004
 */
package tod.core.model.event;

/**
 * @author gpothier
 */
public interface IFieldWriteEvent extends IEvent_Field, IEvent_Target, IEvent_Location
{
	/**
	 * Returns the value written to the field.
	 */
	public Object getValue();
}
