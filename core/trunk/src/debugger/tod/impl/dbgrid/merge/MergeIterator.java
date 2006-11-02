package tod.impl.dbgrid.merge;

import tod.impl.dbgrid.AbstractBidiIterator;
import tod.impl.dbgrid.BidiIterator;

/**
 * Base class for merge iterators. Maintains an array of head items,
 * one for each source iterator.
 * @author gpothier
 */
public abstract class MergeIterator<T> extends AbstractBidiIterator<T>
{
	private final BidiIterator<T>[] itsIterators;

	public MergeIterator(BidiIterator<T>[] aIterators)
	{
		itsIterators = aIterators;
	}
	
	/**
	 * Returns the number of heads (base iterators) of this merge
	 * iterator.
	 */
	protected int getHeadCount()
	{
		return itsIterators.length;
	}

	/**
	 * Moves the specified head to the next element.
	 * @return True if it was possible to move, false otherwise.
	 */
	protected boolean moveNext(int aHeadIndex)
	{
		if (itsIterators[aHeadIndex].hasNext())
		{
			itsIterators[aHeadIndex].next();
			return true;
		}
		else return false;
	}

	/**
	 * Moves the specified head to the previous element.
	 * @return True if it was possible to move, false otherwise.
	 */
	protected boolean movePrevious(int aHeadIndex)
	{
		if (itsIterators[aHeadIndex].hasPrevious())
		{
			itsIterators[aHeadIndex].previous();
			return true;
		}
		else return false;
	}
	
	/**
	 * Returns the timestamp of the specified tuple.
	 */
	protected abstract long getTimestamp(T aItem);
	
	/**
	 * Indicates if the specified items represent the same event.
	 */
	protected abstract boolean sameEvent(T aItem1, T aItem2);

	protected T getNextHead(int aHead)
	{
		return itsIterators[aHead].peekNext();
	}

	protected T getPreviousHead(int aHead)
	{
		return itsIterators[aHead].peekPrevious();
	}
	
}
