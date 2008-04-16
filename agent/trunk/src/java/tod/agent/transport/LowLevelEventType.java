/*
 * Created on Mar 14, 2008
 */
package tod.agent.transport;


/**
 * All possible low level event types. Low level events are those that are directly generated 
 * by instrumented code.
 * @author gpothier
 */
public enum LowLevelEventType 
{
	CLINIT_ENTER,
	BEHAVIOR_ENTER,
	CLINIT_EXIT,
	BEHAVIOR_EXIT,
	BEHAVIOR_EXIT_EXCEPTION,
	EXCEPTION_GENERATED,
	FIELD_WRITE,
	NEW_ARRAY,
	ARRAY_WRITE,
	LOCAL_VARIABLE_WRITE,
	INSTANCEOF,
	BEFORE_CALL_DRY,
	BEFORE_CALL,
	AFTER_CALL_DRY,
	AFTER_CALL,
	AFTER_CALL_EXCEPTION,
	OUTPUT,
	
	
	// Registering
	REGISTER_OBJECT,
	REGISTER_THREAD;
	
	/**
	 * Cached values; call to values() is costly. 
	 */
	public static final LowLevelEventType[] VALUES = values();
}
