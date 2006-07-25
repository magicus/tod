/*
 * Created on Jul 24, 2006
 */
package tod.impl.dbgrid.dbnode;

import zz.utils.BitStruct;

public class StdIndexSet<K> extends IndexSet<K, StdIndexSet.Tuple>
{
	public static final TupleCodec TUPLE_CODEC = new TupleCodec();
	
	public StdIndexSet(PagedFile aFile)
	{
		super(aFile);
	}

	@Override
	protected HierarchicalIndex<Tuple> createIndex(PagedFile aFile)
	{
		return new HierarchicalIndex<Tuple>(aFile, TUPLE_CODEC);
	}

	public static class TupleCodec extends HierarchicalIndex.TupleCodec<Tuple>
	{

		@Override
		public int getTupleSize()
		{
			return super.getTupleSize() + 64;
		}

		@Override
		public Tuple read(BitStruct aBitStruct)
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
		
		public Tuple(BitStruct aBitStruct)
		{
			super(aBitStruct);
			itsEventPointer = aBitStruct.readLong(64);
		}

		@Override
		public void writeTo(BitStruct aBitStruct)
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
