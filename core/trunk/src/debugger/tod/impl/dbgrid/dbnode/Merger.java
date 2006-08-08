/*
 * Created on Jul 24, 2006
 */
package tod.impl.dbgrid.dbnode;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * This class permit to perform n-ary merge joins.
 * @author gpothier
 */
public class Merger
{
	/**
	 * Returns an iterator that retrieves all the events that are common to all
	 * the specified indexes starting from a specified timestamp. 
	 */
	public static <T extends StdIndexSet.Tuple> Iterator<T> conjunction(Iterator<T>[] aIterators)
	{
		return new ConjunctionIterator<T>(aIterators);
	}
	
	/**
	 * Returns an iterator that retrieves all the events of all the specified indexes. 
	 */
	public static <T extends StdIndexSet.Tuple> Iterator<T> disjunction(Iterator<T>[] aIterators)
	{
		return new DisjunctionIterator<T>(aIterators);
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
	
	private static abstract class MergerIterator<T extends StdIndexSet.Tuple> 
	implements Iterator<T>
	{
		private final Iterator<T>[] itsIterators;
		private final T[] itsHeadTuples;
		private T itsNextTuple;
		
		public MergerIterator(Iterator<T>[] aIterators)
		{
			itsIterators = aIterators;
			itsHeadTuples = (T[]) new StdIndexSet.Tuple[itsIterators.length];

			initHeadTuples();
			
			itsNextTuple = readNextTuple();
		}
		
		protected void initHeadTuples()
		{
			for (int i=0;i<itsIterators.length;i++) advance(i);
		}
		
		/**
		 * Advances the specified head until a tuple is found that is accepted by the tuple
		 * filter (a null tuple filter accepts all tuples).
		 * If the end of the stream is reached, the head is set to null and the method returns false.
		 * @return True if it was possible to advance, false otherwise.
		 */
		protected boolean advance(int aHeadIndex)
		{
			if (itsIterators[aHeadIndex].hasNext()) 
			{
				T theTuple = itsIterators[aHeadIndex].next();
				
				itsHeadTuples[aHeadIndex] = theTuple;
				return true;
			}
			else
			{
				itsHeadTuples[aHeadIndex] = null;
				return false;
			}
		}

		/**
		 * Retrieves the next matching event pointer
		 */
		protected abstract T readNextTuple();
		
		protected T[] getHeadTuples()
		{
			return itsHeadTuples;
		}
		
		public T next()
		{
			if (! hasNext()) throw new NoSuchElementException();
			T theResult = itsNextTuple;
			T theNextTuple = readNextTuple();
			itsNextTuple = theNextTuple;
			return theResult;
		}

		public void remove()
		{
			throw new UnsupportedOperationException();
		}

	}	
	
	private static class ConjunctionIterator<T extends StdIndexSet.Tuple> extends MergerIterator<T>
	{
		/**
		 * The iterator is exhausted when any one of its source iterators is
		 * exhausted.
		 */
		private boolean itsExhausted;
		
		public ConjunctionIterator(Iterator<T>[] aIterators)
		{
			super(aIterators);
		}
		
		@Override
		protected void initHeadTuples()
		{
			super.initHeadTuples();

			itsExhausted = false;
			for (T theTuple : getHeadTuples())
			{
				if (theTuple == null) 
				{
					itsExhausted = true;
					break;
				}
			}
		}
		
		public boolean hasNext()
		{
			return ! itsExhausted;
		}
		
		@Override
		protected T readNextTuple()
		{
			if (itsExhausted) return null;
			
			T theResult = null;
			boolean theMatch;
			do
			{
				theMatch = true;
				
				T theRefTuple = null;
				int theMinTimestampHead = -1;
				long theMinTimestamp = Long.MAX_VALUE;
				
				// Check if current head set is a match (ie. all head tuples point to the same event).
				// At the same time find the head that has the minimum timestamp
				for (int i = 0; i < getHeadTuples().length; i++)
				{
					T theTuple = getHeadTuples()[i];
					
					if (theRefTuple == null) theRefTuple = theTuple;
					else if (theRefTuple.getEventPointer() != theTuple.getEventPointer()) theMatch = false;
					
					if (theTuple.getTimestamp() < theMinTimestamp)
					{
						theMinTimestamp = theTuple.getTimestamp();
						theMinTimestampHead = i;
					}
				}

				if (theMatch)
				{
					theResult = theRefTuple;
				}

				if (! advance(theMinTimestampHead)) itsExhausted = true;
				
			} while((! theMatch) && (! itsExhausted));
			
			return theResult;
		}
	}
	
	private static class DisjunctionIterator<T extends StdIndexSet.Tuple> extends MergerIterator<T>
	{
		/**
		 * The number of iterators that still have elements.
		 */
		private int itsRemainingHeads;
		
		public DisjunctionIterator(Iterator<T>[] aIterators)
		{
			super(aIterators);
		}
		
		@Override
		protected void initHeadTuples()
		{
			super.initHeadTuples();
			itsRemainingHeads = 0;
			for (T theTuple : getHeadTuples())
			{
				if (theTuple != null) itsRemainingHeads++; 
			}
		}
		
		public boolean hasNext()
		{
			return itsRemainingHeads > 0;
		}
		
		@Override
		protected T readNextTuple()
		{
			if (itsRemainingHeads == 0) return null;
			
			T theMinTimestampTuple = null;
			long theMinTimestamp = Long.MAX_VALUE;

			// Find the head with the minimum timestamp
			for (int i = 0; i < getHeadTuples().length; i++)
			{
				T theTuple = getHeadTuples()[i];
				
				if (theTuple != null && theTuple.getTimestamp() < theMinTimestamp)
				{
					theMinTimestamp = theTuple.getTimestamp();
					theMinTimestampTuple = theTuple;
				}
			}
			
			// Remove heads that point to the same event
			for (int i = 0; i < getHeadTuples().length; i++)
			{
				T theTuple = getHeadTuples()[i];
				
				if (theTuple != null 
						&& theTuple.getEventPointer() == theMinTimestampTuple.getEventPointer())
				{
					if (! advance(i)) itsRemainingHeads--;
				}
			}
			
			return theMinTimestampTuple;
		}
	}
}
