/*
 * Created on Mar 14, 2008
 */
package tod.agent.transport;

/**
 * Enumerates all possible value types.
 * @author gpothier
 */
public enum ValueType 
{
	// Primitives values
	NULL, BOOLEAN, BYTE, CHAR, SHORT, INT, LONG, FLOAT, DOUBLE,
	
	// Objects
	OBJECT_UID;
	
	/**
	 * Cached values; call to values() is costly. 
	 */
	public static final ValueType[] VALUES = values();
}
