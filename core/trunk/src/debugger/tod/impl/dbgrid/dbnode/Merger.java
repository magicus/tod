/*
 * Created on Jul 24, 2006
 */
package tod.impl.dbgrid.dbnode;

import java.util.Iterator;
import java.util.NoSuchElementException;

import tod.impl.dbgrid.messages.GridEvent;

/**
 * This class permit to perform n-ary merge joins.
 * @author gpothier
 */
public class Merger
{
	/**
	 * Returns an iterator that retrieves all the events that are common to all
	 * the specified indexes. 
	 */
	public static <T extends StdIndexSet.Tuple> Iterator<GridEvent> conjunction(
			EventList aEventList,
			HierarchicalIndex<T>[] aIndexes, 
			long aTimestamp)
	{
		return new ConjunctionIterator<T>(aEventList, getIterators(aIndexes, aTimestamp));
	}
	
	/**
	 * Returns an iterator that retrieves all the events of all the specified indexes. 
	 */
	public static <T extends StdIndexSet.Tuple> Iterator<GridEvent> disjunction(
			EventList aEventList,
			HierarchicalIndex<T>[] aIndexes, 
			long aTimestamp)
	{
		return new DisjunctionIterator<T>(aEventList, getIterators(aIndexes, aTimestamp));
	}
	
	private static <T extends StdIndexSet.Tuple> Iterator<T>[] getIterators(
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
	
	private static abstract class MergerIterator<T extends StdIndexSet.Tuple> implements Iterator<GridEvent>
	{
		private final EventList itsEventList;
		private final Iterator<T>[] itsIterators;
		private final StdIndexSet.Tuple[] itsHeadTuples;
		private GridEvent itsNextEvent;
		private boolean itsExhausted = false;
		
		public MergerIterator(EventList aEventList, Iterator<T>[] aIterators)
		{
			itsEventList = aEventList;
			itsIterators = aIterators;
			itsHeadTuples = new StdIndexSet.Tuple[itsIterators.length];
		
			// Init head tuples
			for (int i=0;i<itsIterators.length && ! itsExhausted;i++) advance(i);
			
			itsNextEvent = itsExhausted ? null : itsEventList.getEvent(readNextEvent());
		}
		
		protected void advance(int aHeadIndex)
		{
			assert ! itsExhausted;
			if (itsIterators[aHeadIndex].hasNext()) 
			{
				itsHeadTuples[aHeadIndex] = itsIterators[aHeadIndex].next();
			}
			else 
			{
				itsExhausted = true;
			}
		}
		
		protected boolean isExhausted()
		{
			return itsExhausted;
		}

		/**
		 * Retrieves the next matching event pointer
		 */
		protected abstract long readNextEvent();
		
		protected StdIndexSet.Tuple[] getHeadTuples()
		{
			return itsHeadTuples;
		}
		
		public boolean hasNext()
		{
			return ! itsExhausted;
		}

		public GridEvent next()
		{
			if (! hasNext()) throw new NoSuchElementException();
			GridEvent theResult = itsNextEvent;
			long theNextEvent = readNextEvent();
			itsNextEvent = isExhausted() ? null : itsEventList.getEvent(theNextEvent);
			return theResult;
		}

		public void remove()
		{
			throw new UnsupportedOperationException();
		}

	}	
	
	private static class ConjunctionIterator<T extends StdIndexSet.Tuple> extends MergerIterator<T>
	{
		
		public ConjunctionIterator(EventList aEventList, Iterator<T>[] aIterators)
		{
			super(aEventList, aIterators);
		}
		
		@Override
		protected long readNextEvent()
		{
			long theResult = -1;
			boolean theMatch;
			do
			{
				theMatch = true;
				
				long theRefPointer = -1;
				int theMinTimestampHead = -1;
				long theMinTimestamp = Long.MAX_VALUE;
				
				for (int i = 0; i < getHeadTuples().length; i++)
				{
					StdIndexSet.Tuple theTuple = getHeadTuples()[i];
					
					if (theRefPointer == -1) theRefPointer = theTuple.getEventPointer();
					else if (theRefPointer != theTuple.getEventPointer()) theMatch = false;
					
					if (theTuple.getTimestamp() < theMinTimestamp)
					{
						theMinTimestamp = theTuple.getTimestamp();
						theMinTimestampHead = i;
					}
				}

				if (theMatch)
				{
					theResult = theRefPointer;
				}

				advance(theMinTimestampHead);
			} while(! theMatch && ! isExhausted());
			
			return theResult;
		}
	}
	
	private static class DisjunctionIterator<T extends StdIndexSet.Tuple> extends MergerIterator<T>
	{
		
		public DisjunctionIterator(EventList aEventList, Iterator<T>[] aIterators)
		{
			super(aEventList, aIterators);
		}
		
		@Override
		protected long readNextEvent()
		{
			int theMinTimestampHead = -1;
			long theMinTimestamp = Long.MAX_VALUE;
			
			for (int i = 0; i < getHeadTuples().length; i++)
			{
				StdIndexSet.Tuple theTuple = getHeadTuples()[i];
				
				if (theTuple.getTimestamp() < theMinTimestamp)
				{
					theMinTimestamp = theTuple.getTimestamp();
					theMinTimestampHead = i;
				}
			}
			
			long thePointer = getHeadTuples()[theMinTimestampHead].getEventPointer();
			advance(theMinTimestampHead);
			
			return thePointer;
		}
	}
}
