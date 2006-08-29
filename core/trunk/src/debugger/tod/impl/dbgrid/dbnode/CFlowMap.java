/*
 * Created on Aug 10, 2006
 */
package tod.impl.dbgrid.dbnode;

import static tod.impl.dbgrid.DebuggerGridConfig.DB_CFLOW_CHILDREN_LIST_BUFFER_SIZE;
import static tod.impl.dbgrid.DebuggerGridConfig.DB_MIN_CFLOW_PAGE_SIZE;
import static tod.impl.dbgrid.DebuggerGridConfig.EVENTID_POINTER_SIZE;
import static tod.impl.dbgrid.DebuggerGridConfig.EVENT_HOST_BITS;
import static tod.impl.dbgrid.DebuggerGridConfig.EVENT_THREAD_BITS;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import tod.impl.dbgrid.ExternalPointer;
import tod.impl.dbgrid.dbnode.file.ExponentialPageBank;
import tod.impl.dbgrid.dbnode.file.HardPagedFile;
import tod.impl.dbgrid.dbnode.file.IndexTuple;
import tod.impl.dbgrid.dbnode.file.IndexTupleCodec;
import tod.impl.dbgrid.dbnode.file.PageBank;
import tod.impl.dbgrid.dbnode.file.SoftPagedFile;
import tod.impl.dbgrid.dbnode.file.TupleCodec;
import tod.impl.dbgrid.dbnode.file.TupleIterator;
import tod.impl.dbgrid.dbnode.file.TupleWriter;
import tod.impl.dbgrid.dbnode.file.PageBank.Page;
import tod.impl.dbgrid.dbnode.file.PageBank.PageBitStruct;
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
	private HardPagedFile itsIndexFile;
	private SoftPagedFile itsDataFile;
	
	private Map<Integer, HierarchicalIndex<CFlowIndexTuple>> itsIndexes =
		new HashMap<Integer, HierarchicalIndex<CFlowIndexTuple>>();
	
	private ChildrenListBuffer itsChildrenListBuffer = new ChildrenListBuffer();
	
	public final CFlowIndexTupleCodec INDEX_TUPLE_CODEC = new CFlowIndexTupleCodec();
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
	

	public CFlowMap(DatabaseNode aNode, HardPagedFile aIndexFile, HardPagedFile aDataFile)
	{
		itsNode = aNode;
		itsIndexFile = aIndexFile;
		itsDataFile = new SoftPagedFile(aDataFile, DB_MIN_CFLOW_PAGE_SIZE);
		
		Monitor.getInstance().register(this);
	}

	/**
	 * Adds a child event to the specified parent.
	 */
	public void add(byte[] aParentPointer, byte[] aChildPointer)
	{
		ChildrenList theChildrenList = itsChildrenListBuffer.get(aParentPointer, true);
		theChildrenList.add(aChildPointer);
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
			thePage = itsDataFile.create(DB_MIN_CFLOW_PAGE_SIZE);
			theTuple = new CFlowIndexTuple(theParentPointer.timestamp, thePage.getPageId());
			theIndex.add(theTuple);
		}
		else
		{
			thePage = itsDataFile.get(theTuple.getPagePointer());
		}
		
		itsFetchCount++;
		
		return createChildrenList(aParentPointer, thePage, aAppend);
	}
	
	/**
	 * Factory method for children lists.
	 */
	private ChildrenList createChildrenList(
			byte[] aParentPointer, 
			Page aFirstPage, 
			boolean aAppend)
	{
		PageBank theBank;
		PageBitStruct theStartingStruct;
		
		if (aAppend)
		{
			Page thePage = aFirstPage;
			int thePageSize = itsDataFile.getMinPageSize();
			int theTupleSize = DATA_TUPLE_CODEC.getTupleSize();
			while (true)
			{
				assert thePage.getSize() == thePageSize;
				if (thePageSize < itsDataFile.getMaxPageSize()) thePageSize *= 2;
				
				Long theNextPage = TupleIterator.readNextPageId(
						thePage, 
						itsDataFile.getPagePointerSize(), 
						theTupleSize);
				
				if (theNextPage == null) break;
				
				thePage = itsDataFile.get(theNextPage);
				itsSeekCount++;
			}
			
			theStartingStruct = thePage.asBitStruct();
			int theFreeIndex = findFreeDataTuple(theStartingStruct);
			theStartingStruct.setPos(theFreeIndex * theTupleSize);

			theBank = new ExponentialPageBank(itsDataFile, thePageSize); 
		}
		else
		{
			assert aFirstPage.getSize() == DB_MIN_CFLOW_PAGE_SIZE;
			theBank = new ExponentialPageBank(itsDataFile, DB_MIN_CFLOW_PAGE_SIZE*2);
			theStartingStruct = aFirstPage.asBitStruct();
		}
		
		return new ChildrenList(aParentPointer, theBank, theStartingStruct);
	}
	
	private int findFreeDataTuple(PageBitStruct aStruct)
	{
		int theCount = (aStruct.getTotalBits() - itsDataFile.getPagePointerSize()) / EVENTID_POINTER_SIZE;
		int theIndex = findFreeDataTuple(aStruct, 0, theCount-1);
		return theIndex;
//		return theIndex >= 0 ? theIndex : theCount;
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

	
	@Probe(key = "cflow map seek count", aggr = AggregationType.SUM)
	public long getSeekCount()
	{
		return itsSeekCount;
	}
	
	@Probe(key = "cflow map add count", aggr = AggregationType.SUM)
	public long getAddCount()
	{
		return itsAddCount;
	}

	@Probe(key = "cflow map fetch count", aggr = AggregationType.SUM)
	public long getFetchCount()
	{
		return itsFetchCount;
	}

	/**
	 * Returns the page pointer size for data pages.
	 */
	private int getDataPagePointerSize()
	{
		return itsDataFile.getPagePointerSize();
	}

	
	private class CFlowIndexTupleCodec extends IndexTupleCodec<CFlowIndexTuple>
	{
		@Override
		public int getTupleSize()
		{
			return super.getTupleSize() + getDataPagePointerSize();
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
	private class CFlowIndexTuple extends IndexTuple
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
			itsPagePointer = aBitStruct.readLong(getDataPagePointerSize());
		}

		@Override
		public void writeTo(BitStruct aBitStruct)
		{
			super.writeTo(aBitStruct);
			aBitStruct.writeLong(getPagePointer(), getDataPagePointerSize());
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
	
	private static class ChildrenList extends TupleWriter<byte[]>
	{
		private byte[] itsParentPointer;
		
		public ChildrenList(byte[] aParentPointer, PageBank aBank, PageBitStruct aCurrentStruct)
		{
			super(aBank, DATA_TUPLE_CODEC);
			itsParentPointer = aParentPointer;
			setCurrentStruct(aCurrentStruct);
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
			return new TupleIterator<byte[]>(getBank(), getTupleCodec(), getCurrentStruct());
		}
	}
	
	private class ChildrenListBuffer extends MRUBuffer<byte[], ChildrenList>
	{
		public ChildrenListBuffer()
		{
			super(DB_CFLOW_CHILDREN_LIST_BUFFER_SIZE);
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
