/*
 * Created on Jan 24, 2008
 */
package tod.impl.evdbng.db.file;

import tod.core.DebugFlags;
import tod.impl.evdbng.DebuggerGridConfigNG;
import tod.impl.evdbng.db.DBExecutor;
import tod.impl.evdbng.db.DBExecutor.DBTask;
import tod.impl.evdbng.db.file.PagedFile.PageIOStream;

/**
 * A {@link BTree} of simple tuples (no extra data).
 * @author gpothier
 */
public class SimpleTree extends BTree<SimpleTuple>
{
	private AddTask itsCurrentTask = new AddTask();

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
		if (DebugFlags.DB_LOG_DIR != null) logLeafTuple(aEventId, null);
		addLeafKey(aEventId);
	}
	
	@Override
	public void writeTo(PageIOStream aStream)
	{
		// Flush buffered tuples before writing out this tree
		DBExecutor.submitAndWait(itsCurrentTask);
		
		super.writeTo(aStream);
	}
	
	/**
	 * Same as {@link #add(long)} but uses the {@link DBExecutor}.
	 */
	public void addAsync(long aEventId)
	{
		itsCurrentTask.addTuple(aEventId);
		if (itsCurrentTask.isFull()) 
		{
			DBExecutor.submit(itsCurrentTask);
			itsCurrentTask = new AddTask();
		}
	}

	private class AddTask extends DBTask
	{
		private final long[] itsEventIds = new long[DebuggerGridConfigNG.DB_TASK_SIZE];
		private int itsPosition = 0;
		
		public void addTuple(long aEventId)
		{
			itsEventIds[itsPosition] = aEventId;
			itsPosition++;
		}
		
		public boolean isFull()
		{
			return itsPosition == itsEventIds.length;
		}
		
		@Override
		public void run()
		{
			for (int i=0;i<itsPosition;i++) add(itsEventIds[i]);
		}

		@Override
		public int getGroup()
		{
			return SimpleTree.this.hashCode();
		}
	}
}
