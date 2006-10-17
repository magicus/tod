/*
 * Created on Nov 15, 2004
 */
package tod.core.database.event;

import tod.core.database.structure.IHostInfo;
import tod.core.database.structure.IThreadInfo;

/**
 * Root of the interface graph of logging events.
 * @author gpothier
 */
public interface ILogEvent
{
	/**
	 * Identifies the host in which the event occurred.
	 */
	public IHostInfo getHost();
	
	/**
	 * Identifies the thread in which the event occured.
	 */
	public IThreadInfo getThread();
	
	/**
	 * Depth of this event in its control flow stack.
	 */
	public int getDepth();
	
	/**
	 * Timestamp of the event. Its absolute value has no
	 * meaning, but the difference between two timestamps
	 * is a duration in nanoseconds.
	 */
	public long getTimestamp();
	
	/**
	 * Returns behavior call event corresponding to the behavior execution
	 * during which this event occured.
	 */
	public IBehaviorCallEvent getParent();
}
