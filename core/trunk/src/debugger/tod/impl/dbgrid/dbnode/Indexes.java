/*
 * Created on Jul 24, 2006
 */
package tod.impl.dbgrid.dbnode;

/**
 * Groups all the indexes maintained by a database node.
 * @author gpothier
 */
public class Indexes
{
	public final StdIndexSet<Byte> typeIndex;
	public final StdIndexSet<Integer> hostIndex;
	public final StdIndexSet<Integer> threadIndex;
	public final StdIndexSet<Integer> bytecodeLocationIndex;
	public final RoleIndexSet<Integer> behaviorIndex;
	public final StdIndexSet<Integer> fieldIndex;
	public final StdIndexSet<Integer> variableIndex;
	public final RoleIndexSet<Object> objectIndex;
	
	public Indexes(PagedFile aFile)
	{
		typeIndex = new StdIndexSet<Byte>(aFile);
		hostIndex = new StdIndexSet<Integer>(aFile);
		threadIndex = new StdIndexSet<Integer>(aFile);
		bytecodeLocationIndex = new StdIndexSet<Integer>(aFile);
		behaviorIndex = new RoleIndexSet<Integer>(aFile);
		fieldIndex = new StdIndexSet<Integer>(aFile);
		variableIndex = new StdIndexSet<Integer>(aFile);
		objectIndex = new RoleIndexSet<Object>(aFile);
	}
}
