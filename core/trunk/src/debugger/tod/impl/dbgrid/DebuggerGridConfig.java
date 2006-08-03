/*
 * Created on Jul 21, 2006
 */
package tod.impl.dbgrid;

import tod.impl.dbgrid.dbnode.EventList;
import tod.impl.dbgrid.dbnode.HierarchicalIndex;
import tod.impl.dbgrid.messages.EventType;
import zz.utils.bit.BitUtils;

public class DebuggerGridConfig
{
	/**
	 * Maximum number of event types 
	 */
	public static final int STRUCTURE_TYPE_COUNT = EventType.values().length;

	/**
	 * Maximum number of hosts
	 */
	public static final int STRUCTURE_HOSTS_COUNT = 100;

	/**
	 * Maximum number of threads
	 */
	public static final int STRUCTURE_THREADS_COUNT = 10000;

	/**
	 * Maximum number of bytecode locations
	 */
	public static final int STRUCTURE_BYTECODE_LOCS_COUNT = 1000;

	/**
	 * Maximum number of behaviors
	 */
	public static final int STRUCTURE_BEHAVIOR_COUNT = 10000;

	/**
	 * Maximum number of fields
	 */
	public static final int STRUCTURE_FIELD_COUNT = 10000;

	/**
	 * Maximum number of variable indexes
	 */
	public static final int STRUCTURE_VAR_COUNT = 1000;

	/**
	 * Maximum number of 
	 */
	public static final int STRUCTURE_OBJECT_COUNT = 10000;


	/**
	 * Number of bits to shift timestamp values.
	 */
	public static final int TIMESTAMP_ADJUST_SHIFT = 8;
	
	/**
	 * Number of bits of original timestamp values that are considered inaccurate.
	 */
	public static final int TIMESTAMP_ADJUST_INACCURACY = 4;
	
	/**
	 * Mask of artificial timestamp bits.
	 */
	public static final long TIMESTAMP_ADJUST_MASK = BitUtils.pow2(TIMESTAMP_ADJUST_INACCURACY+TIMESTAMP_ADJUST_SHIFT)-1;
	
	/**
	 * Number of bits used to represent the owner node of an event.
	 */
	public static final int EVENT_NODE_BITS = 6;
	
	/**
	 * Number of bits used to represent the host of an event.
	 */
	public static final int EVENT_HOST_BITS = 10;
	
	/**
	 * Number of bits used to represent the thread of an event.
	 */
	public static final int EVENT_THREAD_BITS = 16;
	
	/**
	 * Number of bits used to represent the serial number of an event.
	 */
	public static final int EVENT_TIMESTAMP_BITS = 64; 
	
	/**
	 * Number of bits used to represent a behavior id in an event.
	 */
	public static final int EVENT_BEHAVIOR_BITS = 16; 
	
	/**
	 * Number of bits used to represent a field id in an event.
	 */
	public static final int EVENT_FIELD_BITS = 16; 
	
	/**
	 * Number of bits used to represent a variable id in an event.
	 */
	public static final int EVENT_VARIABLE_BITS = 16; 
	
	/**
	 * Number of bits used to represent the bytecode location of an event
	 */
	public static final int EVENT_BYTECODE_LOCATION_BITS = 16; 
	
	/**
	 * Number of bits used to represent the number of arguments of a behavior call.
	 */
	public static final int EVENT_ARGS_COUNT_BITS = 8; 
	
	/**
	 * Number of bits used to represent the event type
	 */
	public static final int EVENT_TYPE_BITS = (int) Math.ceil(Math.log(EventType.values().length)/Math.log(2)); 
	
	/**
	 * Number of bits necessary to represent an external event pointer.
	 */
	public static final int EVENTID_POINTER_SIZE = 
		DebuggerGridConfig.EVENT_NODE_BITS
		+DebuggerGridConfig.EVENT_HOST_BITS
		+DebuggerGridConfig.EVENT_THREAD_BITS
		+DebuggerGridConfig.EVENT_TIMESTAMP_BITS;

	/**
	 * Size of an event page in the database
	 */
	public static final int DB_EVENT_PAGE_SIZE = 8192;
	
	/**
	 * Size of an event page in the database for writing
	 */
	public static final int DB_EVENT_WRITE_PAGE_SIZE = 65536;
	
	/**
	 * Size of an index page in the database
	 */
	public static final int DB_INDEX_PAGE_SIZE = 4096;
	
	/**
	 * Number of bits to represent a page pointer in {@link HierarchicalIndex}
	 * and {@link EventList}.
	 */
	public static final int DB_PAGE_POINTER_BITS = 32;
	
	/**
	 * Size of an index page in the database for writing
	 */
	public static final int DB_INDEX_WRITE_PAGE_SIZE = 8192;
	
	/**
	 * Average event size.
	 */
	public static final int DB_AVG_EVENT_SIZE = 40;
	
	public static final int DB_AVG_EVENTS_PER_PAGE = DB_EVENT_PAGE_SIZE/DB_AVG_EVENT_SIZE;
	
	/**
	 * Number of bits used to represent the record index in an internal
	 * event pointer.
	 */
	public static final int DB_EVENTID_INDEX_BITS = 
		(int) Math.ceil(Math.log(DB_AVG_EVENTS_PER_PAGE)/Math.log(2)) + 1;
	
	/**
	 * Number of bits used to represent the page in an internal
	 * event pointer.
	 */
	public static final int DB_EVENTID_PAGE_BITS = 64 - DB_EVENTID_INDEX_BITS;
	
	/**
	 * Number of bits used to represent event sizes in event pages.
	 */
	public static final int DB_EVENT_SIZE_BITS = 16;
	
	/**
	 * Maximum number of index levels for {@link HierarchicalIndex}.
	 */
	public static final int DB_MAX_INDEX_LEVELS = 6;
	
}
