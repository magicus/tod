/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier (gpothier@dcc.uchile.cl)

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

Parts of this work rely on the MD5 algorithm "derived from the 
RSA Data Security, Inc. MD5 Message-Digest Algorithm".
*/
package tod.impl.dbgrid;

import tod.agent.ConfigUtils;
import tod.impl.dbgrid.db.HierarchicalIndex;
import tod.impl.dbgrid.db.ObjectsDatabase;
import tod.impl.dbgrid.db.file.HardPagedFile;
import tod.impl.dbgrid.db.file.TupleIterator;
import tod.impl.dbgrid.db.file.TupleWriter;
import tod.impl.dbgrid.dispatch.DatabaseNode;
import tod.impl.dbgrid.messages.MessageType;
import zz.utils.bit.BitUtils;

public class DebuggerGridConfig
{
	/**
	 * Number of bits used to represent the host of an event.
	 */
	public static final int EVENT_HOST_BITS = 8;
	
	public static final long EVENT_HOST_MASK = BitUtils.pow2(EVENT_HOST_BITS)-1;
	
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
	public static int DB_REORDER_BUFFER_SIZE = 
		ConfigUtils.readInt("reorder-buffer-size", 100000);
	
	public static int DB_PERTHREAD_REORDER_BUFFER_SIZE = 
		ConfigUtils.readInt("perthread-reorder-buffer-size", DB_REORDER_BUFFER_SIZE);
	
	/**
	 * Size of the object reordering buffer for {@link ObjectsDatabase}.
	 */
	public static final int DB_OBJECTS_BUFFER_SIZE = 1000;
	
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
	 * Maximum number of different depths
	 */
	public static final int STRUCTURE_DEPTH_RANGE = BitUtils.pow2i(EVENT_DEPTH_BITS);
	
	/**
	 * Maximum number of bytecode locations
	 */
	public static final int STRUCTURE_BYTECODE_LOCS_COUNT = 65536;

	/**
	 * Maximum number of behaviors
	 */
	public static final int STRUCTURE_BEHAVIOR_COUNT = 200000;

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
	public static final int STRUCTURE_OBJECT_COUNT = BitUtils.pow2i(14);
	
	/**
	 * Maximum number of array indexes.
	 */
	public static final int STRUCTURE_ARRAY_INDEX_COUNT = BitUtils.pow2i(14);
	
	/**
	 * Number of partitions of key values for the objects index.
	 * @see SplittedConditionHandler
	 */
	public static final int[] INDEX_OBJECT_PARTS = {12, 12};

	/**
	 * Number of partitions of key values for the array index index.
	 * @see SplittedConditionHandler
	 */
	public static final int[] INDEX_ARRAY_INDEX_PARTS = {14, 14};
	

	/**
	 * Number of bits used to represent the message type
	 */
	public static final int MESSAGE_TYPE_BITS = BitUtils.log2ceil(MessageType.VALUES.length); 
	
	/**
	 * Number of bits necessary to represent an external event pointer.
	 */
	public static final int EVENTID_POINTER_SIZE = 
		+DebuggerGridConfig.EVENT_HOST_BITS
		+DebuggerGridConfig.EVENT_THREAD_BITS
		+DebuggerGridConfig.EVENT_TIMESTAMP_BITS;

	/**
	 * Size of file pages in the database
	 */
	public static final int DB_PAGE_SIZE = 4096;
	
	/**
	 * Number of bits to represent a page pointer in a linked pages list,
	 * as used by {@link TupleWriter} and {@link TupleIterator}
	 */
	public static final int DB_PAGE_POINTER_BITS = 32;
	
	/**
	 * NUmber of bits to represent an offset (in bits) in a page.
	 */
	public static final int DB_PAGE_BITOFFSET_BITS = BitUtils.log2ceil(DB_PAGE_SIZE*8);
	
	/**
	 * NUmber of bits to represent an offset (in bytes) in a page.
	 */
	public static final int DB_PAGE_BYTEOFFSET_BITS = BitUtils.log2ceil(DB_PAGE_SIZE);
	
	/**
	 * Average event size.
	 */
	public static final int DB_AVG_EVENT_SIZE = 55;
	
	public static final int DB_AVG_EVENTS_PER_PAGE = DB_PAGE_SIZE/DB_AVG_EVENT_SIZE;
	
	/**
	 * Number of bits used to represent the record index in an internal
	 * event pointer.
	 */
	public static final int DB_EVENTID_INDEX_BITS = 
		BitUtils.log2ceil(DB_AVG_EVENTS_PER_PAGE) + 1;
	
	/**
	 * Number of bits used to represent the node in an internal event
	 * pointer. 
	 */
	public static final int DB_EVENTID_NODE_BITS = 6;
	
	/**
	 * Number of bits used to represent the page in an internal
	 * event pointer.
	 */
	public static final int DB_EVENTID_PAGE_BITS = 64 - DB_EVENTID_INDEX_BITS - DB_EVENTID_NODE_BITS;

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
	public static final int QUERY_ITERATOR_BUFFER_SIZE = 1;
	
	/**
	 * Number of children of dispatchers.
	 */
	public static final int DISPATCH_BRANCHING_FACTOR = 
		ConfigUtils.readInt("dispatch-branching-factor", 0);
	
	/**
	 * Number of consecutive packets to send to children in the dispatch 
	 * round-robin scheme.
	 */
	public static final int DISPATCH_BATCH_SIZE =
		ConfigUtils.readInt("dispatch-batch-size", 128);
	
	/**
	 * Maximum size allocated to page buffers.
	 * See {@link HardPagedFile.PageDataManager}
	 */
	public static final long DB_PAGE_BUFFER_SIZE = 
		ConfigUtils.readSize("page-buffer-size", "100m");
	
	public static final String MASTER_HOST =
		ConfigUtils.readString("master-host", "localhost");
	
	public static final String NODE_DATA_DIR =
		ConfigUtils.readString("node-data-dir", "/tmp/tod");
	
	public static final String STORE_EVENTS_FILE =
		ConfigUtils.readString("events-file", "events-raw.bin");
	
	/**
	 * Whether the grid master should prevent multiple database nodes
	 * on the same host.
	 */
	public static final boolean CHECK_SAME_HOST = 
		ConfigUtils.readBoolean("check-same-host", true);
	
	public static final boolean LOAD_BALANCING =
		ConfigUtils.readBoolean("load-balancing", false);
	
}
