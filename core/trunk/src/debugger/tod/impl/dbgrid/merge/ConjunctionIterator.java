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
	protected void initHeadItems()
	{
		super.initHeadItems();

		itsExhausted = false;
		for (T theItem : getHeadItems())
		{
			if (theItem == null)
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
	protected T readNextItem()
	{
		if (itsExhausted) return null;

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
			for (int i = 0; i < getHeadItems().length; i++)
			{
				T theItem = getHeadItems()[i];

				if (theRefItem == null) theRefItem = theItem;
				else if (! sameEvent(theRefItem, theItem)) theMatch = false;

				if (getTimestamp(theItem) < theMinTimestamp)
				{
					theMinTimestamp = getTimestamp(theItem);
					theMinTimestampHead = i;
				}
			}

			if (theMatch)
			{
				theResult = theRefItem;
			}

			if (!advance(theMinTimestampHead)) itsExhausted = true;

		}
		while ((!theMatch) && (!itsExhausted));

		return theResult;
	}
}
