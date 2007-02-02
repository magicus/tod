/*
TOD - Trace Oriented Debugger.
Copyright (C) 2006 Guillaume Pothier (gpothier@dcc.uchile.cl)

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

Parts of this work rely on the MD5 algorithm "derived from the 
RSA Data Security, Inc. MD5 Message-Digest Algorithm".
*/
package tod.impl.dbgrid.db;

import tod.impl.dbgrid.db.file.HardPagedFile;
import tod.impl.dbgrid.db.file.IndexTuple;
import tod.impl.dbgrid.db.file.TupleCodec;
import tod.impl.dbgrid.db.file.HardPagedFile.Page;
import tod.impl.dbgrid.db.file.HardPagedFile.PageBitStruct;
import zz.utils.list.NakedLinkedList.Entry;
import tod.impl.dbgrid.monitoring.AggregationType;
import tod.impl.dbgrid.monitoring.Monitor;
import tod.impl.dbgrid.monitoring.Probe;
import zz.utils.bit.BitStruct;
import zz.utils.cache.MRUBuffer;
import zz.utils.cache.SyncMRUBuffer;
import static tod.impl.dbgrid.DebuggerGridConfig.*;

/**
 * A set of indexes for a given attribute. Within a set,
 * there is one index per possible attribute value.
 * @author gpothier
 */
public abstract class IndexSet<T extends IndexTuple>
{
	/**
	 * This dummy entry is used in {@link #itsIndexes} to differenciate
	 * entries that never existed (null  value) from entries that were
	 * discarded and that are available in the file. 
	 */
	private static Entry DISCARDED_ENTRY = new Entry(null);
	
	private Entry<MyHierarchicalIndex<T>>[] itsIndexes;
	
	/**
	 * The page ids of all the pages that are used to store discarded indexes.
	 */
	private long[] itsIndexPages;
	
	/**
	 * Number of discarded indexes that fit in a page. 
	 */
	private int itsIndexesPerPage;
	
	/**
	 * Name of this index set (for monitoring)
	 */
	private final String itsName;
	
	private HardPagedFile itsFile;
	
	private int itsIndexCount = 0;
	
	private int itsDiscardCount = 0;
	private int itsLoadCount = 0;

	
	public IndexSet(String aName, HardPagedFile aFile, int aIndexCount)
	{
		itsName = aName;
		itsFile = aFile;
		itsIndexes = new Entry[aIndexCount];
		
		// Init discarded index page directory.
		itsIndexesPerPage = aFile.getPageSize()*8/HierarchicalIndex.getSerializedSize(itsFile);
		int theNumPages = (aIndexCount+itsIndexesPerPage-1)/itsIndexesPerPage;
		itsIndexPages = new long[theNumPages];
		
		System.out.println("Created index "+itsName+" with "+aIndexCount+" entries.");
		Monitor.getInstance().register(this);
	}
	
	public void unregister()
	{
		Monitor.getInstance().unregister(this);		
	}

	/**
	 * Returns the tuple codec used for the level 0 of the indexes of this set.
	 */
	public abstract TupleCodec<T> getTupleCodec();
	
	/**
	 * Returns the file used by the indexes of this set.
	 */
	public HardPagedFile getFile()
	{
		return itsFile;
	}
	
	/**
	 * Retrieved the index corresponding to the specified... index.
	 */
	public HierarchicalIndex<T> getIndex(int aIndex)
	{
		if (aIndex >= itsIndexes.length) throw new IndexOutOfBoundsException("Index overflow for "+itsName+": "+aIndex+" >= "+itsIndexes.length);
		
		Entry<MyHierarchicalIndex<T>> theEntry = itsIndexes[aIndex];
		MyHierarchicalIndex<T> theIndex;
		
		if (theEntry == null)
		{
			theIndex = new MyHierarchicalIndex<T>(
					getTupleCodec(), 
					getFile(), 
					this, 
					aIndex);
			
			theEntry = new Entry<MyHierarchicalIndex<T>>(theIndex);
			itsIndexes[aIndex] = theEntry;
			itsIndexCount++;
		}
		else if (theEntry == DISCARDED_ENTRY)
		{
			theIndex = new MyHierarchicalIndex<T>(
					getTupleCodec(), 
					getFile(), 
					getIndexStruct(aIndex), 
					this, 
					aIndex);
			
			theEntry = new Entry<MyHierarchicalIndex<T>>(theIndex);
			itsIndexes[aIndex] = theEntry;
			itsLoadCount++;
		}
		else theIndex = theEntry.getValue();
		
		IndexManager.getInstance().use((Entry) theEntry);
		
		return theIndex;
	}
	
	/**
	 * Returns the bit struct corresponding to the given index,
	 * positionned right before where the index is stored 
	 */
	private BitStruct getIndexStruct(int aIndex)
	{
		long thePageId = itsIndexPages[aIndex/itsIndexesPerPage];
		
		Page thePage;
		if (thePageId == 0)
		{
			thePage = itsFile.create();
			itsIndexPages[aIndex/itsIndexesPerPage] = thePage.getPageId()+1;
		}
		else
		{
			thePage = itsFile.get(thePageId-1);
		}
		
		PageBitStruct theBitStruct = thePage.asBitStruct();
		theBitStruct.setPos((aIndex % itsIndexesPerPage) * HierarchicalIndex.getSerializedSize(itsFile));
		
		return theBitStruct;
	}
	
	private void discardIndex(int aIndex)
	{
		Entry<MyHierarchicalIndex<T>> theEntry = itsIndexes[aIndex];
		HierarchicalIndex<T> theIndex = theEntry.getValue();
		
		theIndex.writeTo(getIndexStruct(aIndex));
		itsIndexes[aIndex] = DISCARDED_ENTRY;
		itsDiscardCount++;
	}

	public void addTuple(int aIndex, T aTuple)
	{
		getIndex(aIndex).add(aTuple);
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
	
	private static class IndexManager extends SyncMRUBuffer<Integer, MyHierarchicalIndex>
	{
		private static IndexManager INSTANCE = new IndexManager();

		public static IndexManager getInstance()
		{
			return INSTANCE;
		}

		private IndexManager()
		{
			super((int) ((DB_PAGE_BUFFER_SIZE/DB_PAGE_SIZE) / 3), false);
		}
		
		@Override
		protected void dropped(MyHierarchicalIndex aValue)
		{
			aValue.getIndexSet().discardIndex(aValue.getIndex());
		}

		@Override
		protected MyHierarchicalIndex fetch(Integer aId)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		protected Integer getKey(MyHierarchicalIndex aValue)
		{
			throw new UnsupportedOperationException();
		}
	}
	
	private static class MyHierarchicalIndex<T extends IndexTuple> 
	extends HierarchicalIndex<T>
	{
		private final IndexSet<T> itsIndexSet;
		
		/**
		 * The position of this index within the set.
		 */
		private final int itsIndex;
		
		
		public MyHierarchicalIndex(
				TupleCodec<T> aTupleCodec, 
				HardPagedFile aFile,
				IndexSet<T> aIndexSet, 
				int aIndex)
		{
			super(aTupleCodec, aFile);
			itsIndexSet = aIndexSet;
			itsIndex = aIndex;
		}

		public MyHierarchicalIndex(
				TupleCodec<T> aTupleCodec, 
				HardPagedFile aFile,
				BitStruct aStoredIndexStruct, 
				IndexSet<T> aIndexSet, 
				int aIndex)
		{
			super(aTupleCodec, aFile, aStoredIndexStruct);
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

	}
	
}
