/*
 * Created on Jul 23, 2006
 */
package tod.impl.dbgrid.dbnode;

import java.util.Iterator;

import tod.impl.dbgrid.DebuggerGridConfig;
import tod.impl.dbgrid.dbnode.PagedFile.PageBitStruct;
import zz.utils.BitStruct;

/**
 * Implementation of a hierarchical index on an attribute value,
 * for instance a particular behavior id.
 * @author gpothier
 */
public class HierarchicalIndex<T extends HierarchicalIndex.Tuple>
{
	private TupleCodec<T> itsTupleCodec;
	
	private int itsInternalTupleSize = 
		DebuggerGridConfig.EVENT_TIMESTAMP_BITS + DebuggerGridConfig.DB_INDEX_PAGE_POINTER_BITS;
	
	private PagedFile itsFile;
	
	private PagedFile.Page itsRootPage;
	private long itsFirstLeafPageId;
	private int itsRootLevel;
	private PageBitStruct[] itsCurrentPages = new PageBitStruct[DebuggerGridConfig.DB_MAX_INDEX_LEVELS];
	
	public HierarchicalIndex(PagedFile aFile, TupleCodec<T> aTupleCodec) 
	{
		itsFile = aFile;
		itsTupleCodec = aTupleCodec;
		
		// Init pages
		itsRootPage = itsFile.createPage();
		itsFirstLeafPageId = itsRootPage.getPageId();
		itsRootLevel = 0;
		itsCurrentPages[0] = itsRootPage.asBitStruct();
	}
	
	/**
	 * Returns an iterator that returns all tuples whose timestamp
	 * is greater than or equal to the specified timestamp.
	 * @param aTimestamp Requested first timestamp, or 0 to start
	 * at the beginning of the list.
	 */
	public Iterator<T> getTupleIterator(long aTimestamp)
	{
		PageBitStruct theBitStruct;
		
		if (aTimestamp == 0)
		{
			theBitStruct = itsFile.getPage(itsFirstLeafPageId).asBitStruct();
		}
		else
		{
			int theLevel = itsRootLevel;
			PagedFile.Page thePage = itsRootPage;
			while (theLevel > 0)
			{
				InternalTuple theTuple = findTuple(
						thePage.asBitStruct(), 
						aTimestamp, 
						InternalTupleCodec.getInstance());
				
				thePage = itsFile.getPage(theTuple.getPagePointer());
				theLevel--;
			}
			
			theBitStruct = thePage.asBitStruct();
			int theIndex = findTupleIndex(theBitStruct, aTimestamp, itsTupleCodec);
			theBitStruct.setPos(theIndex * itsTupleCodec.getTupleSize());
		}

		return new TupleIterator(theBitStruct);
	}
	
	/**
	 * Finds the tuple which has the greatest timestamp value that is
	 * smaller than the given timestamp
	 */
	private <T2 extends Tuple> T2 findTuple(PageBitStruct aPage, long aTimestamp, TupleCodec<T2> aTupleCodec)
	{
		int theIndex = findTupleIndex(aPage, aTimestamp, aTupleCodec);
		aPage.setPos(theIndex * aTupleCodec.getTupleSize());
		return aTupleCodec.read(aPage);
	}
	
	/**
	 * Finds the index of tuple which has the greatest timestamp value that is
	 * smaller than the given timestamp
	 */
	private <T2 extends Tuple> int findTupleIndex(PageBitStruct aPage, long aTimestamp, TupleCodec<T2> aTupleCodec)
	{
		aPage.setPos(0);
		int thePageSize = aPage.getRemainingBits();
		int theTupleCount = (thePageSize - DebuggerGridConfig.DB_INDEX_PAGE_POINTER_BITS) 
			/ aTupleCodec.getTupleSize();
		
		return findTupleIndex(aPage, aTimestamp, aTupleCodec, 0, theTupleCount-1);
	}
	
	/**
	 * Binary search of tuple.
	 */
	private <T2 extends Tuple> int findTupleIndex(PageBitStruct aPage, long aTimestamp, TupleCodec<T2> aTupleCodec, int aFirst, int aLast)
	{
		assert aLast-aFirst > 0;
		
		T2 theFirstTuple = readTuple(aPage, aTupleCodec, aFirst);
		long theFirstTimestamp = theFirstTuple.getTimestamp();
		if (theFirstTimestamp == 0) theFirstTimestamp = Long.MAX_VALUE;
		
		T2 theLastTuple = readTuple(aPage, aTupleCodec, aLast);
		long theLastTimestamp = theLastTuple.getTimestamp();
		if (theLastTimestamp == 0) theLastTimestamp = Long.MAX_VALUE;
		
		if (aTimestamp < theFirstTimestamp) return -1;
		if (aTimestamp == theFirstTimestamp) return aFirst;
		if (aTimestamp >= theLastTimestamp) return aLast;
		
		if (aLast-aFirst == 1) return aFirst;
		
		int theMiddle = (aFirst + aLast) / 2;
		T2 theMiddleTuple = readTuple(aPage, aTupleCodec, theMiddle);
		long theMiddleTimestamp = theMiddleTuple.getTimestamp();
		if (theMiddleTimestamp == 0) theMiddleTimestamp = Long.MAX_VALUE;
		
		if (aTimestamp == theMiddleTimestamp) return theMiddle;
		if (aTimestamp < theMiddleTimestamp) return findTupleIndex(aPage, aTimestamp, aTupleCodec, aFirst, theMiddle);
		else return findTupleIndex(aPage, aTimestamp, aTupleCodec, theMiddle, aLast);
	}
	
	private <T2 extends Tuple> T2 readTuple(PageBitStruct aPage, TupleCodec<T2> aTupleCodec, int aIndex)
	{
		aPage.setPos(aIndex * aTupleCodec.getTupleSize());
		return aTupleCodec.read(aPage);
	}

	public void add(T aTuple)
	{
		add(aTuple, 0, itsTupleCodec.getTupleSize());
	}
	
	private void add(Tuple aTuple, int aLevel, int aTupleSize)
	{
		PageBitStruct thePage = itsCurrentPages[aLevel];
		if (thePage == null)
		{
			assert aLevel == itsRootLevel+1;
			itsRootLevel = aLevel;
			itsRootPage = itsFile.createPage();
			thePage = itsRootPage.asBitStruct();
			itsCurrentPages[aLevel] = thePage;
		}
		
		if (thePage.getRemainingBits() < aTupleSize + DebuggerGridConfig.DB_INDEX_PAGE_POINTER_BITS)
		{
			PageBitStruct theNextPage = itsFile.createPage().asBitStruct();
			long theNextPageId = theNextPage.getPage().getPageId();
			
			// Write next page id (+1: 0 means no next page).
			thePage.writeLong(theNextPageId+1, DebuggerGridConfig.DB_INDEX_PAGE_POINTER_BITS);
			
			// Read timestamp of first tuple
			thePage.setPos(0);
			long theTimestamp = thePage.readLong(DebuggerGridConfig.EVENT_TIMESTAMP_BITS);
			add(
					new InternalTuple(theTimestamp, thePage.getPage().getPageId()),
					aLevel+1, 
					itsInternalTupleSize);
			
			// Save old page
			itsFile.writePage(thePage.getPage());
//			itsFile.freePage(thePage.getPage());
			
			thePage = theNextPage;
			itsCurrentPages[aLevel] = theNextPage;
		}

		aTuple.writeTo(thePage);
	}
	
	/**
	 * A tuple codec is able to serialized and deserialize tuples in a {@link BitStruct}
	 * @author gpothier
	 */
	public abstract static class TupleCodec<T extends Tuple>
	{
		/**
		 * Returns the size (in bits) of each tuple.
		 * Subclasses must override this method to add the size of their tuple's
		 * own attributes to the total
		 */
		public int getTupleSize()
		{
			return DebuggerGridConfig.EVENT_TIMESTAMP_BITS;
		}
		
		/**
		 * Reads a tuple from the given struct.
		 */
		public abstract T read(BitStruct aBitStruct);
		
		public void write(BitStruct aBitStruct, Tuple aTuple)
		{
			aTuple.writeTo(aBitStruct);
		}
	}
	
	
	/**
	 * Base class for all index tuples. Only contains the timestamp.
	 * @author gpothier
	 */
	public abstract static class Tuple
	{
		private long itsTimestamp;

		public Tuple(long aTimestamp)
		{
			itsTimestamp = aTimestamp;
		}
		
		public Tuple(BitStruct aBitStruct)
		{
			itsTimestamp = aBitStruct.readLong(DebuggerGridConfig.EVENT_TIMESTAMP_BITS);
		}

		/**
		 * Writes a serialized representation of this tuple to
		 * the specified struct.
		 * Subclasses should override to serialize additional attributes,
		 * and call super first.
		 */
		public void writeTo(BitStruct aBitStruct)
		{
			aBitStruct.writeLong(getTimestamp(), DebuggerGridConfig.EVENT_TIMESTAMP_BITS);
		}
		
		/**
		 * Returns the timestamp of this tuple.
		 */
		public long getTimestamp()
		{
			return itsTimestamp;
		}
	}
	
	/**
	 * Codec for {@link InternalTuple}.
	 * @author gpothier
	 */
	public static class InternalTupleCodec extends TupleCodec<InternalTuple>
	{
		private static InternalTupleCodec INSTANCE = new InternalTupleCodec();

		public static InternalTupleCodec getInstance()
		{
			return INSTANCE;
		}

		private InternalTupleCodec()
		{
		}
		
		@Override
		public int getTupleSize()
		{
			return super.getTupleSize() + DebuggerGridConfig.DB_INDEX_PAGE_POINTER_BITS;
		}

		@Override
		public InternalTuple read(BitStruct aBitStruct)
		{
			return new InternalTuple(aBitStruct);
		}
	}
	
	/**
	 * Tuple for internal index nodes.
	 */
	public static class InternalTuple extends Tuple
	{
		/**
		 * Page pointer
		 */
		private long itsPagePointer;

		public InternalTuple(long aTimestamp, long aPagePointer)
		{
			super(aTimestamp);
			itsPagePointer = aPagePointer;
		}
		
		public InternalTuple(BitStruct aBitStruct)
		{
			super(aBitStruct);
			itsPagePointer = aBitStruct.readLong(DebuggerGridConfig.DB_INDEX_PAGE_POINTER_BITS);
		}

		@Override
		public void writeTo(BitStruct aBitStruct)
		{
			super.writeTo(aBitStruct);
			aBitStruct.writeLong(getPagePointer(), DebuggerGridConfig.DB_INDEX_PAGE_POINTER_BITS);
		}
		
		public long getPagePointer()
		{
			return itsPagePointer;
		}
	}
	
	public class TupleIterator implements Iterator<T>
	{
		private PageBitStruct itsPage;
		
		private T itsNextTuple;

		public TupleIterator(PageBitStruct aPage)
		{
			itsPage = aPage;
			itsNextTuple = readNextTuple();
		}

		private T readNextTuple()
		{
			if (itsPage.getRemainingBits() < itsTupleCodec.getTupleSize() + DebuggerGridConfig.DB_INDEX_PAGE_POINTER_BITS)
			{
				// We reached the end of the page, we must read the next-page pointer
				long theNextPage = itsPage.readLong(DebuggerGridConfig.DB_INDEX_PAGE_POINTER_BITS);
				if (theNextPage == 0) return null;
				
//				itsFile.freePage(itsPage.getPage());
				itsPage = itsFile.getPage(theNextPage-1).asBitStruct();
			}
			
			T theTuple = itsTupleCodec.read(itsPage);
			if (theTuple.getTimestamp() == 0) return null;
			
			return theTuple;
		}
		
		public boolean hasNext()
		{
			return itsNextTuple != null;
		}

		public T next()
		{
			T theResult = itsNextTuple;
			itsNextTuple = readNextTuple();
			return theResult;
		}

		public void remove()
		{
			throw new UnsupportedOperationException();
		}
		
	}
}
