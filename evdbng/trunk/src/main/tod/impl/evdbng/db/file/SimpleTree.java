/*
 * Created on Jan 24, 2008
 */
package tod.impl.evdbng.db.file;

import tod.impl.evdbng.db.file.PagedFile.PageIOStream;

/**
 * A {@link BTree} of simple tuples (no extra data).
 * @author gpothier
 */
public class SimpleTree extends BTree<SimpleTuple>
{
	public SimpleTree(String aName, PagedFile aFile)
	{
		super(aName, aFile);
	}

	public SimpleTree(String aName, PagedFile aFile, PageIOStream aStream)
	{
		super(aName, aFile, aStream);
	}

	@Override
	protected TupleBufferFactory<SimpleTuple> getTupleBufferFactory()
	{
		return TupleBufferFactory.SIMPLE;
	}

	/**
	 * Adds a tuple to this tree. The tuple consists only in a event id (event index).
	 */
	public void add(long aEventId)
	{
		logLeafTuple(aEventId, null);
		addLeafKey(aEventId);
	}

}
