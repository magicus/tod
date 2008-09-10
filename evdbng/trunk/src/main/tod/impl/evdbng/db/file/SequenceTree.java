/*
evdbng - Fast database for TOD
Copyright (C) 2007 Sylapse
Proprietary and confidential
*/
package tod.impl.evdbng.db.file;

import tod.core.DebugFlags;
import tod.impl.evdbng.DebuggerGridConfigNG;
import tod.impl.evdbng.db.DBExecutor;
import tod.impl.evdbng.db.DBExecutor.DBTask;
import tod.impl.evdbng.db.file.PagedFile.PageIOStream;
import tod.impl.evdbng.db.file.TupleFinder.NoMatch;

/**
 * A BTree that conceptually stores tuples of the form <key, s> where
 * the s in consecutive tuples are consecutive. Thanks to this constraint,
 * the s doesn't actually need to be stored, as it is equal to the position
 * of the tuple in the BTree.
 * We assume that the sequence starts at 0.
 * @author gpothier
 */
public class SequenceTree extends BTree<SimpleTuple>
{
	private AddTask itsCurrentTask = new AddTask();

	public SequenceTree(String aName, PagedFile aFile)
	{
		super(aName, aFile);
	}

	@Override
	protected TupleBufferFactory<SimpleTuple> getTupleBufferFactory()
	{
		return TupleBufferFactory.SIMPLE;
	}

	/**
	 * Returns the position of the tuple associated with the given key.
	 * @param aNoMatch Indicates the behavior when no exact match is found:
	 * <li> If aNoMatch is null, the method returns -1
	 * <li> Otherwise, the position of the previous or next tuple is returned 
	 */
	public long getTuplePosition(long aKey, NoMatch aNoMatch)
	{
		TupleIterator<SimpleTuple> theIterator = getTupleIterator(
				aKey, 
				aNoMatch != null ? aNoMatch : NoMatch.AFTER);
		
		SimpleTuple theTuple = theIterator.peekNext();
		if (aNoMatch == null && theTuple.getKey() != aKey) return -1;
		else return theIterator.getNextTupleIndex();
	}
	
	/**
	 * Adds a tuple to this btree.
	 */
	public void add(long aKey)
	{
		if (DebugFlags.DB_LOG_DIR != null) logLeafTuple(aKey, null);
		addLeafKey(aKey);
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
			return SequenceTree.this.hashCode();
		}
	}

}
