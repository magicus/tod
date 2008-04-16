/*
 * Created on Mar 14, 2008
 */
package tod.agent.transport;

/**
 * Commands that can be sent by the agent to the database.
 * @author gpothier
 */
public enum Commands
{
	/**
	 * This command flushes all buffered events and indexes.
	 * args: none
	 * return:
	 *  number of flushed events: int
	 */
	CMD_FLUSH,
	
	/**
	 * This command clears the database.
	 * args: none
	 * return: none
	 */
	CMD_CLEAR;
	
	/**
	 * Base value for sending serialized commands
	 */
	public static final int BASE = 100;
	
	/**
	 * Cached values; call to values() is costly. 
	 */
	public static final Commands[] VALUES = values();

}
