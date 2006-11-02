/*
 * Created on Nov 2, 2006
 */
package tod.impl.dbgrid;

import java.util.NoSuchElementException;

public abstract class AbstractBidiIterator<T> implements BidiIterator<T>
{
	private boolean itsNextReady;
	private T itsNext;
	
	private boolean itsPreviousReady;
	private T itsPrevious;
	
	private int itsOffset;
	
	public AbstractBidiIterator()
	{
		this(false);
	}
	
	
	/**
	 * Creates an new iterator.
	 * @param aExhausted if true, the iterator is created exhausted and cannot 
	 * return any element
	 */
	public AbstractBidiIterator(boolean aExhausted)
	{
		itsNext = itsPrevious = null;
		itsNextReady = itsPreviousReady = aExhausted;
		itsOffset = 0;
	}
	
	/**
	 * Fetches the next element and moves the low-level internal pointer
	 * of the iterator. The offset between the low-level internal pointer
	 * and the logical pointer of the iterator is stored in {@link #itsOffset}.
	 * @return Null if no next element is available
	 */
	protected abstract T fetchNext();
	
	
	/**
	 * Fetches the previous element. Symmetric of {@link #fetchNext()}.
	 */
	protected abstract T fetchPrevious();
	
	/**
	 * Reads the next tuple
	 */
	private void readNext()
	{
		if (itsNextReady)
		{
			itsPrevious = itsNext;
			itsPreviousReady = true;
		}
		else itsPreviousReady = false;
		
		itsNext = fetchNext();
		
		itsNextReady = true;
	}

	/**
	 * Reads the previous tuple
	 */
	private void readPrevious()
	{
		if (itsPreviousReady)
		{
			itsNext = itsPrevious;
			itsNextReady = true;
		}
		else itsNextReady = false;
		
		itsPrevious = fetchPrevious();
		
		itsPreviousReady = true;
	}
	
	public boolean hasNext()
	{
		return peekNext() != null;
	}

	public T peekNext()
	{
		if (itsOffset == -1) 
		{
			readNext();
			itsNextReady = false;
			itsOffset = 0;
		}
		
		if (! itsNextReady) 
		{
			readNext();
			if (itsNext != null) itsOffset = 1;
		}
		
		return itsNext;
	}

	public T next()
	{
		if (itsOffset == -1) 
		{
			readNext();
			itsNextReady = false;
		}
		itsOffset = 0;
		
		if (! itsNextReady) readNext();
		if (itsNext == null) throw new NoSuchElementException();
		
		T theResult = itsNext;
		itsNextReady = false;
		
		return theResult;
	}

	public boolean hasPrevious()
	{
		return peekPrevious() != null;
	}

	public T peekPrevious()
	{
		if (itsOffset == 1) 
		{
			readPrevious();
			itsPreviousReady = false;
			itsOffset = 0;
		}
		
		if (! itsPreviousReady) 
		{
			readPrevious();
			if (itsPrevious != null) itsOffset = -1;
		}
		
		return itsPrevious;
	}
	
	public T previous()
	{
		if (itsOffset == 1) 
		{
			readPrevious();
			itsPreviousReady = false;
		}
		itsOffset = 0;
		
		if (! itsPreviousReady) readPrevious();
		if (itsPrevious == null) throw new NoSuchElementException();
		
		T theResult = itsPrevious;
		itsPreviousReady = false;
		
		return theResult;
	}
}
