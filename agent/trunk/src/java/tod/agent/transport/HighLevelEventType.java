/*
 * Created on Mar 14, 2008
 */
package tod.agent.transport;

/**
 * Enumeration of all possible high-level event types. High level events
 * are those that have been processed by {@link EventInterpreter}.
 * @author gpothier
 */
public enum HighLevelEventType 
{
	// Events
	INSTANTIATION,
	NEW_ARRAY,
	SUPER_CALL,
	METHOD_CALL,
	BEHAVIOR_EXIT,
	EXCEPTION,
	FIELD_WRITE,
	ARRAY_WRITE,
	LOCAL_VARIABLE_WRITE,
	OUTPUT,
	
	// Registering
	REGISTER_THREAD;
	
	/**
	 * Cached values; call to values() is costly. 
	 */
	public static final HighLevelEventType[] VALUES = values();
}
