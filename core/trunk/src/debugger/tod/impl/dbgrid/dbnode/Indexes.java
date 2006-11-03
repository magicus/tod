/*
 * Created on Jul 24, 2006
 */
package tod.impl.dbgrid.dbnode;

import static tod.impl.dbgrid.DebuggerGridConfig.*;
import tod.impl.dbgrid.dbnode.file.HardPagedFile;
import tod.impl.dbgrid.monitoring.AggregationType;
import tod.impl.dbgrid.monitoring.Monitor;
import tod.impl.dbgrid.monitoring.Probe;
import zz.utils.bit.BitUtils;
import zz.utils.cache.MRUBuffer;

/**
 * Groups all the indexes maintained by a database node.
 * @author gpothier
 */
public class Indexes
{
	public final StdIndexSet typeIndex;
	public final StdIndexSet hostIndex;
	public final StdIndexSet threadIndex;
	public final StdIndexSet depthIndex;
	public final StdIndexSet bytecodeLocationIndex;
	public final RoleIndexSet behaviorIndex;
	public final StdIndexSet fieldIndex;
	public final StdIndexSet variableIndex;
	public final StdIndexSet indexIndex;
	public final ObjectIndexSet objectIndex;
	
	public Indexes(HardPagedFile aFile)
	{
		typeIndex = new StdIndexSet("type", aFile, STRUCTURE_TYPE_COUNT+1);
		hostIndex = new StdIndexSet("host", aFile, STRUCTURE_HOSTS_COUNT+1);
		threadIndex = new StdIndexSet("thread", aFile, STRUCTURE_THREADS_COUNT+1);
		depthIndex = new StdIndexSet("depth", aFile, STRUCTURE_DEPTH_RANGE+1);
		bytecodeLocationIndex = new StdIndexSet("bytecodeLoc.", aFile, STRUCTURE_BYTECODE_LOCS_COUNT+1);
		behaviorIndex = new RoleIndexSet("behavior", aFile, STRUCTURE_BEHAVIOR_COUNT+1);
		fieldIndex = new StdIndexSet("field", aFile, STRUCTURE_FIELD_COUNT+1);
		variableIndex = new StdIndexSet("variable", aFile, STRUCTURE_VAR_COUNT+1);
		indexIndex = new StdIndexSet("index", aFile, STRUCTURE_ARRAY_INDEX_COUNT+1);
		objectIndex = new ObjectIndexSet("object", aFile, STRUCTURE_OBJECT_COUNT+1);
	}
	
	public void unregister()
	{
		typeIndex.unregister();
		hostIndex.unregister();
		threadIndex.unregister();
		depthIndex.unregister();
		bytecodeLocationIndex.unregister();
		behaviorIndex.unregister();
		fieldIndex.unregister();
		variableIndex.unregister();
		indexIndex.unregister();
		objectIndex.unregister();
	}
}
