/*
 * Created on Nov 15, 2004
 */
package tod.core.model.event;

import tod.core.model.structure.IFieldInfo;

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