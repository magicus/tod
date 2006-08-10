/*
 * Created on Aug 10, 2006
 */
package tod.impl.dbgrid.dbnode;

import static tod.impl.dbgrid.DebuggerGridConfig.*;

import java.util.HashMap;
import java.util.Map;

import tod.impl.dbgrid.ExternalPointer;
import tod.impl.dbgrid.dbnode.HierarchicalIndex.IndexTuple;
import tod.impl.dbgrid.dbnode.HierarchicalIndex.IndexTupleCodec;
import tod.impl.dbgrid.dbnode.PagedFile.Page;
import tod.impl.dbgrid.dbnode.PagedFile.PageBitStruct;
import zz.utils.Utils;
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
	private final CFlowDataTuple itsTupleBuffer = new CFlowDataTuple();
	
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
	}

	/**
	 * Adds a child event to the specified parent.
	 */
	public void add(byte[] aParentPointer, byte[] aChildPointer)
	{
		ChildrenList theChildrenList = getChildrenList(aParentPointer);
		itsTupleBuffer.setPointer(aChildPointer);
		theChildrenList.add(itsTupleBuffer);
		itsChildrenListBuffer.markNode(theChildrenList);
	}
	
	private ChildrenList getChildrenList(byte[] aParentPointer)
	{
		return itsChildrenListBuffer.get(aParentPointer);
	}
	
	private int makeKey(ExternalPointer aPointer)
	{
		int theKey = aPointer.host | aPointer.thread << EVENT_HOST_BITS;
		if (theKey == 0 || theKey == Integer.MAX_VALUE) throw new RuntimeException("key range overflow: "+theKey);
		return theKey;
	}
	
	/**
	 * Returns the children list corresponding to the specified parent event pointer.
	 */
	private ChildrenList fetchChildrenList(byte[] aParentPointer)
	{
		ExternalPointer theParentPointer = ExternalPointer.read(aParentPointer);
		assert theParentPointer.node == itsNode.getNodeId();
		
		// Retrieve the index
		int theKey = makeKey(theParentPointer);
		HierarchicalIndex<CFlowIndexTuple> theIndex = itsIndexes.get(theKey);
		if (theIndex == null)
		{
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
			thePage = itsDataFile.createPage();
			theTuple = new CFlowIndexTuple(theParentPointer.timestamp, thePage.getPageId());
			theIndex.add(theTuple);
		}
		else
		{
			thePage = itsDataFile.getPage(theTuple.getPagePointer());
		}
		
		// Resume writing to the page
		PageBitStruct theStruct = thePage.asBitStruct();
		int theFreeIndex = findFreeDataTuple(theStruct);
		
		return new ChildrenList(aParentPointer, thePage, theFreeIndex*EVENTID_POINTER_SIZE);
	}
	
	private int findFreeDataTuple(PageBitStruct aStruct)
	{
		int theCount = (itsDataFile.getPageSize()*8 - DB_PAGE_POINTER_BITS) / EVENTID_POINTER_SIZE;
		int theIndex = findFreeDataTuple(aStruct, 0, theCount-1);
		return theIndex >= 0 ? theIndex : theCount;
	}
	
	private int findFreeDataTuple(PageBitStruct aStruct, int aFirst, int aLast)
	{
		if (isDataTupleNull(aStruct, aFirst)) return aFirst;
		if (aFirst == aLast) return -1;
		if (! isDataTupleNull(aStruct, aLast)) return -1;
		
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
	
	private static class CFlowDataTupleCodec extends TupleCodec<CFlowDataTuple>
	{

		@Override
		public int getTupleSize()
		{
			return super.getTupleSize() + EVENTID_POINTER_SIZE;
		}

		@Override
		public CFlowDataTuple read(BitStruct aBitStruct)
		{
			return new CFlowDataTuple(aBitStruct.readBytes(EVENTID_POINTER_SIZE));
		}
		
	}
	
	/**
	 * Data tuples contain children event pointers.
	 * @author gpothier
	 */
	private static class CFlowDataTuple extends Tuple
	{
		private byte[] itsPointer;

		public CFlowDataTuple()
		{
		}

		public CFlowDataTuple(byte[] aPointer)
		{
			itsPointer = aPointer;
		}

		@Override
		public boolean isNull()
		{
			return ExternalPointer.isNull(itsPointer);
		}

		@Override
		public void writeTo(BitStruct aBitStruct)
		{
			super.writeTo(aBitStruct);
			aBitStruct.writeBytes(itsPointer, EVENTID_POINTER_SIZE);
		}

		public void setPointer(byte[] aPointer)
		{
			itsPointer = aPointer;
		}
		
		
	}

	private class ChildrenList extends TupleWriter<CFlowDataTuple>
	{
		private byte[] itsParentPointer;
		
		public ChildrenList(byte[] aParentPointer, Page aPage, int aPos)
		{
			super(itsDataFile, DATA_TUPLE_CODEC, aPage, aPos);
			itsParentPointer = aParentPointer;
		}

		public byte[] getParentPointer()
		{
			return itsParentPointer;
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
			itsDataFile.writePage(aValue.getCurrentPage());
		}

		@Override
		protected ChildrenList fetch(byte[] aId)
		{
			return fetchChildrenList(aId);
		}

		@Override
		protected byte[] getKey(ChildrenList aChildrenList)
		{
			return aChildrenList.getParentPointer();
		}
		
	}
}
