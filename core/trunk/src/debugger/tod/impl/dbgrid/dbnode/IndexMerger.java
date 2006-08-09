/*
 * Created on Jul 24, 2006
 */
package tod.impl.dbgrid.dbnode;

import java.util.Iterator;

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
	public static <T extends StdIndexSet.Tuple> Iterator<T> conjunction(Iterator<T>[] aIterators)
	{
		return new MyConjunctionIterator<T>(aIterators);
	}
	
	/**
	 * Returns an iterator that retrieves all the events of all the specified indexes. 
	 */
	public static <T extends StdIndexSet.Tuple> Iterator<T> disjunction(Iterator<T>[] aIterators)
	{
		return new MyDisjunctionIterator<T>(aIterators);
	}
	
	/**
	 * Returns the tuple iterators of all the specified indexes, starting
	 * at the specified timestamp. 
	 */
	public static <T extends StdIndexSet.Tuple> Iterator<T>[] getIterators(
			HierarchicalIndex<T>[] aIndexes,
			long aTimestamp)
	{
		Iterator<T>[] theIterators = new Iterator[aIndexes.length];
		for (int i = 0; i < aIndexes.length; i++)
		{
			HierarchicalIndex<T> theIndex = aIndexes[i];
			theIterators[i] = theIndex.getTupleIterator(aTimestamp);
		}
		return theIterators;
	}
	
	
	public static class MyConjunctionIterator<T extends StdIndexSet.Tuple> 
	extends ConjunctionIterator<T>
	{
		public MyConjunctionIterator(Iterator<T>[] aIterators)
		{
			super(aIterators);
		}

		@Override
		protected long getTimestamp(T aTuple)
		{
			return aTuple.getTimestamp();
		}

		@Override
		protected boolean sameEvent(T aTuple1, T aTuple2)
		{
			return aTuple1.getEventPointer() == aTuple2.getEventPointer();
		}
	}
	
	public static class MyDisjunctionIterator<T extends StdIndexSet.Tuple> 
	extends DisjunctionIterator<T>
	{
		public MyDisjunctionIterator(Iterator<T>[] aIterators)
		{
			super(aIterators);
		}
		
		@Override
		protected long getTimestamp(T aTuple)
		{
			return aTuple.getTimestamp();
		}

		@Override
		protected boolean sameEvent(T aTuple1, T aTuple2)
		{
			return aTuple1.getEventPointer() == aTuple2.getEventPointer();
		}
	}
}
