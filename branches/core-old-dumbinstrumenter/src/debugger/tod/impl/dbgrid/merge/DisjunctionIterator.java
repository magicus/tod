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
	protected void initHeadItems()
	{
		super.initHeadItems();
		itsRemainingHeads = 0;
		for (T theItem : getHeadItems())
		{
			if (theItem != null) itsRemainingHeads++;
		}
	}

	public boolean hasNext()
	{
		return itsRemainingHeads > 0;
	}

	@Override
	protected T readNextItem()
	{
		if (itsRemainingHeads == 0) return null;

		T theMinTimestampItem = null;
		long theMinTimestamp = Long.MAX_VALUE;

		// Find the head with the minimum timestamp
		for (int i = 0; i < getHeadItems().length; i++)
		{
			T theItem = getHeadItems()[i];

			if (theItem != null)
			{
				long theTimestamp = getTimestamp(theItem);
				if (theTimestamp < theMinTimestamp)
				{
					theMinTimestamp = theTimestamp;
					theMinTimestampItem = theItem;
				}
			}
		}

		// Remove heads that point to the same event
		for (int i = 0; i < getHeadItems().length; i++)
		{
			T theItem = getHeadItems()[i];

			if (theItem != null && sameEvent(theMinTimestampItem, theItem))
			{
				if (!advance(i)) itsRemainingHeads--;
			}
		}

		return theMinTimestampItem;
	}
}
