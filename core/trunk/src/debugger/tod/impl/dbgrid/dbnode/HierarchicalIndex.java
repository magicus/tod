/*
 * Created on Jul 23, 2006
 */
package tod.impl.dbgrid.dbnode;

import static tod.impl.dbgrid.DebuggerGridConfig.DB_MAX_INDEX_LEVELS;
import static tod.impl.dbgrid.DebuggerGridConfig.DB_PAGE_POINTER_BITS;
import static tod.impl.dbgrid.DebuggerGridConfig.EVENT_TIMESTAMP_BITS;
import tod.impl.dbgrid.dbnode.file.HardPagedFile;
import tod.impl.dbgrid.dbnode.file.IndexTuple;
import tod.impl.dbgrid.dbnode.file.IndexTupleCodec;
import tod.impl.dbgrid.dbnode.file.TupleCodec;
import tod.impl.dbgrid.dbnode.file.TupleFinder;
import tod.impl.dbgrid.dbnode.file.TupleIterator;
import tod.impl.dbgrid.dbnode.file.TupleWriter;
import tod.impl.dbgrid.dbnode.file.PageBank.Page;
import tod.impl.dbgrid.dbnode.file.PageBank.PageBitStruct;
import tod.impl.dbgrid.monitoring.AggregationType;
import tod.impl.dbgrid.monitoring.Probe;
import zz.utils.bit.BitStruct;

/**
 * Implementation of a hierarchical index on an attribute value,
 * for instance a particular behavior id.
 * @author gpothier
 */
public class HierarchicalIndex<T extends IndexTuple>
{
	private TupleCodec<T> itsTupleCodec;
	
	private HardPagedFile itsFile;
	
	private Page itsRootPage;
	private long itsFirstLeafPageId;
	private int itsRootLevel;
	private MyTupleWriter[] itsTupleWriters = new MyTupleWriter[DB_MAX_INDEX_LEVELS];
	
	/**
	 * The timestamp of the last added tuple
	 */
	private long itsLastTimestamp = 0;
	
	/**
	 * Number of pages per level
	 */
//	private int[] itsPagesCount = new int[DB_MAX_INDEX_LEVELS];
	
	private long itsLeafTupleCount = 0;

	/**
	 * A name for this index (for monitoring);
	 */
	private final String itsName;
	
	public HierarchicalIndex(String aName, HardPagedFile aFile, TupleCodec<T> aTupleCodec) 
	{
		itsName = aName;
		itsFile = aFile;
		itsTupleCodec = aTupleCodec;
		
		// Init pages
		itsTupleWriters[0] = new MyTupleWriter<T>(itsFile, itsTupleCodec, 0);
		itsRootPage = itsTupleWriters[0].getCurrentPage();
		itsFirstLeafPageId = itsRootPage.getPageId();
		itsRootLevel = 0;
		
//		Monitor.getInstance().register(this);
	}
	
	/**
	 * Returns the first tuple that has a timestamp greater or equal
	 * than the specified timestamp, if any.
	 * @param aExact If true, only a tuple with exactly the specified
	 * timestamp is returned.
	 * @return A matching tuple, or null if none is found.
	 */
	public T getTupleAt(long aTimestamp, boolean aExact)
	{
		TupleIterator<T> theIterator = getTupleIterator(aTimestamp);
		if (! theIterator.hasNext()) return null;
		T theTuple = theIterator.nextOneShot();
		if (aExact && theTuple.getTimestamp() != aTimestamp) return null;
		else return theTuple;
	}
	
	/**
	 * Returns an iterator that returns all tuples whose timestamp
	 * is greater than or equal to the specified timestamp.
	 * @param aTimestamp Requested first timestamp, or 0 to start
	 * at the beginning of the list.
	 */
	public TupleIterator<T> getTupleIterator(long aTimestamp)
	{
//		System.out.println("Get    "+aTimestamp);
		if (aTimestamp == 0)
		{
			PageBitStruct theBitStruct = itsFile.get(itsFirstLeafPageId).asBitStruct();
			return new TupleIterator<T>(itsFile, itsTupleCodec, theBitStruct);
		}
		else
		{
			int theLevel = itsRootLevel;
			Page thePage = itsRootPage;
			while (theLevel > 0)
			{
//				System.out.println("Level: "+theLevel);
				InternalTuple theTuple = TupleFinder.findTuple(
						thePage.asBitStruct(), 
						DB_PAGE_POINTER_BITS,
						aTimestamp, 
						InternalTupleCodec.getInstance(),
						true);
				
				if (theTuple == null) 
				{
					// The first tuple of this index is after the specified timestamp
					thePage = itsFile.get(itsFirstLeafPageId);
					PageBitStruct theBitStruct = thePage.asBitStruct();
					return new TupleIterator<T>(itsFile, itsTupleCodec, theBitStruct);
				}
				
				thePage = itsFile.get(theTuple.getPagePointer());
				theLevel--;
			}
			
			PageBitStruct theBitStruct = thePage.asBitStruct();
			int theIndex = TupleFinder.findTupleIndex(
					theBitStruct,
					DB_PAGE_POINTER_BITS,
					aTimestamp, 
					itsTupleCodec,
					true);
			
			if (theIndex == -1) return new TupleIterator<T>();
			else
			{
				theBitStruct.setPos(theIndex * itsTupleCodec.getTupleSize());
				TupleIterator<T> theIterator = new TupleIterator<T>(itsFile, itsTupleCodec, theBitStruct);
				T theTuple = theIterator.getNextTuple();
				if (theIterator.hasNext() && theTuple.getTimestamp() < aTimestamp)
					theIterator.next();
				
				return theIterator;
			}
		}
	}
	
	/**
	 * Adds a tuple to this index.
	 */
	public void add(T aTuple)
	{
		assert checkTimestamp(aTuple);
		add(aTuple, 0, itsTupleCodec);
		itsLeafTupleCount++;
	}
	
	/**
	 * Checks that the newly added tuple's timestamp is greater than
	 * the last timestamp.
	 */
	private boolean checkTimestamp(T aTuple)
	{
		long theTimestamp = aTuple.getTimestamp();
		assert theTimestamp >= itsLastTimestamp;
		itsLastTimestamp = theTimestamp;
		return true;
	}
	
	private <T1 extends IndexTuple> void add(T1 aTuple, int aLevel, TupleCodec<T1> aCodec)
	{
		MyTupleWriter<T1> theWriter = itsTupleWriters[aLevel];
		if (theWriter == null)
		{
			assert aLevel == itsRootLevel+1;
			itsRootLevel = aLevel;
			theWriter = new MyTupleWriter<T1>(itsFile, aCodec, aLevel);
			itsTupleWriters[aLevel] = theWriter;
			itsRootPage = itsTupleWriters[aLevel].getCurrentPage();
		}
		
		theWriter.add(aTuple);
	}
	
	/**
	 * Returns the total number of pages occupied by this index
	 */
	@Probe(key = "index pages", aggr = AggregationType.SUM)	
	public int getTotalPageCount()
	{
		int theCount = 0;
		for (TupleWriter theWriter : itsTupleWriters) theCount += theWriter.getPagesCount();
		return theCount;
	}
	
	@Probe(key = "leaf index tuples", aggr = AggregationType.SUM)
	public long getLeafTupleCount()
	{
		return itsLeafTupleCount;
	}
	
	public int getPageSize()
	{
		return itsFile.getPageSize();
	}
	
	@Probe(key = "index storage", aggr = AggregationType.SUM)
	public long getStorageSpace()
	{
		return getTotalPageCount() * getPageSize();
	}

	@Override
	public String toString()
	{
		return "Index "+itsName;
	}

	/**
	 * Codec for {@link InternalTuple}.
	 * @author gpothier
	 */
	private static class InternalTupleCodec extends IndexTupleCodec<InternalTuple>
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
			return super.getTupleSize() + DB_PAGE_POINTER_BITS;
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
	private static class InternalTuple extends IndexTuple
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
			itsPagePointer = aBitStruct.readLong(DB_PAGE_POINTER_BITS);
		}

		@Override
		public void writeTo(BitStruct aBitStruct)
		{
			super.writeTo(aBitStruct);
			aBitStruct.writeLong(getPagePointer(), DB_PAGE_POINTER_BITS);
		}
		
		public long getPagePointer()
		{
			return itsPagePointer;
		}
		
		@Override
		public String toString()
		{
			return String.format("%s: t=%d p=%d",
					getClass().getSimpleName(),
					getTimestamp(),
					getPagePointer());
		}
	}
	
	private class MyTupleWriter<T extends IndexTuple> extends TupleWriter<T>
	{
		private final int itsLevel;
		
		public MyTupleWriter(HardPagedFile aFile, TupleCodec<T> aTupleCodec, final int aLevel)
		{
			super(aFile, aTupleCodec, aFile.create(), 0);
			itsLevel = aLevel;
		}

		@Override
		protected void newPageHook(PageBitStruct aStruct, long aNewPageId)
		{
			// If this is the first time we finish a page at this level,
			// we must update upper level index.
			if (itsRootLevel == itsLevel)
			{
				// Read timestamp of first tuple
				aStruct.setPos(0);
				long theTimestamp = aStruct.readLong(EVENT_TIMESTAMP_BITS);
				
				HierarchicalIndex.this.add(
						new InternalTuple(theTimestamp, aStruct.getPage().getPageId()),
						itsLevel+1, 
						InternalTupleCodec.getInstance());
			}
		}

		@Override
		protected void startPageHook(PageBitStruct aStruct, T aTuple)
		{
			if (itsRootLevel > itsLevel)
			{
				// When we write the first tuple of a page we also update indexes.
				long theTimestamp = aTuple.getTimestamp();
				HierarchicalIndex.this.add(
						new InternalTuple(theTimestamp, aStruct.getPage().getPageId()),
						itsLevel+1, 
						InternalTupleCodec.getInstance());
			}
		}
		
	}

}
