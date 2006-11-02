package tod.impl.dbgrid.merge;

import java.util.Iterator;

import tod.impl.dbgrid.BidiIterator;

/**
 * Conjunction (boolean AND) merge iterator.
 * @author gpothier
 */
public abstract class ConjunctionIterator<T> extends MergeIterator<T>
{
	public ConjunctionIterator(BidiIterator<T>[] aIterators)
	{
		super(aIterators);
	}

	@Override
	protected T fetchNext()
	{
		T theResult = null;
		boolean theMatch;
		do
		{
			theMatch = true;

			T theRefItem = null;
			int theMinTimestampHead = -1;
			long theMinTimestamp = Long.MAX_VALUE;

			// Check if current head set is a match (ie. all head tuples point
			// to the same event).
			// At the same time find the head that has the minimum timestamp
			for (int i = 0; i < getHeadCount(); i++)
			{
				T theItem = getNextHead(i);
				if (theItem == null) return null;

				if (theRefItem == null) theRefItem = theItem;
				else if (! sameEvent(theRefItem, theItem)) theMatch = false;

				long theTimestamp = getTimestamp(theItem);
				if (theTimestamp < theMinTimestamp)
				{
					theMinTimestamp = theTimestamp;
					theMinTimestampHead = i;
				}
			}

			if (theMatch)
			{
				theResult = theRefItem;
			}

			moveNext(theMinTimestampHead);
		}
		while (!theMatch);

		return theResult;
	}

	@Override
	protected T fetchPrevious()
	{
		T theResult = null;
		boolean theMatch;
		do
		{
			theMatch = true;

			T theRefItem = null;
			int theMaxTimestampHead = -1;
			long theMaxTimestamp = 0;

			// Check if current head set is a match (ie. all head tuples point
			// to the same event).
			// At the same time find the head that has the maximum timestamp
			for (int i = 0; i < getHeadCount(); i++)
			{
				T theItem = getPreviousHead(i);
				if (theItem == null) return null;

				if (theRefItem == null) theRefItem = theItem;
				else if (! sameEvent(theRefItem, theItem)) theMatch = false;

				long theTimestamp = getTimestamp(theItem);
				if (theTimestamp > theMaxTimestamp)
				{
					theMaxTimestamp = theTimestamp;
					theMaxTimestampHead = i;
				}
			}

			if (theMatch)
			{
				theResult = theRefItem;
			}

			movePrevious(theMaxTimestampHead);
		}
		while (!theMatch);

		return theResult;
	}
}
