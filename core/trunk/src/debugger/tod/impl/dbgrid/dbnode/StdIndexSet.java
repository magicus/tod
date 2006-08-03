/*
 * Created on Jul 24, 2006
 */
package tod.impl.dbgrid.dbnode;

import zz.utils.bit.IntBitStruct;

public class StdIndexSet extends IndexSet<StdIndexSet.Tuple> 
{
	public static final TupleCodec TUPLE_CODEC = new TupleCodec();
	
	public StdIndexSet(String aName, PagedFile aFile, int aIndexCount)
	{
		super(aName, aFile, aIndexCount);
	}
	
	@Override
	protected HierarchicalIndex<Tuple> createIndex(String aName, PagedFile aFile)
	{
		return new HierarchicalIndex<Tuple>(aName, aFile, TUPLE_CODEC);
	}

	public static class TupleCodec extends HierarchicalIndex.TupleCodec<Tuple>
	{

		@Override
		public int getTupleSize()
		{
			return super.getTupleSize() + 64;
		}

		@Override
		public Tuple read(IntBitStruct aBitStruct)
		{
			return new Tuple(aBitStruct);
		}
		
	}
	
	public static class Tuple extends HierarchicalIndex.Tuple
	{
		/**
		 * Internal event pointer
		 */
		private long itsEventPointer;

		public Tuple(long aTimestamp, long aEventPointer)
		{
			super(aTimestamp);
			itsEventPointer = aEventPointer;
		}
		
		public Tuple(IntBitStruct aBitStruct)
		{
			super(aBitStruct);
			itsEventPointer = aBitStruct.readLong(64);
		}

		@Override
		public void writeTo(IntBitStruct aBitStruct)
		{
			super.writeTo(aBitStruct);
			aBitStruct.writeLong(getEventPointer(), 64);
		}
		
		public long getEventPointer()
		{
			return itsEventPointer;
		}
	}

}
