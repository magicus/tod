/*
 * Created on Jan 24, 2008
 */
package tod.impl.evdbng.db.file;

import tod.impl.evdbng.db.file.PagedFile.PageIOStream;

/**
 * A {@link BTree} of role tuples.
 * @author gpothier
 */
public class RoleTree extends BTree<RoleTuple>
{
	public RoleTree(String aName, PagedFile aFile)
	{
		super(aName, aFile);
	}
	
	public RoleTree(String aName, PagedFile aFile, PageIOStream aStream)
	{
		super(aName, aFile, aStream);
	}

	@Override
	protected TupleBufferFactory<RoleTuple> getTupleBufferFactory()
	{
		return TupleBufferFactory.ROLE;
	}

	/**
	 * Adds a tuple to this tree. The tuple consists in an event id (event index) and a role.
	 */
	public void add(long aEventId, byte aRole)
	{
		logLeafTuple(aEventId, "("+aRole+")");
		
		PageIOStream theStream = addLeafKey(aEventId);
		theStream.writeByte(aRole);
	}

}
