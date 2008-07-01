/*
evdbng - Fast database for TOD
Copyright (C) 2007 Sylapse
Proprietary and confidential
*/
package tod.impl.evdbng.db;

import static tod.impl.evdbng.DebuggerGridConfig.DB_PAGE_BUFFER_SIZE;
import static tod.impl.evdbng.DebuggerGridConfig.DB_PAGE_SIZE;
import tod.impl.evdbng.db.file.BTree;
import tod.impl.evdbng.db.file.PagedFile;
import tod.impl.evdbng.db.file.Tuple;
import tod.impl.evdbng.db.file.PagedFile.Page;
import tod.impl.evdbng.db.file.PagedFile.PageIOStream;
import zz.utils.cache.SyncMRUBuffer;
import zz.utils.list.NakedLinkedList.Entry;
import zz.utils.monitoring.AggregationType;
import zz.utils.monitoring.Monitor;
import zz.utils.monitoring.Probe;

/**
 * A set of indexes for a given attribute. Within a set,
 * there is one index per possible attribute value.
 * @author gpothier
 */
public abstract class IndexSet<T extends Tuple>
{
	
	/**
	 * This dummy entry is used in {@link #itsIndexes} to differenciate
	 * entries that never existed (null  value) from entries that were
	 * discarded and that are available in the file. 
	 */
	private static Entry DISCARDED_ENTRY = new Entry(null);
	
	/**
	 * The global index manager.
	 */
	private final IndexManager itsIndexManager;
	
	private final Entry<BTreeWrapper<T>>[] itsIndexes;
	
	/**
	 * The page ids of all the pages that are used to store discarded indexes.
	 */
	private final int[] itsIndexPages;
	
	/**
	 * Number of discarded indexes that fit in a page. 
	 */
	private final int itsIndexesPerPage;
	
	/**
	 * Name of this index set (for monitoring)
	 */
	private final String itsName;
	
	private final PagedFile itsFile;
	
	private int itsIndexCount = 0;
	
	private int itsDiscardCount = 0;
	private int itsLoadCount = 0;

	
	public IndexSet(IndexManager aIndexManager, String aName, PagedFile aFile, int aIndexCount)
	{
		itsIndexManager = aIndexManager;
		itsName = aName;
		itsFile = aFile;
		itsIndexes = new Entry[aIndexCount];
		
		// Init discarded index page directory.
		itsIndexesPerPage = aFile.getPageSize()/BTree.getSerializedSize();
		int theNumPages = (aIndexCount+itsIndexesPerPage-1)/itsIndexesPerPage;
		itsIndexPages = new int[theNumPages];
		
		System.out.println("Created index "+itsName+" with "+aIndexCount+" entries.");
		Monitor.getInstance().register(this);
	}
	
	public String getName()
	{
		return itsName;
	}

	public void dispose()
	{
		Monitor.getInstance().unregister(this);		
	}

	public abstract BTree<T> createIndex(int aIndex);
	
	public abstract BTree<T> loadIndex(int aIndex, PageIOStream aStream);
	
	/**
	 * Returns the file used by the indexes of this set.
	 */
	public PagedFile getFile()
	{
		return itsFile;
	}
	
	/**
	 * Retrieved the index corresponding to the specified... index.
	 */
	public BTree<T> getIndex(int aIndex)
	{
		if (aIndex >= itsIndexes.length) throw new IndexOutOfBoundsException("Index overflow for "+itsName+": "+aIndex+" >= "+itsIndexes.length);
		
		Entry<BTreeWrapper<T>> theEntry = itsIndexes[aIndex];
		BTreeWrapper<T> theIndex;
		
		if (theEntry == null)
		{
			// The index never existed.
			BTree<T> theTree = createIndex(aIndex);
			theIndex = new BTreeWrapper<T>(theTree, this, aIndex);
			theEntry = new Entry<BTreeWrapper<T>>(theIndex);
			
			itsIndexes[aIndex] = theEntry;
			itsIndexCount++;
		}
		else if (theEntry == DISCARDED_ENTRY)
		{
			// The index was written to the disk and discarded
			PageIOStream theStream = getIndexPage(aIndex);
			
			BTree<T> theTree = loadIndex(aIndex, theStream);
			theIndex = new BTreeWrapper<T>(theTree, this, aIndex);
			theEntry = new Entry<BTreeWrapper<T>>(theIndex);
			
			itsIndexes[aIndex] = theEntry;
			itsLoadCount++;
		}
		else theIndex = theEntry.getValue();
		
		itsIndexManager.use((Entry) theEntry);
		
		return theIndex.getTree();
	}
	
	/**
	 * Returns the {@link PageIOStream} corresponding to the given index,
	 * positionned right before where the index is stored.
	 */
	private PageIOStream getIndexPage(int aIndex)
	{
		int thePageId = itsIndexPages[aIndex/itsIndexesPerPage];
		
		Page thePage;
		if (thePageId == 0)
		{
			thePage = itsFile.create();
			itsIndexPages[aIndex/itsIndexesPerPage] = thePage.getPageId();
		}
		else
		{
			thePage = itsFile.get(thePageId);
		}
		
		PageIOStream theBitStruct = thePage.asIOStream();
		theBitStruct.setPos((aIndex % itsIndexesPerPage) * BTree.getSerializedSize());
		
		return theBitStruct;
	}
	
	private void discardIndex(int aIndex)
	{
		Entry<BTreeWrapper<T>> theEntry = itsIndexes[aIndex];
		BTree<T> theIndex = theEntry.getValue().getTree();
		
		theIndex.writeTo(getIndexPage(aIndex));
		itsIndexes[aIndex] = DISCARDED_ENTRY;
		itsDiscardCount++;
	}

	@Probe(key = "index count", aggr = AggregationType.SUM)
	public long getIndexCount()
	{
		return itsIndexCount;
	}
	
	@Probe(key = "index discard count", aggr = AggregationType.SUM)
	public int getDiscardCount()
	{
		return itsDiscardCount;
	}

	@Probe(key = "index reload count", aggr = AggregationType.SUM)
	public int getLoadCount()
	{
		return itsLoadCount;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName()+": "+itsName;
	}
	
	/**
	 * The index manager ensures that least-frequently-used indexes
	 * are discarded so that they do not waste memory.
	 * @author gpothier
	 */
	public static class IndexManager extends SyncMRUBuffer<Integer, BTreeWrapper<? extends Tuple>>
	{
		private boolean itsDisposed = false;
		
		public IndexManager()
		{
			super((int) ((DB_PAGE_BUFFER_SIZE/DB_PAGE_SIZE) / 1), false);
		}
		
		/**
		 * Disposes this index manager by dropping all entries.
		 */
		public void dispose()
		{
			itsDisposed = true;
			dropAll();
		}
		
		@Override
		protected void dropped(BTreeWrapper<? extends Tuple> aValue)
		{
			if (itsDisposed) return;
			aValue.getIndexSet().discardIndex(aValue.getIndex());
		}
		
		@Override
		public Entry<BTreeWrapper<? extends Tuple>> getEntry(Integer aKey, boolean aFetch)
		{
			if (itsDisposed) throw new IllegalStateException();
			return super.getEntry(aKey, aFetch);
		}

		@Override
		protected BTreeWrapper<? extends Tuple> fetch(Integer aId)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		protected Integer getKey(BTreeWrapper<? extends Tuple> aValue)
		{
			throw new UnsupportedOperationException();
		}
	}
	
	private static class BTreeWrapper<T extends Tuple>
	{
		private final BTree<T> itsTree;
		private final IndexSet<T> itsIndexSet;
		
		/**
		 * The position of this index within the set.
		 */
		private final int itsIndex;
		
		
		public BTreeWrapper(
				BTree<T> aTree, 
				IndexSet<T> aIndexSet, 
				int aIndex)
		{
			itsTree = aTree;
			itsIndexSet = aIndexSet;
			itsIndex = aIndex;
		}

		public int getIndex()
		{
			return itsIndex;
		}

		public IndexSet<T> getIndexSet()
		{
			return itsIndexSet;
		}

		public BTree<T> getTree()
		{
			return itsTree;
		}
	}
	
}