/*
 * Created on Oct 9, 2004
 */
package reflex.lib.logging.miner.impl.sql;

/**
 * @author gpothier
 */
public enum EventType
{
	INSTANTIATION,
	BEFORE_METHOD_CALL,
	AFTER_METHOD_CALL,
	BEHAVIOUR_ENTER,
	BEHAVIOUR_EXIT,
	FIELD_WRITE
}
