package tod.impl.dbgrid.merge;

import java.util.Iterator;

/**
 * A disjunction (boolean OR) merge iterator.
 * @author gpothier
 */
public abstract class DisjunctionIterator<T> extends MergeIterator<T>
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

			if (theTuple != null)
			{
				long theTimestamp = getTimestamp(theTuple);
				if (theTimestamp < theMinTimestamp)
				{
					theMinTimestamp = theTimestamp;
					theMinTimestampTuple = theTuple;
				}
			}
		}

		// Remove heads that point to the same event
		for (int i = 0; i < getHeadTuples().length; i++)
		{
			T theTuple = getHeadTuples()[i];

			if (theTuple != null && sameEvent(theMinTimestampTuple, theTuple))
			{
				if (!advance(i)) itsRemainingHeads--;
			}
		}

		return theMinTimestampTuple;
	}
}
