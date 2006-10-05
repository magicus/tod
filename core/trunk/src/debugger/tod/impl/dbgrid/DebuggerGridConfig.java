/*
 * Created on Jul 21, 2006
 */
package tod.impl.dbgrid;

import tod.impl.dbgrid.dbnode.DatabaseNode;
import tod.impl.dbgrid.dbnode.HierarchicalIndex;
import tod.impl.dbgrid.dbnode.file.TupleIterator;
import tod.impl.dbgrid.dbnode.file.TupleWriter;
import tod.impl.dbgrid.messages.MessageType;
import zz.utils.bit.BitUtils;

public class DebuggerGridConfig
{
	/**
	 * Number of bits used to represent the host of an event.
	 */
	public static final int EVENT_HOST_BITS = 10;
	
	/**
	 * Number of bits used to represent the thread of an event.
	 */
	public static final int EVENT_THREAD_BITS = 16;
	
	/**
	 * Number of bits used to represent the depth of an event.
	 */
	public static final int EVENT_DEPTH_BITS = 12;
	
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
	 * Port at which database nodes connect to the master.
	 */
	public static final int MASTER_NODE_PORT = 8060;
	
	/**
	 * Number of array slots in the master's event buffer.
	 */
	public static final int MASTER_EVENT_BUFFER_SIZE = 4096;
	
	/**
	 * Size of the {@link DatabaseNode} reordering event buffer
	 */
	public static final int DB_EVENT_BUFFER_SIZE = 50000;
	
	/**
	 * Maximum number of event types 
	 */
	public static final int STRUCTURE_TYPE_COUNT = 40000;

	/**
	 * Maximum number of hosts
	 */
	public static final int STRUCTURE_HOSTS_COUNT = 100;

	/**
	 * Maximum number of threads
	 */
	public static final int STRUCTURE_THREADS_COUNT = 10000;

	/**
	 * Maximum number of threads
	 */
	public static final int STRUCTURE_DEPTH_RANGE = BitUtils.pow2i(EVENT_DEPTH_BITS);
	
	/**
	 * Maximum number of bytecode locations
	 */
	public static final int STRUCTURE_BYTECODE_LOCS_COUNT = 65536;

	/**
	 * Maximum number of behaviors
	 */
	public static final int STRUCTURE_BEHAVIOR_COUNT = 100000;

	/**
	 * Maximum number of fields
	 */
	public static final int STRUCTURE_FIELD_COUNT = 100000;

	/**
	 * Maximum number of variable indexes
	 */
	public static final int STRUCTURE_VAR_COUNT = 1000;

	/**
	 * Maximum number of objects 
	 */
	public static final int STRUCTURE_OBJECT_COUNT = 1000000;


	/**
	 * Number of bits used to represent the message type
	 */
	public static final int MESSAGE_TYPE_BITS = BitUtils.log2ceil(MessageType.values().length); 
	
	/**
	 * Number of bits necessary to represent an external event pointer.
	 */
	public static final int EVENTID_POINTER_SIZE = 
		+DebuggerGridConfig.EVENT_HOST_BITS
		+DebuggerGridConfig.EVENT_THREAD_BITS
		+DebuggerGridConfig.EVENT_TIMESTAMP_BITS;

	/**
	 * Size of an event page in the database
	 */
	public static final int DB_EVENT_PAGE_SIZE = 8192;
	
	/**
	 * Size of an index page in the database
	 */
	public static final int DB_INDEX_PAGE_SIZE = 4096;
	
	/**
	 * Size of a cflow data page in the database
	 */
	public static final int DB_CFLOW_PAGE_SIZE = 4096;
	
	/**
	 * Minimum page size for CFlow data pages
	 */
	public static final int DB_MIN_CFLOW_PAGE_SIZE = 128;
	
	/**
	 * Size of the children list buffer in {@link CFlowMap}.
	 */
	public static final int DB_CFLOW_CHILDREN_LIST_BUFFER_SIZE = 256;
	
	/**
	 * Size of a btree page 
	 */
	public static final int DB_BTREE_PAGE_SIZE = 8192;
	
	/**
	 * Number of bits to represent a page pointer in a linked pages list,
	 * as used by {@link TupleWriter} and {@link TupleIterator}
	 */
	public static final int DB_PAGE_POINTER_BITS = 32;
	
	/**
	 * Average event size.
	 */
	public static final int DB_AVG_EVENT_SIZE = 55;
	
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
	
	/**
	 * Number of events to fetch at a time 
	 */
	public static final int QUERY_ITERATOR_BUFFER_SIZE = 10;
}
