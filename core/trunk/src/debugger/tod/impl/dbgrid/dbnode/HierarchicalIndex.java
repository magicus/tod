/*
 * Created on Jul 23, 2006
 */
package tod.impl.dbgrid.dbnode;

import static tod.impl.dbgrid.DebuggerGridConfig.DB_MAX_INDEX_LEVELS;
import static tod.impl.dbgrid.DebuggerGridConfig.DB_PAGE_POINTER_BITS;
import static tod.impl.dbgrid.DebuggerGridConfig.EVENT_TIMESTAMP_BITS;
import tod.agent.AgentUtils;
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
import zz.utils.ArrayStack;
import zz.utils.Stack;
import zz.utils.bit.BitStruct;
import zz.utils.bit.BitUtils;

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
	 * Returns the level number that corresponds to the root page.
	 * This is equivalent to the height of the index.
	 */
	public int getRootLevel()
	{
		return itsRootLevel;
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
	 * Returns an iterator over the tuples of the given page
	 */
	public TupleIterator<IndexTuple> getTupleIterator(Page aPage, int aLevel)
	{
		return new TupleIterator(
				itsFile, 
				aLevel > 0 ? InternalTupleCodec.getInstance() : itsTupleCodec, 
				aPage.asBitStruct());
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
	
	private TupleIterator<? extends IndexTuple> createTupleIterator(PageBitStruct aPage, int aLevel)
	{
		return aLevel > 0 ?
				new TupleIterator<InternalTuple>(itsFile, InternalTupleCodec.getInstance(), aPage)
				: new TupleIterator<T>(itsFile, itsTupleCodec, aPage);
	}
	
	private String printIndex()
	{
		StringBuilder theBuilder = new StringBuilder();
		int theLevel = itsRootLevel;
		Page theCurrentPage = itsRootPage;
		Page theFirstChildPage = null;
		
		while (theLevel > 0)
		{
			theBuilder.append("Level "+theLevel+"\n");
			
			TupleIterator<InternalTuple> theIterator = new TupleIterator<InternalTuple>(
					itsFile, 
					InternalTupleCodec.getInstance(), 
					theCurrentPage.asBitStruct());
			
			int i = 0;
			while (theIterator.hasNext())
			{
				InternalTuple theTuple = theIterator.next();
				if (theFirstChildPage == null)
				{
					theFirstChildPage = itsFile.get(theTuple.getPagePointer());
				}
				
				theBuilder.append(AgentUtils.formatTimestampU(theTuple.getTimestamp()));
				theBuilder.append('\n');
				i++;
			}
			theBuilder.append(""+i+" entries\n");
			
			theCurrentPage = theFirstChildPage;
			theFirstChildPage = null;
			theLevel--;
		}
		
		return theBuilder.toString();
	}
	
	/**
	 * Realizes a fast counting of the tuples of this index, using
	 * upper-level indexes when possible.
	 */
	public long[] fastCountTuples(
			long aT1, 
			long aT2, 
			int aSlotsCount) 
	{
		return new TupleCounter(aT1, aT2, aSlotsCount).count();
	}
	
	/**
	 * Data structure used by {@link HierarchicalIndex#fastCountTuples(long, long, int)}.
	 * @author gpothier
	 */
	private static class LevelData
	{
		public TupleIterator<? extends IndexTuple> iterator;
		public IndexTuple lastTuple;
		
		/**
		 * Minimum number of tuples to read at this level.
		 */
		public int remaining;
		
		public LevelData(TupleIterator< ? extends IndexTuple> aIterator, int aRemaining)
		{
			iterator = aIterator;
			remaining = aRemaining;
		}

		public IndexTuple next()
		{
			lastTuple = iterator.next();
			return lastTuple;
		}
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

	/**
	 * Implementation of fast tuple counting. 
	 * @author gpothier
	 */
	private class TupleCounter
	{
		private long itsT1;
		private long itsT2;
		private int itsSlotsCount;
		
		/**
		 * t2-t1
		 */
		private long itsDT;
		
		private Stack<LevelData> itsStack = new ArrayStack<LevelData>();

		private float[] itsCounts;

		private int[] itsTuplesBetweenPairs;
		private int itsTuplesPerPage0;
		private int itsTuplesPerPageU;
		private LevelData itsCurrentLevel;
		private IndexTuple itsLastTuple;
		private int itsCurrentHeight;
		
		public TupleCounter(long aT1, long aT2, int aSlotsCount)
		{
			itsT1 = aT1;
			itsT2 = aT2;
			itsSlotsCount = aSlotsCount;
			itsDT = (aT2-aT1)/aSlotsCount;
			
			precomputeTuplesBetweenPairs();
			itsCounts = new float[aSlotsCount];

			itsStack.push(new LevelData(
					createTupleIterator(itsRootPage.asBitStruct(), itsRootLevel), 
					0));

		}

		/**
		 * Compute the number of level-0 tuples between each pair of
		 * level-i tuples, for each level.
		 */
		private void precomputeTuplesBetweenPairs()
		{
			itsTuplesBetweenPairs = new int[itsRootLevel+1];
			
			itsTuplesPerPage0 = TupleFinder.getTuplesPerPage(
					itsFile.getPageSize()*8,
					DB_PAGE_POINTER_BITS,
					itsTupleCodec);
			
			itsTuplesPerPageU = TupleFinder.getTuplesPerPage(
					itsFile.getPageSize()*8,
					DB_PAGE_POINTER_BITS,
					InternalTupleCodec.getInstance());
			
			for (int i=0;i<=itsRootLevel;i++)
			{
				itsTuplesBetweenPairs[i] = i > 0 ?
						itsTuplesPerPage0 * BitUtils.powi(itsTuplesPerPageU, i-1)
						: 1;
			}
		}
		
		private void drillDown()
		{
			InternalTuple theTuple = (InternalTuple) itsLastTuple;
			Page theChildPage = itsFile.get(theTuple.getPagePointer());
			
			itsStack.push(new LevelData(
					createTupleIterator(theChildPage.asBitStruct(), itsCurrentHeight-1),
					itsCurrentHeight > 1 ? itsTuplesPerPageU : itsTuplesPerPage0));
		}

		public long[] count() 
		{
//			System.out.println(printIndex());

//			System.out.println("dt: "+AgentUtils.formatTimestampU(dt));
			
			long t = itsT1;
			
			boolean theFinished = false;
			
			while(! theFinished)
			{
				itsCurrentLevel = itsStack.peek();
				itsCurrentHeight = itsRootLevel - itsStack.size() + 1;
				long theStart;
				long theEnd;

				itsLastTuple = itsCurrentLevel.lastTuple;
				if (itsCurrentLevel.iterator.hasNext()) 
				{
					IndexTuple theCurrent = itsCurrentLevel.next();
					theEnd = theCurrent.getTimestamp();
				}
				else if (itsCurrentHeight > 0)
				{
					drillDown();
					continue;
				}
				else
				{
					theFinished = true;
					theEnd = itsLastTimestamp;
				}
				
				if (itsLastTuple == null || theEnd < t) continue;
				theStart = itsLastTuple.getTimestamp();

				itsCurrentHeight = itsRootLevel - itsStack.size() + 1;
				long dtPair = theEnd-theStart;
//				System.out.println("dtPair: "+AgentUtils.formatTimestampU(dtPair));
				
				if (itsCurrentHeight > 0 && dtPair > itsDT/2)
				{
					drillDown();
					continue;
				}
				
				t = theStart;
				int theSlot = (int)(((t - itsT1) * itsSlotsCount) / (itsT2 - itsT1));
				if (theSlot >= itsSlotsCount) break;
				
				if (itsCurrentHeight == 0)
				{
					itsCounts[theSlot] += 1;
				}
				else
				{
					int theCount = itsTuplesBetweenPairs[itsCurrentHeight];
					
					distributeCounts(theCount, theStart, theEnd, theSlot);
				}

				itsCurrentLevel.remaining--;
				if (itsCurrentLevel.remaining == 0)
				{
					itsStack.pop();
				}
			}
			
			long[] theResult = new long[itsSlotsCount];
			for (int i = 0; i < itsCounts.length; i++)
			{
				float f = itsCounts[i];
				theResult[i] = (long) f;
			}
			
			return theResult;
		}

		/**
		 * Distribute a number of events across one or several slots.
		 * @param theCount The number of events to distribute
		 * @param theStart Beginning of the interval in which the events occurred
		 * @param theEnd End of the interval
		 * @param theSlot The main receiving slot
		 */
		private void distributeCounts(
				int theCount, 
				long theStart, 
				long theEnd, 
				int theSlot)
		{
			long theSlotStart = itsT1 + theSlot * (itsT2 - itsT1) / itsSlotsCount;
			long theSlotEnd = theSlotStart + itsDT;
			
			long dtPair = theEnd-theStart;
			
			if (theStart < theSlotStart)
			{
				// We overflow before
				long theBefore = theSlotStart-theStart;
				float theRatio = 1f * theBefore / dtPair;
				if (theSlot > 0) itsCounts[theSlot-1] += theRatio * theCount;
				itsCounts[theSlot] += (1f-theRatio) * theCount;
			}
			else if (theEnd > theSlotEnd)
			{
				// We overflow after
				long theAfter = theEnd-theSlotEnd;
				float theRatio = 1f * theAfter / dtPair;
				if (theSlot < itsCounts.length-1) itsCounts[theSlot+1] += theRatio * theCount;
				itsCounts[theSlot] += (1f-theRatio) * theCount;
			}
			else
			{
				// No overflow - note the invariant dtPair > dt/2
				itsCounts[theSlot] += theCount;
			}
		}
		
	}
}
