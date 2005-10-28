/*
 * Created on Nov 17, 2004
 */
package reflex.lib.logging.miner.impl.common.event;

import tod.core.model.event.ILogEvent;

/**
 * This interface is implemented by all miner events.
 * It adds depth and serial information to the basic log event.
 * @author gpothier
 */
public interface IMinerEvent extends ILogEvent
{
	/**
	 * Returns the depth of this event in its thread's call stack
	 */
	public int getDepth();
	
}
