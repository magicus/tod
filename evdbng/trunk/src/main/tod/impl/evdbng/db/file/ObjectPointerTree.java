/*
 * Created on Jan 25, 2008
 */
package tod.impl.evdbng.db.file;

import tod.core.DebugFlags;
import tod.impl.dbgrid.db.ObjectsDatabase;
import tod.impl.evdbng.db.file.PagedFile.PageIOStream;

/**
 * A {@link BTree} of object pointers for the {@link ObjectsDatabase}.
 * @author gpothier
 */
public class ObjectPointerTree extends BTree<ObjectPointerTuple>
{

	public ObjectPointerTree(String aName, PagedFile aFile, PageIOStream aStream)
	{
		super(aName, aFile, aStream);
	}

	public ObjectPointerTree(String aName, PagedFile aFile)
	{
		super(aName, aFile);
	}

	@Override
	protected TupleBufferFactory<ObjectPointerTuple> getTupleBufferFactory()
	{
		return TupleBufferFactory.OBJECT_POINTER;
	}
	
	/**
	 * Adds a tuple to this tree. The tuple consists in an event id (event index) and a role.
	 */
	public void add(long aObjectId, int aPageId, int aOffset)
	{
		logLeafTuple(aObjectId, "pid: "+aPageId+", off: "+aOffset);

		PageIOStream theStream = addLeafKey(aObjectId);
		theStream.writePagePointer(aPageId);
		theStream.writePageOffset(aOffset);
	}


	
}