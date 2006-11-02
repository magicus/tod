package tod.impl.dbgrid.merge;

import tod.impl.dbgrid.BidiIterator;

/**
 * A disjunction (boolean OR) merge iterator.
 * @author gpothier
 */
public abstract class DisjunctionIterator<T> extends MergeIterator<T>
{
	public DisjunctionIterator(BidiIterator<T>[] aIterators)
	{
		super(aIterators);
	}
	
	@Override
	protected T fetchNext()
	{
		T theMinTimestampItem = null;
		long theMinTimestamp = Long.MAX_VALUE;

		// Find the item with the minimum timestamp
		for (int i = 0; i < getHeadCount(); i++)
		{
			T theItem = getNextHead(i);

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
		
		if (theMinTimestampItem == null) return null;

		// Move all heads that point to the same event
		for (int i = 0; i < getHeadCount(); i++)
		{
			T theItem = getNextHead(i);

			if (theItem != null && sameEvent(theMinTimestampItem, theItem))
			{
				moveNext(i);
			}
		}

		return theMinTimestampItem;
	}

	@Override
	protected T fetchPrevious()
	{
		T theMaxTimestampItem = null;
		long theMaxTimestamp = 0;

		// Find the item with the maximum timestamp
		for (int i = 0; i < getHeadCount(); i++)
		{
			T theItem = getPreviousHead(i);

			if (theItem != null)
			{
				long theTimestamp = getTimestamp(theItem);
				if (theTimestamp > theMaxTimestamp)
				{
					theMaxTimestamp = theTimestamp;
					theMaxTimestampItem = theItem;
				}
			}
		}
		
		if (theMaxTimestampItem == null) return null;

		// Move all heads that point to the same event
		for (int i = 0; i < getHeadCount(); i++)
		{
			T theItem = getPreviousHead(i);

			if (theItem != null && sameEvent(theMaxTimestampItem, theItem))
			{
				movePrevious(i);
			}
		}

		return theMaxTimestampItem;
	}
}
