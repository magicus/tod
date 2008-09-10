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
 * A {@link BTree} of role tuples.
 * @author gpothier
 */
public class RoleTree extends BTree<RoleTuple>
{
	private AddTask itsCurrentTask = new AddTask();

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
		if (DebugFlags.DB_LOG_DIR != null) logLeafTuple(aEventId, "("+aRole+")");
		
		PageIOStream theStream = addLeafKey(aEventId);
		theStream.writeByte(aRole);
	}
	
	@Override
	public void writeTo(PageIOStream aStream)
	{
		// Flush buffered tuples before writing out this tree
		DBExecutor.submitAndWait(itsCurrentTask);
		
		super.writeTo(aStream);
	}
	
	/**
	 * Same as {@link #add(long, byte)} but uses the {@link DBExecutor}.
	 */
	public void addAsync(long aEventId, byte aRole)
	{
		itsCurrentTask.addTuple(aEventId, aRole);
		if (itsCurrentTask.isFull()) 
		{
			DBExecutor.submit(itsCurrentTask);
			itsCurrentTask = new AddTask();
		}
	}

	private class AddTask extends DBTask
	{
		private final long[] itsEventIds = new long[DebuggerGridConfigNG.DB_TASK_SIZE];
		private final byte[] itsRoles = new byte[DebuggerGridConfigNG.DB_TASK_SIZE];
		private int itsPosition = 0;
		
		public void addTuple(long aEventId, byte aRole)
		{
			itsEventIds[itsPosition] = aEventId;
			itsRoles[itsPosition] = aRole;
			itsPosition++;
		}
		
		public boolean isFull()
		{
			return itsPosition == itsEventIds.length;
		}
		
		@Override
		public void run()
		{
			for (int i=0;i<itsPosition;i++) add(itsEventIds[i], itsRoles[i]);
		}

		@Override
		public int getGroup()
		{
			return RoleTree.this.hashCode();
		}
	}
}
