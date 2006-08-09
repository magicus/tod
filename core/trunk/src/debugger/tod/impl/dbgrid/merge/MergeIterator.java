package tod.impl.dbgrid.merge;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Base class for merge iterators. Maintains an array of head tuples,
 * one for each source iterator.
 * @author gpothier
 */
public abstract class MergeIterator<T> implements Iterator<T>
{
	private final Iterator<T>[] itsIterators;
	private final T[] itsHeadTuples;
	private T itsNextTuple;

	public MergeIterator(Iterator<T>[] aIterators)
	{
		itsIterators = aIterators;
		itsHeadTuples = (T[]) new Object[itsIterators.length];

		initHeadTuples();

		itsNextTuple = readNextTuple();
	}

	protected void initHeadTuples()
	{
		for (int i = 0; i < itsIterators.length; i++)
			advance(i);
	}

	/**
	 * Advances the specified head until a tuple is found that is accepted by
	 * the tuple filter (a null tuple filter accepts all tuples). If the end of
	 * the stream is reached, the head is set to null and the method returns
	 * false.
	 * 
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
	
	/**
	 * Returns the timestamp of the specified tuple.
	 */
	protected abstract long getTimestamp(T aTuple);
	
	/**
	 * Indicates if the specified tuples represent the same event.
	 */
	protected abstract boolean sameEvent(T aTuple1, T aTuple2);

	protected T[] getHeadTuples()
	{
		return itsHeadTuples;
	}

	public T next()
	{
		if (!hasNext()) throw new NoSuchElementException();
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
