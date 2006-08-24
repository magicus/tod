/*
 * Created on Nov 15, 2004
 */
package tod.core.database.event;

import tod.core.database.structure.IFieldInfo;

/**
 * @author gpothier
 */
public interface IFieldWriteEvent extends ICallerSideEvent
{
	/**
	 * The object on which the field is written.
	 */
	public Object getTarget();

	
	/**
	 * The written field
	 */
	public IFieldInfo getField();
	
	/**
	 * Returns the value written to the field.
	 */
	public Object getValue();
}
