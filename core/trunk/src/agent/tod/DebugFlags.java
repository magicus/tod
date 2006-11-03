/*
 * Created on Oct 24, 2006
 */
package tod;

import tod.core.EventInterpreter;

/**
 * This class groups severla flags that are used to
 * disable certain features for testing purposes.
 * 
 * @author gpothier
 */
public class DebugFlags
{
	/**
	 * Causes database nodes to skip incoming events
	 */
	public static final boolean SKIP_EVENTS = false; 
	
	/**
	 * Causes database nodes to not reorder incoming events
	 */
	public static final boolean DISABLE_REORDER = false; 
	
	/**
	 * Causes database nodes to not index incoming events.
	 */
	public static final boolean DISABLE_INDEXES = false; 
	
	/**
	 * Causes {@link HardPagedFile} to not store pages.
	 */
	public static final boolean DISABLE_STORE = false;
	
	/**
	 * Artificially limits the number of of object indexes.
	 * Set to 0 to disable the limit
	 */
	public static final int ALIAS_OBJECTS = 0;//150000;
	
	/**
	 * Disables the bytecode location index.
	 */
	public static final boolean DISABLE_LOCATION_INDEX = false;
	
	/**
	 * Disables asynchronous file writes in {@link HardPagedFile}
	 */
	public static final boolean DISABLE_ASYNC_WRITES = true;
	
	/**
	 * if true, pages are not explicitly marked as used when accessed.
	 */
	public static final boolean DISABLE_USE_PAGES = false;

	/**
	 * Causes the {@link EventInterpreter} to ignore all events
	 */
	public static final boolean DISABLE_INTERPRETER = false;

	/**
	 * If true, the {@link EventInterpreter} prints all the events it receives
	 */
	public static final boolean EVENT_INTERPRETER_LOG = false;

	/**
	 * Causes the high level collectors to ignore all events
	 */
	public static final boolean COLLECTOR_IGNORE_ALL = false;

	/**
	 * Causes the socket collector to not send events
	 */
	public static final boolean DISABLE_EVENT_SEND = false;
}
