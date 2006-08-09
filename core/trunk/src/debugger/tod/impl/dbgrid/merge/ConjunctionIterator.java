package tod.impl.dbgrid.merge;

import java.util.Iterator;

/**
 * Conjunction (boolean AND) merge iterator.
 * @author gpothier
 */
public abstract class ConjunctionIterator<T> extends MergeIterator<T>
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
		return !itsExhausted;
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

			// Check if current head set is a match (ie. all head tuples point
			// to the same event).
			// At the same time find the head that has the minimum timestamp
			for (int i = 0; i < getHeadTuples().length; i++)
			{
				T theTuple = getHeadTuples()[i];

				if (theRefTuple == null) theRefTuple = theTuple;
				else if (! sameEvent(theRefTuple, theTuple)) theMatch = false;

				if (getTimestamp(theTuple) < theMinTimestamp)
				{
					theMinTimestamp = getTimestamp(theTuple);
					theMinTimestampHead = i;
				}
			}

			if (theMatch)
			{
				theResult = theRefTuple;
			}

			if (!advance(theMinTimestampHead)) itsExhausted = true;

		}
		while ((!theMatch) && (!itsExhausted));

		return theResult;
	}
}
