/*
 * Created on Jul 24, 2006
 */
package tod.impl.dbgrid.dbnode;

import static tod.impl.dbgrid.DebuggerGridConfig.*;

/**
 * Groups all the indexes maintained by a database node.
 * @author gpothier
 */
public class Indexes
{
	public final StdIndexSet typeIndex;
	public final StdIndexSet hostIndex;
	public final StdIndexSet threadIndex;
	public final StdIndexSet bytecodeLocationIndex;
	public final RoleIndexSet behaviorIndex;
	public final StdIndexSet fieldIndex;
	public final StdIndexSet variableIndex;
	public final RoleIndexSet objectIndex;
	
	public Indexes(PagedFile aFile)
	{
		typeIndex = new StdIndexSet("type", aFile, STRUCTURE_TYPE_COUNT);
		hostIndex = new StdIndexSet("host", aFile, STRUCTURE_HOSTS_COUNT);
		threadIndex = new StdIndexSet("thread", aFile, STRUCTURE_THREADS_COUNT);
		bytecodeLocationIndex = new StdIndexSet("bytecodeLoc.", aFile, STRUCTURE_BYTECODE_LOCS_COUNT);
		behaviorIndex = new RoleIndexSet("behavior", aFile, STRUCTURE_BEHAVIOR_COUNT);
		fieldIndex = new StdIndexSet("field", aFile, STRUCTURE_FIELD_COUNT);
		variableIndex = new StdIndexSet("variable", aFile, STRUCTURE_VAR_COUNT);
		objectIndex = new RoleIndexSet("object", aFile, STRUCTURE_OBJECT_COUNT);
	}
}
