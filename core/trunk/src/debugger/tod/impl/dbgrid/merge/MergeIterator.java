package tod.impl.dbgrid.merge;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Base class for merge iterators. Maintains an array of head items,
 * one for each source iterator.
 * @author gpothier
 */
public abstract class MergeIterator<T> implements Iterator<T>
{
	private final Iterator<T>[] itsIterators;
	private final T[] itsHeadItems;
	private T itsNextItem;

	public MergeIterator(Iterator<T>[] aIterators)
	{
		itsIterators = aIterators;
		itsHeadItems = (T[]) new Object[itsIterators.length];

		initHeadItems();

		itsNextItem = readNextItem();
	}

	protected void initHeadItems()
	{
		for (int i = 0; i < itsIterators.length; i++)
			advance(i);
	}

	/**
	 * Advances the specified head. If the end of
	 * the stream is reached, the head is set to null and the method returns
	 * false.
	 * 
	 * @return True if it was possible to advance, false otherwise.
	 */
	protected boolean advance(int aHeadIndex)
	{
		if (itsIterators[aHeadIndex].hasNext())
		{
			T theitem = itsIterators[aHeadIndex].next();

			itsHeadItems[aHeadIndex] = theitem;
			return true;
		}
		else
		{
			itsHeadItems[aHeadIndex] = null;
			return false;
		}
	}

	/**
	 * Retrieves the next matching event pointer
	 */
	protected abstract T readNextItem();
	
	/**
	 * Returns the timestamp of the specified tuple.
	 */
	protected abstract long getTimestamp(T aItem);
	
	/**
	 * Indicates if the specified items represent the same event.
	 */
	protected abstract boolean sameEvent(T aItem1, T aItem2);

	protected T[] getHeadItems()
	{
		return itsHeadItems;
	}

	public T next()
	{
		if (!hasNext()) throw new NoSuchElementException();
		T theResult = itsNextItem;
		T theNextItem = readNextItem();
		itsNextItem = theNextItem;
		return theResult;
	}

	public void remove()
	{
		throw new UnsupportedOperationException();
	}

}
