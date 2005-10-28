/*
 * Created on Nov 15, 2004
 */
package tod.core.model.event;

import tod.core.model.structure.FieldInfo;

/**
 * @author gpothier
 */
public interface IEvent_Field extends ILogEvent
{
	public FieldInfo getField();
}
