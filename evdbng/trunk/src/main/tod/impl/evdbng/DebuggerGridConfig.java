/*
evdbng - Fast database for TOD
Copyright (C) 2007 Sylapse
Proprietary and confidential
*/
package tod.impl.evdbng;

import sun.misc.VM;
import tod.impl.evdbng.db.ObjectsDatabase;
import tod.impl.evdbng.db.file.PagedFile;
import tod.utils.ConfigUtils;
import zz.utils.bit.BitUtils;

public class DebuggerGridConfig
{
	private static final String HOME = System.getProperty("user.home");
	
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
	 * Maximum number of threads
	 */
	public static final int STRUCTURE_THREADS_COUNT = 10000;

	/**
	 * Maximum number of different depths
	 */
	public static final int STRUCTURE_DEPTH_RANGE = 4096;
	
	/**
	 * Maximum number of bytecode locations
	 */
	public static final int STRUCTURE_BYTECODE_LOCS_COUNT = 65536;

	/**
	 * Maximum number of advice source ids
	 */
	public static final int STRUCTURE_ADVICE_SRC_ID_COUNT = 10000;
	
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
	public static final int INDEX_OBJECT_PARTS = 16;

	/**
	 * Number of partitions of key values for the array index index.
	 * @see SplittedConditionHandler
	 */
	public static final int INDEX_ARRAY_INDEX_PARTS = 14;
	

	/**
	 * Size of file pages in the database
	 */
	public static final int DB_PAGE_SIZE = 4096;
	
	/**
	 * Average event size.
	 */
	public static final int DB_AVG_EVENT_SIZE = 55;
	
	public static final int DB_AVG_EVENTS_PER_PAGE = DB_PAGE_SIZE/DB_AVG_EVENT_SIZE;
	
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
	 * See {@link PagedFile.PageDataManager}
	 */
	public static final long DB_PAGE_BUFFER_SIZE = 
		ConfigUtils.readSize("page-buffer-size", getDefaultPageBufferSize());
	
	private static String getDefaultPageBufferSize()
	{
//		int theSize = (int) (Runtime.getRuntime().maxMemory() / (1024*1024));
		int theSize = (int) (VM.maxDirectMemory() / (1024*1024));
		int theBufferSize = theSize * 10 / 10;
		return theBufferSize + "m";
	}
	
	public static final String MASTER_HOST =
		ConfigUtils.readString("master-host", "localhost");
	
	public static final String NODE_DATA_DIR =
		ConfigUtils.readString("node-data-dir", HOME+"/tmp/tod");
	
	/**
	 * Whether the grid master should prevent multiple database nodes
	 * on the same host.
	 */
	public static final boolean CHECK_SAME_HOST = 
		ConfigUtils.readBoolean("check-same-host", true);
	
	public static final boolean LOAD_BALANCING =
		ConfigUtils.readBoolean("load-balancing", false);
	
}
