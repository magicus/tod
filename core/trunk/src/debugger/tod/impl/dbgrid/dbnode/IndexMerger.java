/*
 * Created on Jul 24, 2006
 */
package tod.impl.dbgrid.dbnode;

import tod.impl.dbgrid.BidiIterator;
import tod.impl.dbgrid.merge.ConjunctionIterator;
import tod.impl.dbgrid.merge.DisjunctionIterator;

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
	public static <T extends StdIndexSet.StdTuple> BidiIterator<T> conjunction(
			BidiIterator<T>[] aIterators)
	{
		return new MyConjunctionIterator<T>(aIterators);
	}
	
	/**
	 * Returns an iterator that retrieves all the events of all the specified indexes. 
	 */
	public static <T extends StdIndexSet.StdTuple> BidiIterator<T> disjunction(
			BidiIterator<T>[] aIterators)
	{
		return new MyDisjunctionIterator<T>(aIterators);
	}
	
	/**
	 * Returns the tuple iterators of all the specified indexes, starting
	 * at the specified timestamp. 
	 */
	public static <T extends StdIndexSet.StdTuple> BidiIterator<T>[] getIterators(
			HierarchicalIndex<T>[] aIndexes,
			long aTimestamp)
	{
		BidiIterator<T>[] theIterators = new BidiIterator[aIndexes.length];
		for (int i = 0; i < aIndexes.length; i++)
		{
			HierarchicalIndex<T> theIndex = aIndexes[i];
			theIterators[i] = theIndex.getTupleIterator(aTimestamp);
		}
		return theIterators;
	}
	
	
	public static class MyConjunctionIterator<T extends StdIndexSet.StdTuple> 
	extends ConjunctionIterator<T>
	{
		public MyConjunctionIterator(BidiIterator<T>[] aIterators)
		{
			super(aIterators);
		}

		@Override
		protected long getTimestamp(T aItem)
		{
			return aItem.getTimestamp();
		}

		@Override
		protected boolean sameEvent(T aItem1, T aItem2)
		{
			return aItem1.getEventPointer() == aItem2.getEventPointer();
		}
	}
	
	public static class MyDisjunctionIterator<T extends StdIndexSet.StdTuple> 
	extends DisjunctionIterator<T>
	{
		public MyDisjunctionIterator(BidiIterator<T>[] aIterators)
		{
			super(aIterators);
		}
		
		@Override
		protected long getTimestamp(T aItem)
		{
			return aItem.getTimestamp();
		}

		@Override
		protected boolean sameEvent(T aItem1, T aItem2)
		{
			return aItem1.getEventPointer() == aItem2.getEventPointer();
		}
	}
}
