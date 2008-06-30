/*
evdbng - Fast database for TOD
Copyright (C) 2007 Sylapse
Proprietary and confidential
*/
package tod.impl.evdbng.db;

import tod.impl.database.IBidiIterator;
import tod.impl.dbgrid.merge.ConjunctionIterator;
import tod.impl.dbgrid.merge.DisjunctionIterator;
import tod.impl.evdbng.db.file.BTree;
import tod.impl.evdbng.db.file.Tuple;

/**
 * This class permit to perform n-ary merge joins between indexes.
 * @author gpothier
 */
public class IndexMerger
{
	/**
	 * Returns an iterator that retrieves all the events that are common to all
	 * the specified indexes starting from a specified timestamp. 
	 */
	public static <T extends Tuple> IBidiIterator<T> conjunction(
			boolean aMatchRoles, 
			IBidiIterator<T>[] aIterators)
	{
		return new MyConjunctionIterator<T>(aMatchRoles, aIterators);
	}
	
	/**
	 * Returns an iterator that retrieves all the events of all the specified indexes. 
	 */
	public static <T extends Tuple> IBidiIterator<T> disjunction(
			IBidiIterator<T>[] aIterators)
	{
		return new MyDisjunctionIterator<T>(aIterators);
	}
	
	/**
	 * Returns the tuple iterators of all the specified indexes, starting
	 * at the specified timestamp. 
	 */
	public static <T extends Tuple> IBidiIterator<T>[] getIterators(
			BTree<T>[] aIndexes,
			long aTimestamp)
	{
		IBidiIterator<T>[] theIterators = new IBidiIterator[aIndexes.length];
		for (int i = 0; i < aIndexes.length; i++)
		{
			BTree<T> theIndex = aIndexes[i];
			theIterators[i] = theIndex.getTupleIterator(aTimestamp);
		}
		return theIterators;
	}
	
	
	public static class MyConjunctionIterator<T extends Tuple> 
	extends ConjunctionIterator<T>
	{
		public MyConjunctionIterator(boolean aMatchRoles, IBidiIterator<T>[] aIterators)
		{
			super(aMatchRoles, aIterators);
		}

		@Override
		protected long getKey(T aItem)
		{
			return aItem.getKey();
		}

		@Override
		protected boolean sameEvent(T aItem1, T aItem2)
		{
			return aItem1.getKey() == aItem2.getKey();
		}
	}
	
	public static class MyDisjunctionIterator<T extends Tuple> 
	extends DisjunctionIterator<T>
	{
		public MyDisjunctionIterator(IBidiIterator<T>[] aIterators)
		{
			super(aIterators);
		}
		
		@Override
		protected long getKey(T aItem)
		{
			return aItem.getKey();
		}

		@Override
		protected boolean sameEvent(T aItem1, T aItem2)
		{
			return aItem1.getKey() == aItem2.getKey();
		}
	}
}
