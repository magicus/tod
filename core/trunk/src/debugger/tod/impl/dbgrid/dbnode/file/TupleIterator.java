/*
 * Created on Aug 10, 2006
 */
package tod.impl.dbgrid.dbnode.file;

import java.util.Iterator;
import java.util.NoSuchElementException;

import tod.impl.dbgrid.dbnode.file.PageBank.Page;
import tod.impl.dbgrid.dbnode.file.PageBank.PageBitStruct;


/**
 * A tuple iterator reads {@link Tuple}s from a linked list of
 * {@link Page}s. Tuples are decoded with a user-specified
 * {@link TupleCodec}.
 * @author gpothier
 */
public class TupleIterator<T> implements Iterator<T>
{
	private PageBank itsBank;
	private TupleCodec<T> itsTupleCodec;
	private PageBitStruct itsStruct;
	private T itsNextTuple;

	/**
	 * Creates an exhausted iterator.
	 */
	public TupleIterator()
	{
		itsNextTuple = null;
	}

	public TupleIterator(PageBank aBank, TupleCodec<T> aTupleCodec, PageBitStruct aStruct)
	{
		itsBank = aBank;
		itsTupleCodec = aTupleCodec;
		itsStruct = aStruct;
		itsNextTuple = readNextTuple();
	}
	
	private int getPagePointerSize()
	{
		return itsBank.getPagePointerSize();
	}


	
	/**
	 * Reads the next page pointer from the given page.
	 */
	public static Long readNextPageId(Page aPage, int aPagePointerSize, int aTupleSize)
	{
		PageBitStruct theStruct = aPage.asBitStruct();
		int thePageSize = theStruct.getRemainingBits();
		int theTupleCount = (thePageSize - aPagePointerSize) / aTupleSize;
		theStruct.setPos(theTupleCount * aTupleSize);
		long theNextPage = theStruct.readLong(aPagePointerSize);
		return theNextPage == 0 ? null : theNextPage-1;
	}

	private T readNextTuple()
	{
		if (itsStruct.getRemainingBits() < itsTupleCodec.getTupleSize() + getPagePointerSize())
		{
			// We reached the end of the page, we must read the next-page
			// pointer
			long theNextPage = itsStruct.readLong(getPagePointerSize());
			if (theNextPage == 0) return null;

			// itsFile.freePage(itsPage.getPage());
			itsStruct = itsBank.get(theNextPage - 1).asBitStruct();
		}

		T theTuple = itsTupleCodec.read(itsStruct);
		if (itsTupleCodec.isNull(theTuple)) return null;

		return theTuple;
	}

	public boolean hasNext()
	{
		return itsNextTuple != null;
	}

	public T next()
	{
		if (!hasNext()) throw new NoSuchElementException();
		T theResult = itsNextTuple;
		assert theResult != null;
		itsNextTuple = readNextTuple();
		return theResult;
	}

	/**
	 * Returns the next element and closes the iterator. More efficient than
	 * {@link #next()} if only one element is needed.
	 */
	public T nextOneShot()
	{
		if (!hasNext()) throw new NoSuchElementException();
		T theResult = itsNextTuple;
		assert theResult != null;
		itsNextTuple = null;
		return theResult;
	}

	/**
	 * Returns the next tuple (the one that would be returned by {@link #next()}),
	 * or null if the iterator is exhausted, without advancing the iterator.
	 * @return
	 */
	public T getNextTuple()
	{
		return itsNextTuple;
	}

	public void remove()
	{
		throw new UnsupportedOperationException();
	}

}