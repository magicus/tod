/*
 * Created on Aug 10, 2006
 */
package tod.impl.dbgrid.dbnode;

import static tod.impl.dbgrid.DebuggerGridConfig.DB_PAGE_POINTER_BITS;
import static tod.impl.dbgrid.DebuggerGridConfig.EVENTID_POINTER_SIZE;
import static tod.impl.dbgrid.DebuggerGridConfig.EVENT_HOST_BITS;
import static tod.impl.dbgrid.DebuggerGridConfig.EVENT_THREAD_BITS;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import tod.impl.dbgrid.ExternalPointer;
import tod.impl.dbgrid.dbnode.HierarchicalIndex.IndexTuple;
import tod.impl.dbgrid.dbnode.HierarchicalIndex.IndexTupleCodec;
import tod.impl.dbgrid.dbnode.PagedFile.Page;
import tod.impl.dbgrid.dbnode.PagedFile.PageBitStruct;
import tod.impl.dbgrid.monitoring.AggregationType;
import tod.impl.dbgrid.monitoring.Monitor;
import tod.impl.dbgrid.monitoring.Probe;
import zz.utils.bit.BitStruct;
import zz.utils.cache.MRUBuffer;

/**
 * Maintains the control flow tree.
 * @author gpothier
 */
public class CFlowMap
{
	private DatabaseNode itsNode;
	private PagedFile itsIndexFile;
	private PagedFile itsDataFile;
	
	private Map<Integer, HierarchicalIndex<CFlowIndexTuple>> itsIndexes =
		new HashMap<Integer, HierarchicalIndex<CFlowIndexTuple>>();
	
	private ChildrenListBuffer itsChildrenListBuffer = new ChildrenListBuffer();
	
	public static final CFlowIndexTupleCodec INDEX_TUPLE_CODEC = new CFlowIndexTupleCodec();
	public static final CFlowDataTupleCodec DATA_TUPLE_CODEC = new CFlowDataTupleCodec();
	
	private final byte[] itsPointerBuffer = new byte[(EVENTID_POINTER_SIZE+7) / 8];
	
	/**
	 * The number of page seeks performed for appending.
	 */
	private long itsSeekCount = 0;
	
	/**
	 * The number of adds
	 */
	private long itsAddCount = 0;
	
	/**
	 * The number of children list fetches.
	 */
	private long itsFetchCount = 0;
	
	static
	{
		// The key must fit in an int.
		assert EVENT_HOST_BITS+EVENT_THREAD_BITS <= 31;
	}
	

	public CFlowMap(DatabaseNode aNode, PagedFile aIndexFile, PagedFile aDataFile)
	{
		itsNode = aNode;
		itsIndexFile = aIndexFile;
		itsDataFile = aDataFile;
		
		Monitor.getInstance().register(this);
	}

	/**
	 * Adds a child event to the specified parent.
	 */
	public void add(byte[] aParentPointer, byte[] aChildPointer)
	{
		ChildrenList theChildrenList = itsChildrenListBuffer.get(aParentPointer, true);
		theChildrenList.add(aChildPointer);
		itsChildrenListBuffer.markNode(theChildrenList);
		
		itsAddCount++;
	}
	
	/**
	 * Returns an iterator over all the children event pointers
	 * of the given parent pointer.
	 */
	public Iterator<byte[]> getChildrenPointers(byte[] aParentPointer)
	{
		ChildrenList theList = fetchChildrenList(aParentPointer, false, false);
		return theList != null ? theList.createIterator() : null;
	}
	
	/**
	 * Constructs an integer key taking the thread and host attributes
	 * of the given event pointer. 
	 */
	private int makeKey(ExternalPointer aPointer)
	{
		int theKey = aPointer.host | aPointer.thread << EVENT_HOST_BITS;
		if (theKey == 0 || theKey == Integer.MAX_VALUE) throw new RuntimeException("key range overflow: "+theKey);
		return theKey;
	}
	
	/**
	 * Returns the children list corresponding to the specified parent event pointer.
	 * @param aCreate If the list does not exist and this parameter is true, a new list
	 * is created.
	 * @param aAppend If this parameter is true, the returned list will be positioned
	 * for appending at the end. Otherwise, the list will be positioned at the beginning.
	 */
	private ChildrenList fetchChildrenList(
			byte[] aParentPointer, 
			boolean aCreate, 
			boolean aAppend)
	{
		ExternalPointer theParentPointer = ExternalPointer.read(aParentPointer);
		assert theParentPointer.node == itsNode.getNodeId();
		
		// Retrieve the index
		int theKey = makeKey(theParentPointer);
		HierarchicalIndex<CFlowIndexTuple> theIndex = itsIndexes.get(theKey);
		if (theIndex == null)
		{
			if (! aCreate) return null;
			theIndex = new HierarchicalIndex<CFlowIndexTuple>(
					"cflow-"+aParentPointer, 
					itsIndexFile, 
					INDEX_TUPLE_CODEC);
			
			itsIndexes.put(theKey, theIndex);
		}

		// Retrieve the page that contains the children list 
		Page thePage;
		
		CFlowIndexTuple theTuple = theIndex.getTupleAt(theParentPointer.timestamp, true);
		if (theTuple == null)
		{
			if (! aCreate) return null;
			thePage = itsDataFile.createPage();
			theTuple = new CFlowIndexTuple(theParentPointer.timestamp, thePage.getPageId());
			theIndex.add(theTuple);
		}
		else
		{
			thePage = itsDataFile.getPage(theTuple.getPagePointer());
		}
		
		itsFetchCount++;
		
		return new ChildrenList(aParentPointer, thePage, aAppend);
	}
	
	@Probe(key = "cflow map seek count", aggr = AggregationType.SUM)
	public long getSeekCount()
	{
		return itsSeekCount;
	}

	
	private static class CFlowIndexTupleCodec extends IndexTupleCodec<CFlowIndexTuple>
	{
		@Override
		public int getTupleSize()
		{
			return super.getTupleSize() + DB_PAGE_POINTER_BITS;
		}

		@Override
		public CFlowIndexTuple read(BitStruct aBitStruct)
		{
			return new CFlowIndexTuple(aBitStruct);
		}
	}
	
	/**
	 * Index tuples contain children list page pointers
	 * @author gpothier
	 */
	private static class CFlowIndexTuple extends IndexTuple
	{
		/**
		 * Data page pointer 
		 */
		private long itsPagePointer;

		public CFlowIndexTuple(long aTimestamp, long aPagePointer)
		{
			super(aTimestamp);
			itsPagePointer = aPagePointer;
		}
		
		public CFlowIndexTuple(BitStruct aBitStruct)
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
	}
	
	private static class CFlowDataTupleCodec extends TupleCodec<byte[]>
	{
		@Override
		public int getTupleSize()
		{
			return EVENTID_POINTER_SIZE;
		}

		@Override
		public byte[] read(BitStruct aBitStruct)
		{
			return aBitStruct.readBytes(EVENTID_POINTER_SIZE);
		}
		
		@Override
		public void write(BitStruct aBitStruct, byte[] aTuple)
		{
			aBitStruct.writeBytes(aTuple, EVENTID_POINTER_SIZE);
		}
		
		@Override
		public boolean isNull(byte[] aTuple)
		{
			return ExternalPointer.isNull(aTuple);
		}
	}
	

	private class ChildrenList extends TupleWriter<byte[]>
	{
		private byte[] itsParentPointer;
		
		public ChildrenList(byte[] aParentPointer, Page aFirstPage, boolean aAppend)
		{
			super(itsDataFile, DATA_TUPLE_CODEC);
			itsParentPointer = aParentPointer;
			
			if (aAppend)
			{
				Page thePage = aFirstPage;
				int theTupleSize = DATA_TUPLE_CODEC.getTupleSize();
				while (true)
				{
					Long theNextPage = TupleIterator.readNextPageId(thePage, theTupleSize);
					if (theNextPage == null) break;
					
					thePage = itsDataFile.getPage(theNextPage);
					itsSeekCount++;
				}
				
				PageBitStruct theStruct = thePage.asBitStruct();
				int theFreeIndex = findFreeDataTuple(theStruct);
				theStruct.setPos(theFreeIndex * theTupleSize);

				setCurrentStruct(theStruct);
			}
			else
			{
				setCurrentPage(aFirstPage, 0);
			}
		}
		
		private int findFreeDataTuple(PageBitStruct aStruct)
		{
			int theCount = (itsDataFile.getPageSize()*8 - DB_PAGE_POINTER_BITS) / EVENTID_POINTER_SIZE;
			int theIndex = findFreeDataTuple(aStruct, 0, theCount-1);
			return theIndex;
//			return theIndex >= 0 ? theIndex : theCount;
		}
		
		private int findFreeDataTuple(PageBitStruct aStruct, int aFirst, int aLast)
		{
			if (isDataTupleNull(aStruct, aFirst)) return aFirst;
			if (aFirst == aLast) return aLast+1;
			if (! isDataTupleNull(aStruct, aLast)) return aLast+1;
			
			if (aLast - aFirst == 1) return aLast;
			
			int theMiddle = (aFirst + aLast)/2;
			if (isDataTupleNull(aStruct, theMiddle))
				return findFreeDataTuple(aStruct, aFirst, theMiddle-1);
			else return findFreeDataTuple(aStruct, theMiddle+1, aLast);
		}
		
		private boolean isDataTupleNull(PageBitStruct aStruct, int aIndex)
		{
			aStruct.setPos(aIndex*EVENTID_POINTER_SIZE);
			aStruct.readBytes(EVENTID_POINTER_SIZE, itsPointerBuffer);
			return ExternalPointer.isNull(itsPointerBuffer);
		}
		


		public byte[] getParentPointer()
		{
			return itsParentPointer;
		}
		
		/**
		 * Creates a tuple iterator that starts at this list's current position
		 */
		public TupleIterator<byte[]> createIterator()
		{
			return new TupleIterator<byte[]>(getFile(), getTupleCodec(), getCurrentStruct());
		}
	}
	
	private class ChildrenListBuffer extends MRUBuffer<byte[], ChildrenList>
	{

		public ChildrenListBuffer()
		{
			super(256);
		}

		@Override
		protected void saveNode(ChildrenList aValue)
		{
			Page thePage = aValue.getCurrentPage();
			itsDataFile.writePage(thePage);
		}

		@Override
		protected ChildrenList fetch(byte[] aId)
		{
			return fetchChildrenList(aId, true, true);
		}

		@Override
		protected byte[] getKey(ChildrenList aChildrenList)
		{
			return aChildrenList.getParentPointer();
		}
		
	}

}
