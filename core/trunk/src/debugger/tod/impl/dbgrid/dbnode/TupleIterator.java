/*
 * Created on Aug 10, 2006
 */
package tod.impl.dbgrid.dbnode;

import static tod.impl.dbgrid.DebuggerGridConfig.DB_PAGE_POINTER_BITS;

import java.util.Iterator;
import java.util.NoSuchElementException;

import tod.impl.dbgrid.dbnode.PagedFile.Page;
import tod.impl.dbgrid.dbnode.PagedFile.PageBitStruct;

/**
 * A tuple iterator reads {@link Tuple}s from a linked list of
 * {@link Page}s. Tuples are decoded with a user-specified
 * {@link TupleIterator}.
 * @author gpothier
 */
public class TupleIterator<T extends Tuple> implements Iterator<T>
{
	private PagedFile itsFile;
	private TupleCodec<T> itsTupleCodec;
	private PageBitStruct itsPage;
	private T itsNextTuple;

	/**
	 * Creates an exhausted iterator.
	 */
	public TupleIterator()
	{
		itsNextTuple = null;
	}

	public TupleIterator(PagedFile aFile, TupleCodec<T> aTupleCodec, PageBitStruct aPage)
	{
		itsFile = aFile;
		itsTupleCodec = aTupleCodec;
		itsPage = aPage;
		itsNextTuple = readNextTuple();
	}

	private T readNextTuple()
	{
		if (itsPage.getRemainingBits() < itsTupleCodec.getTupleSize() + DB_PAGE_POINTER_BITS)
		{
			// We reached the end of the page, we must read the next-page
			// pointer
			long theNextPage = itsPage.readLong(DB_PAGE_POINTER_BITS);
			if (theNextPage == 0) return null;

			// itsFile.freePage(itsPage.getPage());
			itsPage = itsFile.getPage(theNextPage - 1).asBitStruct();
		}

		T theTuple = itsTupleCodec.read(itsPage);
		if (theTuple.isNull()) return null;

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