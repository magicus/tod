/*
 * Created on Sep 5, 2005
 */
package tod.core.model.event;


/**
 * Interface for events whose location in source code can be determined.
 * @author gpothier
 */
public interface IEvent_Location extends ILogEvent
{
	public int getOperationBytecodeIndex();
}
