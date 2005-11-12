/*
 * Created on Nov 15, 2004
 */
package tod.core.model.event;

import tod.core.model.structure.ThreadInfo;

/**
 * Root of the interface graph of logging events.
 * @author gpothier
 */
public interface ILogEvent
{
	/**
	 * Identifies the thread in which the event occured.
	 */
	public ThreadInfo getThread();
	
	/**
	 * Timestamp of the event. Its absolute value has no
	 * meaning, but the difference between two timestamps
	 * is a duration in nanoseconds.
	 */
	public long getTimestamp();
	
	/**
	 * Returns the serial number of this event.
	 * Events in a thread have successive serial numbers.
	 */
	public long getSerial();
	
	/**
	 * Returns behavior call event corresponding to the behavior execution
	 * during which this event occured.
	 */
	public IBehaviorCallEvent getParent();
}
