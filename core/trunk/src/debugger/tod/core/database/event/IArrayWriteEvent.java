/*
 * Created on Nov 15, 2004
 */
package tod.core.database.event;

import tod.core.database.structure.IFieldInfo;

/**
 * @author gpothier
 */
public interface IArrayWriteEvent extends ICallerSideEvent
{
	/**
	 * The array that is written
	 */
	public Object getTarget();

	
	/**
	 * The written index
	 */
	public int getIndex();
	
	/**
	 * Returns the value written to the array slot.
	 */
	public Object getValue();
}
